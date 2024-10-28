package io.github.webbasedwodt.DTDManager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.ditto.things.model.Thing;
import io.github.webbasedwodt.DTDManager.api.OntologyManager;
import io.github.webbasedwodt.common.ThingModelElement;
import io.github.webbasedwodt.common.ThingModelUtils;
import io.github.webbasedwodt.ontology.DTOntology;
import io.github.webbasedwodt.ontology.Individual;
import io.github.webbasedwodt.ontology.Literal;
import io.github.webbasedwodt.ontology.Node;
import io.github.webbasedwodt.ontology.RdfProperty;

public final class OntologyManagerImpl implements DTOntology, OntologyManager {
    
    private final ThingModelUtils thingModelUtils;
    private Optional<YamlOntologyProvider> yamlOntologyHandler = Optional.empty();

    public OntologyManagerImpl(
        Thing dittoThing,
        String yamlOntologyPath
    ) {
        this.thingModelUtils = new ThingModelUtils(dittoThing);
        if(!yamlOntologyPath.isEmpty()) {
            yamlOntologyHandler = Optional.of(new YamlOntologyProvider(yamlOntologyPath));
        }
    }

    @Override
    public String getDigitalTwinType() {
        return yamlOntologyHandler
            .flatMap(YamlOntologyProvider::getDigitalTwinType)
            .orElse(thingModelUtils.getDigitalTwinType()
            .orElse("UnknownDigitalTwinType"));
    }

    @Override
    public Optional<String> getDomainTag(String rawElement) {
        Map<String, String> domainTags = getMergedPropertiesAndRelationships();
        domainTags.putAll(getMergedActions());
        return Optional.ofNullable(domainTags.get(rawElement));
    }

    @Override
    public Optional<String> obtainEventType(String rawEvent) {
        Map<String, String> mergedEvents = getMergedEvents();
        return Optional.ofNullable(mergedEvents.get(rawEvent));
    }

    @Override
    public <T> Optional<Pair<RdfProperty, Node>> mapPropertyData(String rawProperty, T value) {
        Map<String, String> propertiesMap = getMergedPropertiesAndRelationships();
        return Optional.ofNullable(propertiesMap.get(rawProperty))
            .map(domainTag -> Pair.of(new RdfProperty(domainTag), new Literal<>(value)));
    }

    @Override
    public Optional<Pair<RdfProperty, Individual>> mapRelationshipInstance(String rawRelationship, String targetUri) {
        Map<String, String> relationshipsMap = getMergedPropertiesAndRelationships();
        return Optional.ofNullable(relationshipsMap.get(rawRelationship))
            .map(domainTag -> Pair.of(new RdfProperty(domainTag), new Individual(targetUri)));
    }
    
    private Map<String, String> getMergedPropertiesAndRelationships() {
        Map<String, String> mergedMap = new HashMap<>();
        thingModelUtils.getTMProperties().forEach(element -> {
            mergedMap.put(
                element.getField(),
                element.getDomainTag().orElse("")
            );
        });
        
        yamlOntologyHandler.ifPresent(handler ->
            handler.getProperties().stream().forEach(properties -> {
                properties.ifPresent(prop -> {
                    String name = prop.get("name");
                    String domainTag = prop.get("domainTag");
                    mergedMap.merge(name, domainTag, (existing, newValue) -> newValue);
                });
            })
        );
        return mergedMap;
    }
    
    private Map<String, String> getMergedActions() {
        Map<String, String> mergedActions = new HashMap<>();
        thingModelUtils.getTMActions().forEach(element -> {
            mergedActions.put(element.getField(), element.getDomainTag().orElse(""));
        });
        
        yamlOntologyHandler.ifPresent(handler ->
            handler.getActions().stream().forEach(actions -> {
                actions.ifPresent(action -> {
                    String name = action.get("name");
                    String type = action.get("domainTag");
                    mergedActions.merge(name, type, (existing, newValue) -> newValue);
                });
            })
        );
        return mergedActions;
    }
    
    private Map<String, String> getMergedEvents() {
        Map<String, String> mergedEvents = new HashMap<>();
        thingModelUtils.getTMEvents().forEach(element -> {
            mergedEvents.put(element.getField(), element.getDomainTag().orElse(""));
        });
        
        yamlOntologyHandler.ifPresent(handler ->
            handler.getEvents().stream().forEach(events -> {
                events.ifPresent(event -> {
                    String name = event.get("name");
                    String domainTag = event.get("domainTag");
                    mergedEvents.merge(name, domainTag, (existing, newValue) -> newValue);
                });
            })
        );
        return mergedEvents;
    }

    @Override
    public List<ThingModelElement> getAvailableContextExtensions() {
        return thingModelUtils.getTMContextExtensions();
    }

    @Override
    public List<ThingModelElement> getAvailableProperties() {
        Map<String, String> mergedPropertiesAndRelationships = getMergedPropertiesAndRelationships();
        List<ThingModelElement> propertiesList = new ArrayList<>();
        thingModelUtils.getTMProperties().forEach(element -> {
            String field = element.getField();
            String featureName = element.getFeature().orElse("");
            
            if (mergedPropertiesAndRelationships.containsKey(field)) {
                String domainTag = mergedPropertiesAndRelationships.get(field);
                propertiesList.add(new ThingModelElement(field, Optional.of(featureName), Optional.of(domainTag)));
            } else {
                propertiesList.add(element);
            }
        });

        return propertiesList;
    }

    @Override
    public List<ThingModelElement> getAvailableRelationships() {
        List<ThingModelElement> relationshipsList = new ArrayList<>();
        thingModelUtils.getTMProperties().forEach(element -> {
            String field = element.getField();
            if (field.startsWith("rel-")) {
                relationshipsList.add(element);
            }
        });

        yamlOntologyHandler.ifPresent(handler -> 
            handler.getProperties().stream().forEach(optionalMap -> {
                optionalMap.ifPresent(property -> {
                    String name = property.get("name");
                    if (name.startsWith("rel-")) {
                        String domainTag = property.get("domainTag");
                        relationshipsList.add(new ThingModelElement(name, Optional.empty(), Optional.ofNullable(domainTag)));
                    }
                });
            })
        );
        return relationshipsList;
    }

    @Override
    public List<ThingModelElement> getAvailableActions() {
        Map<String, String> mergedActions = getMergedActions();
        List<ThingModelElement> actionsList = new ArrayList<>();
        thingModelUtils.getTMActions().forEach(element -> {
            String field = element.getField();
            String featureName = element.getFeature().orElse("");
            
            if (mergedActions.containsKey(field)) {
                String domainTag = mergedActions.get(field);
                actionsList.add(new ThingModelElement(field, Optional.of(featureName),
                    Optional.of(domainTag)));
            } else {
                actionsList.add(element);
            }
        });

        return actionsList;
    }

    @Override
    public List<ThingModelElement> getAvailableEvents() {
        Map<String, String> mergedEvents = getMergedEvents();
        List<ThingModelElement> eventsList = new ArrayList<>();
        thingModelUtils.getTMEvents().forEach(element -> {
            String field = element.getField();
            String featureName = element.getFeature().orElse("");

            if (mergedEvents.containsKey(field)) {
                String domainTag = mergedEvents.get(field);
                eventsList.add(new ThingModelElement(field, Optional.of(featureName),
                    Optional.of(domainTag)));
            } else {
                eventsList.add(element);
            }
        });

        return eventsList;
    }
}
