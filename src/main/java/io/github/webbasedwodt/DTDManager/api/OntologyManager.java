package io.github.webbasedwodt.DTDManager.api;

import java.util.List;

import io.github.webbasedwodt.common.ThingModelElement;

/**
 * Get the available properties, relationships, actions, and events of the digital twin.
 * These data are obtained combining the information from the ThingModel and the Ontology mapping,
 * prioritizing the Ontology mapping.
 */
public interface OntologyManager {

    /**
     * Get the available context extensions of the digital twin.
     * @return the available context extensions.
     */
    List<ThingModelElement> getAvailableContextExtensions();

    /**
     * Get the available properties.
     * @return the available properties
     */
    List<ThingModelElement> getAvailableProperties();
    
    /**
     * Get the available relationships of the digital twin.
     * @return the available relationships
     */
    List<ThingModelElement> getAvailableRelationships();
    
    /**
     * Get the available actions of the digital twin.
     * @return the available actions
     */
    List<ThingModelElement> getAvailableActions();

    /**
     * Get the available events of the digital twin.
     * @return the available events
     */
    List<ThingModelElement> getAvailableEvents();
}
