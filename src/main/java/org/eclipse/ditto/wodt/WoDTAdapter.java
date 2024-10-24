package org.eclipse.ditto.wodt;

import java.util.Set;

import org.eclipse.ditto.wodt.WoDTShadowingAdapter.api.WoDTDigitalAdapterConfiguration;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl.WoDTDigitalAdapter;
import org.eclipse.ditto.wodt.config.ConfigurationLoader;
import org.eclipse.ditto.wodt.config.EnvironmentConfigurationLoader;

/*
 * Application entry point.
 */
public class WoDTAdapter {
    public static void main(String[] args) {
        final ConfigurationLoader configurationLoader = new EnvironmentConfigurationLoader();

        new WoDTDigitalAdapter(
            new WoDTDigitalAdapterConfiguration(
                configurationLoader.getDittoEndpoint(),
                configurationLoader.getDittoUsername(),
                configurationLoader.getDittoPassword(),
                configurationLoader.getDittoThingId(),
                configurationLoader.getYamlOntologyPath(),
                configurationLoader.getPhysicalAssetId(),
                configurationLoader.getPlatformUri().map(Set::of).orElseGet(Set::of),
                configurationLoader.getDigitalTwinUri(),
                configurationLoader.getDigitalTwinExposedPort()
            )
        );
    }    
}
