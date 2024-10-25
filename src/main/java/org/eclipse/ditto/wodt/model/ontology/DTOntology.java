/*
 * Copyright (c) 2023. Andrea Giulianelli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.ditto.wodt.model.ontology;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

/**
 * It models the ontology followed by the Digital Twin.
 * This will be used to convert raw data to semantic data, following the domain ontology.
 * This interface is the one that DT Developer must implement.
 */
public interface DTOntology {
    /**
     * This represents the type of the Digital Twin.
     * @return the type of the Digital Twin
     */
    String getDigitalTwinType();

    /**
     * Get the Domain Tag of a Digital Twin property, relationship or action.
     * If the property/relationship/action is not present, returns an empty {@link Optional}
     * @param rawElement the Digital Twin property/relationship/action name for which obtain the corresponding Domain Tag
     * @return the Domain Tag of the Digital Twin property/relationship/action
     */
    Optional<String> getDomainTag(String rawElement);

    /**
     * Convert a raw property and its value to the ontology model.
     * If the mapping cannot be done it will return an empty optional
     * @param rawProperty the input raw property
     * @param value the value of the property
     * @return an optional that is filled with the Pair of the mapped Property and its mapped value if possible
     * @param <T> the type of the value
     */
    <T> Optional<Pair<RdfProperty, Node>> mapPropertyData(String rawProperty, T value);

    /**
     * Convert a raw relationship and its target uri to the ontology model.
     * If the mapping cannot be done it will return an empty optional.
     * @param rawRelationship the input raw relationship
     * @param targetUri the target uri of the relationship
     * @return an optional that is filled with the Pair of the mapped Property and its mapped target uri if possible
     */
    Optional<Pair<RdfProperty, Individual>> mapRelationshipInstance(String rawRelationship, String targetUri);

    /*
     * Obtain the semantic type that describe the event.
     */
    Optional<String> obtainEventType(String rawEvent);
}
