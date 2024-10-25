/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.wodt.common;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.DisconnectedDittoClient;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.live.internal.MessageSerializerFactory;
import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.eclipse.ditto.client.live.messages.MessageSerializers;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.AuthenticationProviders;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.wodt.common.model.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.WebSocket;

/**
 * Reads configuration properties and instantiates {@link org.eclipse.ditto.client.DittoClient}s.
 */
public class DittoBase {
    private static final int TIMEOUT = 10;
    private final DittoClient client;

    public DittoBase(final URI dittoEndpoint, final String dittoUsername, final String dittoPassword) {
        try {
            client = buildClient(dittoEndpoint, dittoUsername, dittoPassword)
                    .connect().toCompletableFuture().get(TIMEOUT, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public DittoClient getClient() {
        return this.client;
    }

    private DisconnectedDittoClient buildClient(final URI dittoEndpoint, final String dittoUsername, final String dittoPassword) {
        final AuthenticationProvider<WebSocket> authenticationProvider =
                AuthenticationProviders.basic(BasicAuthenticationConfiguration.newBuilder()
                        .username(dittoUsername)
                        .password(dittoPassword)
                        .build());

        final MessagingConfiguration.Builder messagingConfigurationBuilder =
                WebSocketMessagingConfiguration.newBuilder()
                        .jsonSchemaVersion(JsonSchemaVersion.V_2)
                        .reconnectEnabled(false)
                        .endpoint(dittoEndpoint.toString());

        final MessagingProvider messagingProvider =
                MessagingProviders.webSocket(messagingConfigurationBuilder.build(), authenticationProvider);

        return DittoClients.newInstance(messagingProvider, messagingProvider, messagingProvider, buildMessageSerializerRegistry());
    }

    private MessageSerializerRegistry buildMessageSerializerRegistry() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final MessageSerializerRegistry messageSerializerRegistry =
                MessageSerializerFactory.initializeDefaultSerializerRegistry();
        messageSerializerRegistry.registerMessageSerializer(
                MessageSerializers.of(User.USER_CUSTOM_CONTENT_TYPE, User.class, "*",
                        (exampleUser, charset) -> {
                            try {
                                return ByteBuffer.wrap(objectMapper.writeValueAsBytes(exampleUser));
                            } catch (final JsonProcessingException e) {
                                throw new IllegalStateException("Could not serialize", e);
                            }
                        }, (byteBuffer, charset) -> {
                            try {
                                return objectMapper.readValue(byteBuffer.array(), User.class);
                            } catch (IOException e) {
                                throw new IllegalStateException("Could not deserialize", e);
                            }
                        }));

        return messageSerializerRegistry;
    }

    /**
     * Destroys the client and waits for its graceful shutdown.
     */
    public void terminate() {
        client.destroy();
    }
}
