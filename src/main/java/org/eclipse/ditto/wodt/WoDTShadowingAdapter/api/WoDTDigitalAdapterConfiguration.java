package org.eclipse.ditto.wodt.WoDTShadowingAdapter.api;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.wodt.DTDManager.impl.OntologyManagerImpl;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl.WoDTDigitalAdapter;
import org.eclipse.ditto.wodt.common.DittoBase;

/**
 * Configuration for the {@link WoDTDigitalAdapter}.
*/
public final class WoDTDigitalAdapterConfiguration {
    private final URI dittoEndpoint;
    private final String dittoUsername;
    private final String dittoPassword;
    private final URI digitalTwinUri;
    private final int portNumber;
    private final String physicalAssetId;
    private final Set<URI> platformToRegister;
    private final OntologyManagerImpl ontologyManager;
    private final Thing thing;

    public WoDTDigitalAdapterConfiguration(
        final URI dittoEndpoint,
        final String dittoUsername,
        final String dittoPassword,
        final String thingId,
        final String yamlOntologyPath,
        final String physicalAssetId,
        final Set<URI> platformToRegister,
        final URI digitalTwinUri,
        final int digitalTwinExposedPort
    ) {
        this.dittoEndpoint = dittoEndpoint;
        this.dittoUsername = dittoUsername;
        this.dittoPassword = dittoPassword;
        this.thing = this.obtainDittoThing(thingId);
        this.ontologyManager = new OntologyManagerImpl(this.thing, yamlOntologyPath);
        this.digitalTwinUri = digitalTwinUri;
        this.portNumber = digitalTwinExposedPort;
        this.physicalAssetId = physicalAssetId;
        this.platformToRegister = new HashSet<>(platformToRegister);
    }

    private Thing obtainDittoThing(String dittoThingId) {
        return new DittoBase(this.dittoEndpoint, this.dittoUsername, this.dittoPassword).getClient().twin()
            .forId(ThingId.of(dittoThingId))
            .retrieve()
            .toCompletableFuture()
            .join();
    }

    /**
     * Get ditto endpoint.
     * @return the ditto endpoint uri
     */
    public URI getDittoEndpoint() {return this.dittoEndpoint; }

    /**
     * Get ditto username.
     * @return the username
     */
    public String getDittoUsername() {return this.dittoUsername;}

    /**
     * Get ditto password.
     * @return the password
     */
    public String getDittoPassword() {return this.dittoPassword;}

    /*
     * Return the Ditto Thing associated with the Digital Twin.
     */
    public Thing getDittoThing() {
        return this.thing;
    }

    /**
     * Obtain the WoDT Digital Twin URI.
    * @return the URI.
    */
    public URI getDigitalTwinUri() {
        return this.digitalTwinUri;
    }

    /**
     * Obtain the ontology to describe the Digital Twin data.
    * @return the ontology.
    */
    public OntologyManagerImpl getOntology() {
        return this.ontologyManager;
    }

    /**
     * Obtain the port number where to expose services.
    * @return the port number
    */
    public int getPortNumber() {
        return this.portNumber;
    }

    /**
     * Obtain the associated physical asset id.
    * @return the id of the associated physical asset
    */
    public String getPhysicalAssetId() {
        return this.physicalAssetId;
    }

    /**
     * Obtain the platform to which register.
    * @return the platforms urls.
    */
    public Set<URI> getPlatformToRegister() {
        return new HashSet<>(this.platformToRegister);
    }
}