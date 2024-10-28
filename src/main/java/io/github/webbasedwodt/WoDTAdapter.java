package io.github.webbasedwodt;

import java.util.Set;

import io.github.webbasedwodt.WoDTShadowingAdapter.api.WoDTDigitalAdapterConfiguration;
import io.github.webbasedwodt.WoDTShadowingAdapter.impl.WoDTDigitalAdapter;
import io.github.webbasedwodt.config.ConfigurationLoader;
import io.github.webbasedwodt.config.EnvironmentConfigurationLoader;

/*
 * Application entry point.
 */
public class WoDTAdapter {
    public static void main(String[] args) {
        final ConfigurationLoader configurationLoader = new EnvironmentConfigurationLoader();

        new WoDTDigitalAdapter(
            new WoDTDigitalAdapterConfiguration(
                configurationLoader.getDittoUrl(),
                configurationLoader.getDittoObservationEndpoint(),
                configurationLoader.getDittoUsername(),
                configurationLoader.getDittoPassword(),
                configurationLoader.getDittoThingId(),
                configurationLoader.getYamlOntologyPath(),
                configurationLoader.getPhysicalAssetId(),
                configurationLoader.getPlatformUri().map(Set::of).orElseGet(Set::of),
                configurationLoader.getDigitalTwinUri(),
                configurationLoader.getDigitalTwinExposedPort(),
                configurationLoader.getDigitalTwinVersion()
            )
        );
    }    
}
