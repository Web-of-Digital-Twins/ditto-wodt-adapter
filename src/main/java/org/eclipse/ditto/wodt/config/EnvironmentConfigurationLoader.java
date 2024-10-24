package org.eclipse.ditto.wodt.config;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The Configuration Loader implementation based on Environment Variables.
 */
public class EnvironmentConfigurationLoader implements ConfigurationLoader{
    private static final String DITTO_ENDPOINT_VARIABLE = "DITTO_ENDPOINT";
    private static final String DITTO_USERNAME_VARIABLE = "DITTO_USERNAME";
    private static final String DITTO_PASSWORD_VARIABLE = "DITTO_PASSWORD";
    private static final String DITTO_THING_ID_VARIABLE = "DITTO_THING_ID";
    private static final String YAML_ONTOLOGY_PATH_VARIABLE = "YAML_ONTOLOGY_PATH";
    private static final String PLATFORM_URI_VARIABLE = "PLATFORM_URI";
    private static final String PA_ID_VARIABLE = "PHYSICAL_ASSET_ID";
    private static final String DT_URI_VARIABLE = "DIGITAL_TWIN_URI";
    private static final String DT_EXPOSED_PORT_VARIABLE = "DIGITAL_TWIN_EXPOSED_PORT";

    /**
     * Default constructor.
     */
    public EnvironmentConfigurationLoader() {
        final boolean isAMandatoryVariableMissing = Stream.of(DITTO_ENDPOINT_VARIABLE,
                DITTO_USERNAME_VARIABLE,
                DITTO_PASSWORD_VARIABLE,
                DITTO_THING_ID_VARIABLE,
                YAML_ONTOLOGY_PATH_VARIABLE,
                PA_ID_VARIABLE,
                DT_URI_VARIABLE,
                DT_EXPOSED_PORT_VARIABLE).anyMatch(variable -> System.getenv(variable) == null);

        if (isAMandatoryVariableMissing) {
            throw new IllegalStateException("Please specify all the mandatory environment variables");
        }
    }

    @Override
    public URI getDittoEndpoint() {
        return URI.create(System.getenv(DITTO_ENDPOINT_VARIABLE));
    }

    @Override
    public String getDittoUsername() {
        return System.getenv(DITTO_USERNAME_VARIABLE);
    }

    @Override
    public String getDittoPassword() {
        return System.getenv(DITTO_PASSWORD_VARIABLE);
    }

    @Override
    public String getDittoThingId() {
        return System.getenv(DITTO_THING_ID_VARIABLE);
    }

    @Override
    public String getYamlOntologyPath() {
        return System.getenv(YAML_ONTOLOGY_PATH_VARIABLE);
    }

    @Override
    public Optional<URI> getPlatformUri() {
        return Optional.ofNullable(System.getenv(PLATFORM_URI_VARIABLE)).map(URI::create);
    }

    @Override
    public String getPhysicalAssetId() {
        return System.getenv(PA_ID_VARIABLE);
    }

    @Override
    public URI getDigitalTwinUri() {
        return URI.create(System.getenv(DT_URI_VARIABLE));
    }

    @Override
    public int getDigitalTwinExposedPort() {
        return Integer.parseInt(System.getenv(DT_EXPOSED_PORT_VARIABLE));
    }
}
