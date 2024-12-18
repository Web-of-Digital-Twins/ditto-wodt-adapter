package io.github.webbasedwodt.WoDTShadowingAdapter.impl;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

import io.github.webbasedwodt.common.DittoBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class handle a Ditto Client that listen to Thing changes and messages.
 */
public class DittoThingListener extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(DittoThingListener.class);
    
    private final CountDownLatch latch = new CountDownLatch(1);
    private final DittoBase client;
    private final WoDTDigitalAdapter woDTDigitalAdapter;

    public DittoThingListener(final URI dittoEndpoint,
                              final String dittoUsername,
                              final String dittoPassword,
                              WoDTDigitalAdapter woDTDigitalAdapter
    ) {
        super();
        this.woDTDigitalAdapter = woDTDigitalAdapter;
        this.client = new DittoBase(dittoEndpoint, dittoUsername, dittoPassword);
    }

    @Override
    public void run() {
        try {
            client.getClient().twin().startConsumption().thenAccept(v -> {
                LOGGER.info("Subscribed for Ditto Thing changes");
                client.getClient().twin().registerForThingChanges("my-changes", change -> {
                    LOGGER.info("Received Thing element " + change.getAction() + " change");
                    this.woDTDigitalAdapter.onThingChange(change);
                });
            });
            
            latch.await();
        } catch (InterruptedException e) {
            LOGGER.error("Error in DittoThingListener", e);
        } finally {
            client.getClient().destroy();
            woDTDigitalAdapter.stopAdapter();
        }
    }
    
    public void stopThread() {
        latch.countDown();
    }
}
