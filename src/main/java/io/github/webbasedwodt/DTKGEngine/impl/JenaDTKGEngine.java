package io.github.webbasedwodt.DTKGEngine.impl;

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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.RDF;
import io.github.webbasedwodt.DTKGEngine.api.DTKGEngine;
import io.github.webbasedwodt.DTKGEngine.api.DTKGObserver;
import io.github.webbasedwodt.ontology.BlankNode;
import io.github.webbasedwodt.ontology.DTOntology;
import io.github.webbasedwodt.ontology.Individual;
import io.github.webbasedwodt.ontology.Literal;
import io.github.webbasedwodt.ontology.Node;
import io.github.webbasedwodt.ontology.RdfProperty;
import io.github.webbasedwodt.ontology.WoDTVocabulary;

/**
 * This class provides an implementation of the {@link DTKGEngine} using
* Apache Jena.
*/
public class JenaDTKGEngine implements DTKGEngine {
    private final Model dtkgModel;
    private final Resource digitalTwinResource;
    private final List<DTKGObserver> observers;

    /**
     * Default constructor.
    * @param digitalTwinUri the uri of the Digital Twin for which this class creates the DTKG
    * @param digitalTwinType the type of the Digital Twin
    */
    public JenaDTKGEngine(final URI digitalTwinUri, final String digitalTwinType) {
        this.dtkgModel = ModelFactory.createDefaultModel();
        this.digitalTwinResource = this.dtkgModel.createResource(digitalTwinUri.toString());
        this.digitalTwinResource.addProperty(RDF.type, this.dtkgModel.createResource(digitalTwinType));
        this.observers = new ArrayList<>();
    }

    @Override
    public void removeDigitalTwin() {
        this.writeModel(Model::removeAll);
        this.notifyObservers();
    }

    @Override
    public void addDigitalTwinPropertyUpdate(final RdfProperty property, final Node newValue) {
        if (property.getUri().isPresent()) {
            this.writeModel(model -> {
                this.digitalTwinResource.removeAll(model.getProperty(property.getUri().get()));
                addProperty(this.digitalTwinResource, Pair.of(property, newValue));
            });
            this.notifyObservers();
        }
    }

    @Override
    public boolean removeProperty(final RdfProperty property) {
        if (property.getUri().isPresent()
                && this.digitalTwinResource.hasProperty(this.dtkgModel.getProperty(property.getUri().get()))) {
            this.writeModel(model ->
                this.digitalTwinResource.removeAll(model.getProperty(property.getUri().get()))
            );
            this.notifyObservers();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void addRelationship(final RdfProperty relationshipPredicate, final Individual targetIndividual) {
        if (relationshipPredicate.getUri().isPresent()) {
            this.writeModel(model ->
                    addProperty(this.digitalTwinResource, Pair.of(relationshipPredicate, targetIndividual))
            );
            this.notifyObservers();
        }
    }

    @Override
    public boolean removeRelationship(final RdfProperty relationshipPredicate, final Individual targetIndividual) {
        if (relationshipPredicate.getUri().isPresent()
                && targetIndividual.getUri().isPresent()
                && this.digitalTwinResource.hasProperty(this.dtkgModel.getProperty(relationshipPredicate.getUri().get()))) {
            this.writeModel(model ->
                    model.remove(
                            this.digitalTwinResource,
                            this.dtkgModel.getProperty(relationshipPredicate.getUri().get()),
                            model.getResource(targetIndividual.getUri().get())
                    )
            );
            this.notifyObservers();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeRelationship(RdfProperty relationshipPredicate) {
        if (relationshipPredicate.getUri().isPresent()
                && this.digitalTwinResource.hasProperty(this.dtkgModel.getProperty(relationshipPredicate.getUri().get()))) {
            this.writeModel(model ->
                    this.digitalTwinResource.removeAll(this.dtkgModel.getProperty(relationshipPredicate.getUri().get()))
            );
            this.notifyObservers();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void addActionId(final String actionId) {
        this.writeModel(model ->
                this.digitalTwinResource.addLiteral(
                        this.dtkgModel.createProperty(WoDTVocabulary.AVAILABLE_ACTION_ID.getUri()),
                        actionId
                )
        );
        this.notifyObservers();
    }

    @Override
    public boolean removeActionId(final String actionId) {
        if (this.dtkgModel.containsLiteral(
                this.digitalTwinResource,
                this.dtkgModel.getProperty(WoDTVocabulary.AVAILABLE_ACTION_ID.getUri()),
                actionId)
        ) {
            this.writeModel(model ->
                model.remove(
                    this.digitalTwinResource,
                    model.getProperty(WoDTVocabulary.AVAILABLE_ACTION_ID.getUri()),
                    model.createTypedLiteral(actionId)
                )
            );
            return true;
        }
        return false;
    }

    @Override
    public String getCurrentDigitalTwinKnowledgeGraph() {
        try {
            this.dtkgModel.enterCriticalSection(Lock.READ);
            return RDFWriter.create().lang(Lang.TTL).source(this.dtkgModel).asString();
        } finally {
            this.dtkgModel.leaveCriticalSection();
        }
    }

    @Override
    public void addDTKGObserver(final DTKGObserver observer) {
        this.observers.add(observer);
    }

    private void notifyObservers() {
        final String currentDTKG = this.getCurrentDigitalTwinKnowledgeGraph();
        this.observers.forEach(observer -> observer.notifyNewDTKG(currentDTKG));
    }

    private void addProperty(final Resource resourceToAdd, final Pair<RdfProperty, Node> predicate) {
        final String propertyUri = predicate.getLeft().getUri().orElse("");
        final Property property = this.dtkgModel.createProperty(propertyUri);
        if (predicate.getRight() instanceof RdfProperty) {
            resourceToAdd.addProperty(
                    property,
                    dtkgModel.createProperty(((RdfProperty) predicate.getRight()).getUri().orElse(""))
            );
        } else if (predicate.getRight() instanceof BlankNode) {
            resourceToAdd.addProperty(
                    property,
                    addProperties(this.dtkgModel.createResource(), ((BlankNode) predicate.getRight()).getPredicates())
            );
        } else if (predicate.getRight() instanceof Literal<?>) {
            resourceToAdd.addLiteral(property, ((Literal<?>) predicate.getRight()).getValue());
        } else if (predicate.getRight() instanceof Individual) {
            resourceToAdd.addProperty(
                    property,
                    this.dtkgModel.createResource(((Individual) predicate.getRight()).getUri().orElse(""))
            );
        }
    }

    private Resource addProperties(final Resource resourceToAdd, final List<Pair<RdfProperty, Node>> predicates) {
        predicates.forEach(predicate -> addProperty(resourceToAdd, predicate));
        return resourceToAdd;
    }

    private void writeModel(final Consumer<Model> modelConsumer) {
        this.dtkgModel.enterCriticalSection(Lock.WRITE);
        modelConsumer.accept(this.dtkgModel);
        this.dtkgModel.leaveCriticalSection();
    }
}
