package org.eclipse.ditto.wodt.DTDManager.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.wodt.DTDManager.api.DTDManager;
import org.eclipse.ditto.wodt.PlatformManagementInterface.api.PlatformManagementInterfaceReader;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.api.WoDTDigitalAdapterConfiguration;
import org.eclipse.ditto.wodt.common.ThingModelElement;
import org.eclipse.ditto.wodt.model.ontology.DTOntology;
import org.eclipse.ditto.wodt.model.ontology.WoDTVocabulary;

import org.eclipse.ditto.wot.model.Action;
import org.eclipse.ditto.wot.model.ActionFormElement;
import org.eclipse.ditto.wot.model.ActionForms;
import org.eclipse.ditto.wot.model.Actions;
import org.eclipse.ditto.wot.model.AtContext;
import org.eclipse.ditto.wot.model.AtType;
import org.eclipse.ditto.wot.model.BaseLink;
import org.eclipse.ditto.wot.model.Event;
import org.eclipse.ditto.wot.model.EventFormElement;
import org.eclipse.ditto.wot.model.EventForms;
import org.eclipse.ditto.wot.model.IRI;
import org.eclipse.ditto.wot.model.Link;
import org.eclipse.ditto.wot.model.Properties;
import org.eclipse.ditto.wot.model.Property;
import org.eclipse.ditto.wot.model.PropertyFormElement;
import org.eclipse.ditto.wot.model.PropertyForms;
import org.eclipse.ditto.wot.model.RootFormElement;
import org.eclipse.ditto.wot.model.Security;
import org.eclipse.ditto.wot.model.SecurityDefinitions;
import org.eclipse.ditto.wot.model.SecurityScheme;
import org.eclipse.ditto.wot.model.SingleActionFormElementOp;
import org.eclipse.ditto.wot.model.SingleAtContext;
import org.eclipse.ditto.wot.model.SingleEventFormElementOp;
import org.eclipse.ditto.wot.model.SinglePropertyFormElementOp;
import org.eclipse.ditto.wot.model.SingleRootFormElementOp;
import org.eclipse.ditto.wot.model.SingleUriAtContext;
import org.eclipse.ditto.wot.model.ThingDescription;
import org.eclipse.ditto.wot.model.Version;

/**
 * This class provide an implementation of the {@link DTDManager} using
* a WoT Thing Description to implement the Digital Twin Description.
*/
public class WoTDTDManager implements DTDManager {
    private static final String ATTRIBUTE_URL = "/attributes/{attributePath}";
    private static final String FEATURE_URL = "/features/{featureId}";
    private static final String PROPERTY_URL = FEATURE_URL + "/properties/{propertyPath}";
    private static final String ACTION_URL = "/inbox/messages/";
    private static final String EVENT_URL = "/outbox/messages/";
    private static final String MODEL_VERSION = "1.0.0";
    private static final String AVAILABLE_ACTIONS_PROPERTY = "availableActions";
    private static final String THING_MODEL_URL = "https://raw.githubusercontent.com/Web-of-Digital-Twins/"
            + "dtd-conceptual-model/refs/heads/main/implementations/wot/dtd-thing-model.tm.jsonld";

    private final String dittoBaseUrl;
    private final URI digitalTwinUri;
    private final String physicalAssetId;
    private final DTOntology ontology;
    private final int portNumber;
    private final String dtVersion;
    private final String dittoThingId;
    private final PlatformManagementInterfaceReader platformManagementInterfaceReader;
    private final WoDTDigitalAdapterConfiguration configuration;

    private final Map<String, Property> properties;
    private final Map<String, Property> relationships;
    private final Map<String, Action> actions;
    private final Map<String, Event> events;

    public WoTDTDManager(
        final WoDTDigitalAdapterConfiguration configuration,
        final PlatformManagementInterfaceReader platformManagementInterfaceReader
    ) {
        this.configuration = configuration;
        this.dittoBaseUrl = this.configuration.getDittoUrl().resolve("/api/2/things/").toString();
        this.dittoThingId = configuration.getDittoThing().getEntityId().get().toString();
        this.digitalTwinUri = configuration.getDigitalTwinUri();
        this.ontology = configuration.getOntology();
        this.physicalAssetId = configuration.getPhysicalAssetId();
        this.portNumber = configuration.getPortNumber();
        this.dtVersion = configuration.getDigitalTwinVersion();
        this.platformManagementInterfaceReader = platformManagementInterfaceReader;
        this.properties = new HashMap<>();
        this.relationships = new HashMap<>();
        this.actions = new HashMap<>();
        this.events = new HashMap<>();
    }

    @Override
    public void addProperty(final String rawPropertyName) {
        this.createThingDescriptionProperty(rawPropertyName, true)
                .ifPresent(property -> this.properties.put(rawPropertyName, property));
    }

