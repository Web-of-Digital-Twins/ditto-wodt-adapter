package org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.wodt.DTDManager.api.DTDManager;
import org.eclipse.ditto.wodt.DTDManager.impl.WoTDTDManager;
import org.eclipse.ditto.wodt.DTKGEngine.api.DTKGEngine;
import org.eclipse.ditto.wodt.DTKGEngine.impl.JenaDTKGEngine;
import org.eclipse.ditto.wodt.PlatformManagementInterface.api.PlatformManagementInterface;
import org.eclipse.ditto.wodt.PlatformManagementInterface.impl.BasePlatformManagementInterface;
import org.eclipse.ditto.wodt.WoDTDigitalTwinInterface.api.WoDTWebServer;
import org.eclipse.ditto.wodt.WoDTDigitalTwinInterface.impl.WoDTWebServerImpl;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.api.WoDTDigitalAdapterConfiguration;
import org.eclipse.ditto.wodt.common.DittoBase;
import org.eclipse.ditto.wodt.common.ThingModelElement;
import static org.eclipse.ditto.wodt.common.ThingUtils.extractDataFromThing;

/**
 * This class represents the Eclipse Ditto Adapter that allows to implement the WoDT Digital Twin layer
* implementing the components of the Abstract Architecture.
*/
public final class WoDTDigitalAdapter {

    private static final int DITTO_PORT_NUMBER = 3000;
    private static final String DITTO_THING_ID = "io.eclipseprojects.ditto:bulb-holder";
    private static final String BASE_URL = "http://localhost:" + DITTO_PORT_NUMBER +
        "/api/2/things/" + DITTO_THING_ID;

    private final DTKGEngine dtkgEngine;
    private final DTDManager dtdManager;
    private final WoDTWebServer woDTWebServer;
    private final PlatformManagementInterface platformManagementInterface;

    private DittoBase dittoBase;
    private List<ThingModelElement> propertiesList;
    private List<ThingModelElement> actionsList;
    private List<ThingModelElement> eventsList;

    /**
     * Default constructor.
    * @param digitalAdapterId the id of the Digital Adapter
    * @param configuration the configuration of the Digital Adapter
    */
    public WoDTDigitalAdapter(
        final String digitalAdapterId,
        final WoDTDigitalAdapterConfiguration configuration
    ) {
        this.dittoBase = new DittoBase();
        Thing thing = dittoBase.getClient().twin()
            .forId(ThingId.of(DITTO_THING_ID))
            .retrieve()
            .toCompletableFuture()
            .join();

        List<List<ThingModelElement>> result = extractDataFromThing(thing);
        this.propertiesList = result.get(1);
        this.actionsList = result.get(2);
        this.eventsList = result.get(3);

        this.platformManagementInterface = new BasePlatformManagementInterface(
                configuration.getDigitalTwinUri());
        this.dtkgEngine = new JenaDTKGEngine(configuration.getDigitalTwinUri());
        this.dtdManager = new WoTDTDManager(
                configuration.getDigitalTwinUri(),
                configuration.getOntology(),
                configuration.getPhysicalAssetId(),
                configuration.getPortNumber(),
                this.platformManagementInterface,
                result.get(0)
        );

        this.init(thing, configuration);

        this.woDTWebServer = new WoDTWebServerImpl(
                configuration.getPortNumber(),
                this.dtkgEngine,
                this.dtdManager,
                (actionName, body) -> {
                    try {
                        //publishDigitalActionWldtEvent(actionName, body); // TO DO: cambia qui, inviare msg in base all'actionName e body
                        return true;
                    } catch (Exception e) {
                        Logger.getLogger(WoDTDigitalAdapter.class.getName())
                                .log(Level.INFO, "Impossible to forward action: {0}", e);
                        return false;
                    }
                },
                this.platformManagementInterface
        );
        this.woDTWebServer.start();
        
        DittoThingListener dittoClientThread = new DittoThingListener(this);
        dittoClientThread.start();
    }

    private void init(Thing thing, WoDTDigitalAdapterConfiguration configuration) {
        // PROPERTIES (Thing Attributes)
        thing.getAttributes().ifPresent(attributes -> {
            attributes.forEach((attribute) -> {
                ThingModelElement prop = this.propertiesList.stream()
                    .filter(p -> p.getElement().equals(attribute.getKey().toString()))
                    .findFirst()
                    .orElse(null);
                if(prop != null) {
                    configuration.getOntology().convertPropertyValue(
                        attribute.getKey().toString(),
                        attribute.getValue().asString()
                    ).ifPresent(triple ->
                            this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                    );
                    this.dtdManager.addProperty(attribute.getKey().toString());
                }
            });
        });

        // PROPERTIES, ACTIONS, EVENTS (from Thing Features)
        thing.getFeatures().ifPresent(features -> {
            features.forEach((featureName) -> {
                featureName.getProperties().ifPresent(properties -> {
                    properties.forEach((property) -> {
                        ThingModelElement featureProp = this.propertiesList.stream()
                            .filter(p -> p.getElement().equals(property.getKey().toString()))
                            .filter(p -> p.getValue().get().equals(featureName.getId()))
                            .findFirst()
                            .orElse(null);
                        if(featureProp != null) {
                            configuration.getOntology().convertPropertyValue(
                                property.getKey().toString(),
                                property.getValue().toString()
                            ).ifPresent(triple ->
                                this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                            );
                            this.dtdManager.addProperty(property.getKey().toString());
                        }
                    });
                });
            });
        });

        // ACTIONS (Thing Actions)
        this.actionsList.stream()
            .filter(action -> action.getValue().isEmpty()) // Thing Action = no associated value
            .forEach(action -> {                
                this.dtdManager.addAction(action.getElement());
                this.dtkgEngine.addActionId(action.getElement());
            });

        // EVENTS (Thing Events)
        
    }

    public void onThingChange(ThingChange change) {
        // TO DO: implementa qui la logica per gestire i cambiamenti delle Thing
        System.out.println("me ne occup io fratema");
    }

    public void onMessage(RepliableMessage message) {
        // TO DO: implementa qui la logica per gestire i messaggi in arrivo
        System.out.println("me ne sto occupando io fratema");
    }
}