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

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.base.model.auth.AuthorizationSubject;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.DisconnectedDittoClient;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.ClientCredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.ProxyConfiguration;
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
public abstract class DittoBase {

    private static final ConfigProperties CONFIG_PROPERTIES = ConfigProperties.getInstance();
    protected final DittoClient client;
    protected AuthorizationSubject authorizationSubject;

    protected DittoBase() {
        try {
            client = buildClient().connect().toCompletableFuture().get(10, TimeUnit.SECONDS); // TO DO: cambia
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    protected void startConsumeChanges(final DittoClient client) {
        try {
            client.twin().startConsumption().toCompletableFuture().get(10, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Error subscribing to change events.", e);
        }
    }

    private DisconnectedDittoClient buildClient() {
        final AuthenticationProvider<WebSocket> authenticationProvider = buildAuthenticationProvider();

        final MessagingConfiguration.Builder messagingConfigurationBuilder =
                WebSocketMessagingConfiguration.newBuilder()
                        .jsonSchemaVersion(JsonSchemaVersion.V_2)
                        .reconnectEnabled(false)
                        .endpoint(CONFIG_PROPERTIES.getEndpointOrThrow());

        proxyConfiguration().ifPresent(messagingConfigurationBuilder::proxyConfiguration);

        final MessagingProvider messagingProvider =
                MessagingProviders.webSocket(messagingConfigurationBuilder.build(), authenticationProvider);

        return DittoClients.newInstance(messagingProvider, messagingProvider, messagingProvider, buildMessageSerializerRegistry());
    }

    private AuthenticationProvider<WebSocket> buildAuthenticationProvider() {
        final AuthenticationProvider<WebSocket> authenticationProvider;

        if (CONFIG_PROPERTIES.getUsername().isPresent()) {
            authenticationProvider = AuthenticationProviders.basic(BasicAuthenticationConfiguration.newBuilder()
                    .username(CONFIG_PROPERTIES.getUsernameOrThrow())
                    .password(CONFIG_PROPERTIES.getPasswordOrThrow())
                    .build());
            authorizationSubject = AuthorizationSubject.newInstance("nginx:" + CONFIG_PROPERTIES.getUsernameOrThrow());
        } else if (CONFIG_PROPERTIES.getClientId().isPresent()) {
            final ClientCredentialsAuthenticationConfiguration.ClientCredentialsAuthenticationConfigurationBuilder
                    clientCredentialsAuthenticationConfigurationBuilder =
                    ClientCredentialsAuthenticationConfiguration.newBuilder()
                            .clientId(CONFIG_PROPERTIES.getClientIdOrThrow())
                            .clientSecret(CONFIG_PROPERTIES.getClientSecretOrThrow())
                            .scopes(CONFIG_PROPERTIES.getScopes())
                            .tokenEndpoint(CONFIG_PROPERTIES.getTokenEndpointOrThrow());
            final Optional<ProxyConfiguration> proxyConfiguration = proxyConfiguration();
            proxyConfiguration.ifPresent(clientCredentialsAuthenticationConfigurationBuilder::proxyConfiguration);
            authenticationProvider =
                    AuthenticationProviders.clientCredentials(clientCredentialsAuthenticationConfigurationBuilder
                            .build());
            authorizationSubject = AuthorizationSubject.newInstance(CONFIG_PROPERTIES.getClientId().toString());
        } else {
            throw new IllegalStateException("No authentication configured in config.properties!");
        }

        return authenticationProvider;
    }

    /**
     * Sets up a serializer/deserializer for the {@link org.eclipse.ditto.User.common.model.ExampleUser} model class
     * which uses Jackson in order to serialize and deserialize messages which should directly be mapped to this type.
     */
    private MessageSerializerRegistry buildMessageSerializerRegistry() { // TO DO: rimuovi ?
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
                            } catch (Exception e) {
                                throw new IllegalStateException("Could not deserialize", e);
                            }
                        }));

        return messageSerializerRegistry;
    }

    private Optional<ProxyConfiguration> proxyConfiguration() {
        final Optional<String> proxyHost = CONFIG_PROPERTIES.getProxyHost();
        final Optional<String> proxyPort = CONFIG_PROPERTIES.getProxyPort();
        if (proxyHost.isPresent() && proxyPort.isPresent()) {
            final ProxyConfiguration.ProxyOptionalSettable builder = ProxyConfiguration.newBuilder()
                    .proxyHost(proxyHost.get())
                    .proxyPort(Integer.parseInt(proxyPort.get()));
            final Optional<String> proxyPrincipal = CONFIG_PROPERTIES.getProxyPrincipal();
            final Optional<String> proxyPassword = CONFIG_PROPERTIES.getProxyPassword();
            if (proxyPrincipal.isPresent() && proxyPassword.isPresent()) {
                builder.proxyUsername(proxyPrincipal.get()).proxyPassword(proxyPassword.get());
            }
            return Optional.of(builder.build());
        }
        return Optional.empty();
    }

    /**
     * Destroys the client and waits for its graceful shutdown.
     */
    public void terminate() {
        client.destroy();
    }


}