    @Override
    public boolean removeProperty(final String rawPropertyName) {
        return this.properties.remove(rawPropertyName) != null;
    }

    @Override
    public void addRelationship(final String rawRelationshipName) {
        this.createThingDescriptionProperty(rawRelationshipName, false)
                .ifPresent(relationship -> this.relationships.put(rawRelationshipName, relationship));
    }

    @Override
    public boolean removeRelationship(final String rawRelationshipName) {
        return this.relationships.remove(rawRelationshipName) != null;
    }

    @Override
    public void addAction(final String rawActionName) {
        this.createThingDescriptionAction(rawActionName)
            .ifPresent(action -> this.actions.put(rawActionName, action));
    }

    @Override
    public boolean removeAction(final String rawActionName) {
        return this.actions.remove(rawActionName) != null;
    }

    @Override
    public void addEvent(String rawEventName) {
        this.createThingDescriptionEvent(rawEventName).ifPresent(event -> this.events.put(rawEventName, event));
    }

    @Override
    public boolean removeEvent(String rawEventName) {
        return this.events.remove(rawEventName) != null;
    }

    @Override
    public ThingDescription getDTD() {
        final Map<String, Property> dtdProperties = new HashMap<>(this.properties);
        dtdProperties.putAll(this.relationships);
        if (!actions.isEmpty()) {
            dtdProperties.put(AVAILABLE_ACTIONS_PROPERTY, Property.newBuilder(AVAILABLE_ACTIONS_PROPERTY)
                    .setAtType(AtType.newSingleAtType(WoDTVocabulary.AVAILABLE_ACTIONS.getUri()))
                    .setReadOnly(true)
                    .build());
        }

        final List<BaseLink<?>> links = this.platformManagementInterfaceReader
                .getRegisteredPlatformUrls()
                .stream().map(uri -> BaseLink.newLinkBuilder()
                        .setHref(IRI.of(uri.toString()))
                        .setRel(WoDTVocabulary.REGISTERED_TO_PLATFORM.getUri())
                        .build())
                .collect(Collectors.toList());
        links.add(Link.newBuilder()
                .setHref(IRI.of(THING_MODEL_URL))
                .setRel("type")
                .setType("application/tm+json")
                .build());
        links.add(Link.newBuilder()
                .setHref(IRI.of(this.digitalTwinUri.resolve("/dtkg").toString()))
                .setRel(WoDTVocabulary.DTKG.getUri())
                .build());

        final List<SingleAtContext> contexts = new ArrayList<>(List.of(SingleUriAtContext.W3ORG_2022_WOT_TD_V11));
        contexts.addAll(this.configuration.getOntology().getAvailableContextExtensions()
                .stream()
                .map(context ->
                        AtContext.newSinglePrefixedAtContext(
                                context.getField(),
                                AtContext.newSingleUriAtContext(context.getFeature().get()))).toList());

        return ThingDescription.newBuilder()
                .setAtContext(AtContext.newMultipleAtContext(contexts))
                .setId(IRI.of(this.digitalTwinUri.toString()))
                .setAtType(AtType.newSingleAtType(this.ontology.getDigitalTwinType()))
                .setVersion(Version.newBuilder()
                        .setInstance(this.dtVersion)
                        .setModel(MODEL_VERSION)
                        .build())
                .set(WoDTVocabulary.PHYSICAL_ASSET_ID.getUri(), this.physicalAssetId)
                .setSecurityDefinitions(SecurityDefinitions.of(Map.of("basic_sc",
                        SecurityScheme.newBasicSecurityBuilder("header").build())))
                .setSecurity(Security.newSingleSecurity("basic_sc"))
                .setProperties(Properties.from(dtdProperties.values()))
                .setActions(Actions.from(this.actions.values()))
                .setForms(List.of(RootFormElement.newBuilder()
                        .setHref(IRI.of(
                                URI.create(this.digitalTwinUri.toString().replaceFirst("([a-zA-Z][a-zA-Z0-9+.-]*):", "ws:"))
                                        .resolve("/dtkg")
                                        .toString()
                        ))
                        .setSubprotocol("websocket")
                        .setOp(SingleRootFormElementOp.OBSERVEALLPROPERTIES)
                        .build()))
                .setLinks(links)
                .build();
    }

