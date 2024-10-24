package org.eclipse.ditto.wodt.config;

import java.net.URI;
import java.util.Optional;

/**
 * This interface models the Configuration Loader for the Ditto WoDT Adapter.
 */
public interface ConfigurationLoader {
    /**
     * Get the ditto endpoint.
     * @return the endpoint.
     */
    URI getDittoEndpoint();

    /**
     * Get the ditto username.
     * @return the ditto username
     */
    String getDittoUsername();

    /**
     * Get the ditto password
     * @return the ditto password.
     */
    String getDittoPassword();

    /**
     * Get the Ditto Thing ID.
     * @return the Thing ID.
     */
    String getDittoThingId();

    /**
     * Get the path of the YAML file that specifies the DT semantics.
     * @return the file path
     */
    String getYamlOntologyPath();

    /**
     * Get the Platform URI to register.
     * @return the URI.
     */
    Optional<URI> getPlatformUri();

    /**
     * Get the associated Physical Asset ID.
     * @return the physical asset id
     */
    String getPhysicalAssetId();

    /**
     * Get the Digital Twin URI.
     * @return the DT URI
     */
    URI getDigitalTwinUri();

    /**
     * Get the port to expose for the Digital Twin WoDT services
     * @return the port to expose
     */
    int getDigitalTwinExposedPort();
}
