package io.github.webbasedwodt.WoDTShadowingAdapter.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.things.model.Thing;
import io.github.webbasedwodt.DTDManager.api.DTDManager;
import io.github.webbasedwodt.DTDManager.impl.WoTDTDManager;
import io.github.webbasedwodt.DTKGEngine.api.DTKGEngine;
import io.github.webbasedwodt.DTKGEngine.impl.JenaDTKGEngine;
import io.github.webbasedwodt.PlatformManagementInterface.api.PlatformManagementInterface;
import io.github.webbasedwodt.PlatformManagementInterface.impl.BasePlatformManagementInterface;
import io.github.webbasedwodt.WoDTDigitalTwinInterface.api.WoDTWebServer;
import io.github.webbasedwodt.WoDTDigitalTwinInterface.impl.WoDTWebServerImpl;
import io.github.webbasedwodt.WoDTShadowingAdapter.api.WoDTDigitalAdapterConfiguration;
import io.github.webbasedwodt.common.ThingModelElement;
import static io.github.webbasedwodt.common.ThingModelUtils.convertStringToType;
import static io.github.webbasedwodt.common.ThingModelUtils.extractSubPropertiesNames;
import static io.github.webbasedwodt.common.ThingModelUtils.extractSubPropertyValue;

/**
* This class represents the Eclipse Ditto Adapter that allows to implement the WoDT Digital Twin layer
* implementing the components of the Abstract Architecture.
*/
public final class WoDTDigitalAdapter {

    private final DTKGEngine dtkgEngine;
    private final DTDManager dtdManager;
    private final WoDTWebServer woDTWebServer;
    private final PlatformManagementInterface platformManagementInterface;
    private final WoDTDigitalAdapterConfiguration configuration;    
    private final DittoThingListener dittoClientThread;

    /**
    * Default constructor.
    * @param configuration the configuration of the Digital Adapter
    */
    public WoDTDigitalAdapter(final WoDTDigitalAdapterConfiguration configuration) {
        this.configuration = configuration;
        this.platformManagementInterface = new BasePlatformManagementInterface(
            this.configuration.getDigitalTwinUri());
        this.dtkgEngine = new JenaDTKGEngine(
                this.configuration.getDigitalTwinUri(),
                this.configuration.getOntology().getDigitalTwinType()
        );
        this.dtdManager = new WoTDTDManager(
            this.configuration,
            this.platformManagementInterface
        );
        this.syncWithDittoThing(this.configuration.getDittoThing());
        this.woDTWebServer = new WoDTWebServerImpl(
            this.configuration.getPortNumber(),
            this.dtkgEngine,
            this.dtdManager,
            this.platformManagementInterface
        );
        this.dittoClientThread = new DittoThingListener(
                configuration.getDittoObservationEndpoint(),
                configuration.getDittoUsername(),
                configuration.getDittoPassword(),
                this
        );
        this.startAdapter();        
    }

    private void startAdapter() {
        this.woDTWebServer.start();
        this.configuration.getPlatformToRegister().forEach(platform ->
                this.platformManagementInterface.registerToPlatform(platform, this.dtdManager.getDTD().toJsonString()));
        dittoClientThread.start();
    }

    public void stopAdapter() {
        this.platformManagementInterface.signalDigitalTwinDeletion();
        this.dittoClientThread.stopThread();
    }

    private void handleRelationship(String key, String value, boolean isDeletion) {
        configuration.getOntology().mapRelationshipInstance(key, value).ifPresent(triple -> {
            if (isDeletion) {
                this.dtkgEngine.removeRelationship(triple.getLeft());
                this.dtdManager.removeRelationship(key);
            } else {
                this.dtkgEngine.addRelationship(triple.getLeft(), triple.getRight());
                this.dtdManager.addRelationship(key);
            }
        });
    }
    