    private Optional<Property> createThingDescriptionProperty(
            final String rawPropertyName,
            final boolean indicateAugmentation
    ) {
        final Optional<String> domainTag = this.ontology.getDomainTag(rawPropertyName);

        if (domainTag.isPresent()) {
            final JsonObjectBuilder metadata = JsonObject.newBuilder()
                    .set(WoDTVocabulary.DOMAIN_TAG.getUri(), domainTag.get());
            if (indicateAugmentation) {
                metadata.set(WoDTVocabulary.AUGMENTED_INTERACTION.getUri(), false);
            }
            return Optional.of(Property.newBuilder(rawPropertyName, metadata.build())
                    .setReadOnly(true)
                    .setForms(PropertyForms.of(List.of(
                                    PropertyFormElement.newBuilder()
                                        .setHref(IRI.of(getPropertyAffordance(rawPropertyName).toString()))
                                        .setOp(SinglePropertyFormElementOp.READPROPERTY)
                                        .build(),
                                    PropertyFormElement.newBuilder()
                                        .setHref(IRI.of(getPropertyAffordance(rawPropertyName).toString()))
                                        .setOp(SinglePropertyFormElementOp.OBSERVEPROPERTY)
                                        .setSubprotocol("sse")
                                        .build()
                                )
                            )
                    )
                    .build());
        }
        return Optional.empty();
    }

    private Optional<Action> createThingDescriptionAction(final String rawActionName) {
        return this.ontology.getDomainTag(rawActionName).map(actionDomainTag -> Action.newBuilder(rawActionName)
                .set(WoDTVocabulary.DOMAIN_TAG.getUri(), actionDomainTag)
                .set(WoDTVocabulary.AUGMENTED_INTERACTION.getUri(), false)
                .setForms(ActionForms.of(List.of(ActionFormElement.newBuilder()
                        .setHref(IRI.of(getActionAffordances(rawActionName)))
                        .setOp(SingleActionFormElementOp.INVOKEACTION)
                        .build())))
                .build());
    }

    private Optional<Event> createThingDescriptionEvent(final String rawEventName) {
        return this.ontology.obtainEventType(rawEventName).map(eventDomainTag -> Event.newBuilder(rawEventName)
                .set(WoDTVocabulary.DOMAIN_TAG.getUri(), eventDomainTag)
                .setForms(EventForms.of(List.of(EventFormElement.newBuilder()
                        .setHref(IRI.of(getEventAffordances(rawEventName)))
                        .setOp(SingleEventFormElementOp.SUBSCRIBEEVENT)
                        .setSubprotocol("sse")
                        .build())))
                .build());
    }

    private URI getPropertyAffordance(String rawName) {
        return URI.create(rawName.contains("rel-") ? getRelAffordances(rawName) : getStandardPropertyAffordances(rawName));
    }

    private String getStandardPropertyAffordances(String name) {
        String[] splitName = splitStringAtFirstCharOccurrence(name, '_');
        ThingModelElement prop = findThingModelElement(this.configuration.getOntology().getAvailableProperties(), name, splitName);
        String href = dittoBaseUrl + this.dittoThingId;
        if (prop.getFeature().isPresent()) {
            href += PROPERTY_URL.replace("{featureId}", prop.getFeature().get())
                    .replace("{propertyPath}", splitName[1].replace("_", "/"));
        } else {
            href += ATTRIBUTE_URL.replace("{attributePath}", name);
        }
        return href;
    }

    private String getRelAffordances(String name) {
        return dittoBaseUrl + this.dittoThingId + ATTRIBUTE_URL.replace("{attributePath}", name);
    }

    private String getActionAffordances(String name) {
        String[] splitName = splitStringAtFirstCharOccurrence(name, '_');
        ThingModelElement act = findThingModelElement(this.configuration.getOntology().getAvailableActions(), name, splitName);
        String href = dittoBaseUrl + this.dittoThingId;
        if (act.getFeature().isPresent()) {
            href += FEATURE_URL.replace("{featureId}", act.getFeature().get()) + ACTION_URL + splitName[1];
        } else {
            href += ACTION_URL + name;
        }
        return href;
    }

    private String getEventAffordances(String name) {
        String[] splitName = splitStringAtFirstCharOccurrence(name, '_');
        ThingModelElement evt = findThingModelElement(this.configuration.getOntology().getAvailableEvents(), name, splitName);
        String href = dittoBaseUrl + this.dittoThingId;
        if (evt.getFeature().isPresent()) {
            href += FEATURE_URL.replace("{featureId}", evt.getFeature().get()) + EVENT_URL + splitName[1];
        } else {
            href += EVENT_URL + name;
        }
        return href;
    }

    private ThingModelElement findThingModelElement(List<ThingModelElement> list, String name, String[] splitName) {
        return list.stream()
                .filter(p -> splitName != null
                        ? p.getField().equals(splitName[1]) && p.getFeature().get().equals(splitName[0])
                        : p.getField().equals(name))
                .findFirst()
                .orElse(null);
    }

    private String[] splitStringAtFirstCharOccurrence(String input, char character) {
        int underscoreIndex = input.indexOf(character);
        if (underscoreIndex != -1) {
            String firstPart = input.substring(0, underscoreIndex);
            String secondPart = input.substring(underscoreIndex + 1);
            return new String[]{firstPart, secondPart};
        } else {
            return null;
        }
    }
}