    private void handleProperty(String key, String value, boolean isFeatureProperty, boolean isDeletion, String featureId) {
        String fullPropertyName = (isFeatureProperty ? featureId + "_" : "") + key;
        configuration.getOntology().mapPropertyData(fullPropertyName, convertStringToType(value)).ifPresent(triple -> {
            if (isDeletion) {
                this.dtkgEngine.removeProperty(triple.getLeft());
                this.dtdManager.removeProperty(fullPropertyName);
            } else {
                this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight());
                this.dtdManager.addProperty(fullPropertyName);
            }
        });
    }
    
    private void handleAction(String actionId, boolean isDeletion, String featureId) {
        String fullActionName = (featureId != null ? featureId + "_" : "") + actionId;
        if (isDeletion) {
            this.dtdManager.removeAction(fullActionName);
            this.dtkgEngine.removeActionId(fullActionName);
        } else {
            this.dtdManager.addAction(fullActionName);
            this.dtkgEngine.addActionId(fullActionName);
        }
    }
    
    private void handleEvent(String eventId, boolean isDeletion, String featureId) {
        String fullEventName = (featureId != null ? featureId + "_" : "") + eventId;
        if (isDeletion) {
            this.dtdManager.removeEvent(fullEventName);
        } else {
            this.dtdManager.addEvent(fullEventName);
        }
    }

    private void syncWithDittoThing(final Thing thing) {
        // Thing Attributes (Relationships and Properties)
        thing.getAttributes().ifPresent(attributes -> {
            attributes.forEach((attribute) -> {
                if (attribute.getKey().toString().contains("rel-")) {
                    handleRelationship(attribute.getKey().toString(), attribute.getValue().asString(), false);
                } else {
                    handleProperty(attribute.getKey().toString(), attribute.getValue().toString(), false, false, null);
                }
            });
        });

        // Thing Features (Properties, Actions, Events)
        thing.getFeatures().ifPresent(features -> {
            features.forEach((feature) -> {
                feature.getProperties().ifPresent(properties -> {
                    properties.forEach((property) -> {
                        List<String> subProperties = extractSubPropertiesNames(property.getValue().toString());
                        if (!subProperties.isEmpty()) {
                            subProperties.forEach(subProperty -> {
                                String fullKey = property.getKey().toString() + "_" + subProperty;
                                handleProperty(fullKey, extractSubPropertyValue(property.getValue().toString(), subProperty), true, false, feature.getId());
                            });
                        } else {
                            handleProperty(property.getKey().toString(), property.getValue().toString(), true, false, feature.getId());
                        }
                    });
                });

                this.configuration.getOntology().getAvailableActions().stream()
                        .filter(action -> action.getFeature().isPresent() && action.getFeature().get().equals(feature.getId()))
                        .forEach(action -> handleAction(action.getField(), false, feature.getId()));

                this.configuration.getOntology().getAvailableEvents().stream()
                    .filter(event -> event.getFeature().isPresent() && event.getFeature().get().equals(feature.getId()))
                    .forEach(event -> handleEvent(event.getField(), false, feature.getId()));
            });
        });

        // Thing Actions
        this.configuration.getOntology().getAvailableActions().stream()
                .filter(action -> action.getFeature().isEmpty())
                .forEach(action -> handleAction(action.getField(), false, null));

        // Thing Events
        this.configuration.getOntology().getAvailableEvents().stream()
                .filter(event -> event.getFeature().isEmpty())
                .forEach(event -> handleEvent(event.getField(), false, null));
    }

    public void onThingChange(ThingChange change) {
        switch (change.getAction()) {
            case CREATED, MERGED, UPDATED:
                change.getThing().get().getAttributes().ifPresent(attributes -> {
                    attributes.forEach((attribute) -> {
                        if (attribute.getKey().toString().contains("rel-")) {
                            handleRelationship(attribute.getKey().toString(), null, true);
                            handleRelationship(attribute.getKey().toString(), attribute.getValue().asString(), false);
                        } else {
                            handleProperty(attribute.getKey().toString(), attribute.getValue().toString(), false, false, null);
                        }
                    });
                });
                change.getThing().get().getFeatures().ifPresent(features -> {
                    features.forEach((feature) -> {
                        feature.getProperties().ifPresent(properties -> {
                            properties.forEach((property) -> {
                                List<String> subProperties = extractSubPropertiesNames(property.getValue().toString());
                                if (!subProperties.isEmpty()) {
                                    subProperties.forEach(subProperty -> {
                                        String fullKey = property.getKey().toString() + "_" + subProperty;
                                        handleProperty(fullKey, extractSubPropertyValue(property.getValue().toString(), subProperty), true, false, feature.getId());
                                    });
                                } else {
                                    handleProperty(property.getKey().toString(), property.getValue().toString(), true, false, feature.getId());
                                }
                            });
                        });
                        this.configuration.getOntology().getAvailableActions().stream()
                                .filter(action -> action.getFeature().isPresent() && action.getFeature().get().equals(feature.getId()))
                                .forEach(action -> handleAction(action.getField(), false, feature.getId()));

                        this.configuration.getOntology().getAvailableEvents().stream()
                                .filter(event -> event.getFeature().isPresent() && event.getFeature().get().equals(feature.getId()))
                                .forEach(event -> handleEvent(event.getField(), false, feature.getId()));
                    });
                });
                break;
            case DELETED:
                String elementToDelete = change.getPath().toString().split("/")[2];
                if (change.getPath().toString().contains("attributes")) {
                    if (elementToDelete.contains("rel-")) {
                        handleRelationship(elementToDelete, null, true);
                    } else {
                        handleProperty(elementToDelete, null, false, true, null);
                    }
                }
                if (change.getPath().toString().contains("features")) {
                    List<ThingModelElement> matchingProperties = this.configuration.getOntology().getAvailableProperties().stream()
                            .filter(p -> p.getFeature().isPresent() && p.getFeature().get().equals(elementToDelete))
                            .collect(Collectors.toList());
                    matchingProperties.forEach(prop -> handleProperty(prop.getField(), null, true, true, elementToDelete));

                    this.configuration.getOntology().getAvailableActions().stream()
                            .filter(action -> action.getFeature().isPresent() && action.getFeature().get().equals(elementToDelete))
                            .forEach(action -> handleAction(action.getField(), true, elementToDelete));

                    this.configuration.getOntology().getAvailableEvents().stream()
                            .filter(event -> event.getFeature().isPresent() && event.getFeature().get().equals(elementToDelete))
                            .forEach(event -> handleEvent(event.getField(), true, elementToDelete));
                }
                break;
            default:
                break;
        }
    }
}