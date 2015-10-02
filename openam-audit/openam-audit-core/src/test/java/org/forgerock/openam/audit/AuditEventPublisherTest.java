/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015 ForgeRock AS.
 */
package org.forgerock.openam.audit;

import static org.assertj.core.api.Assertions.*;
import static org.forgerock.audit.events.AccessAuditEventBuilder.ResponseStatus.SUCCESS;
import static org.forgerock.audit.events.AccessAuditEventBuilder.TimeUnit.MILLISECONDS;
import static org.forgerock.json.JsonValue.*;
import static org.forgerock.json.resource.Responses.newResourceResponse;
import static org.forgerock.openam.audit.AuditConstants.EventName;
import static org.forgerock.openam.utils.CollectionUtils.asSet;
import static org.forgerock.util.promise.Promises.newResultPromise;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

import org.forgerock.audit.AuditException;
import org.forgerock.audit.AuditServiceBuilder;
import org.forgerock.audit.events.AuditEvent;
import org.forgerock.audit.events.handlers.AuditEventHandler;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.json.resource.ServiceUnavailableException;
import org.forgerock.openam.audit.configuration.AMAuditServiceConfiguration;
import org.forgerock.services.context.Context;
import org.forgerock.util.promise.Promise;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @since 13.0.0
 */
@SuppressWarnings("unchecked")
public class AuditEventPublisherTest {

    private static final String FAILURE_SUPPRESSED_REALM = "realm1";
    private static final String FAILURE_NOT_SUPPRESSED_REALM = "realm2";

    private AuditEventHandler mockHandler;
    private AuditEventPublisher auditEventPublisher;
    private AuditServiceProvider auditServiceProvider;
    private ArgumentCaptor<JsonValue> auditEventCaptor;
    private Promise<ResourceResponse, ResourceException> dummyPromise;

    @BeforeMethod
    protected void setUp() throws Exception {
        mockHandler = mock(AuditEventHandler.class);
        auditServiceProvider = mock(AuditServiceProvider.class);
        auditEventPublisher = new AuditEventPublisher(auditServiceProvider);
        auditEventCaptor = ArgumentCaptor.forClass(JsonValue.class);
        dummyPromise = newResultPromise(newResourceResponse("", "", json(object())));
    }

    @Test
    public void publishesProvidedAuditEventToAuditService() throws Exception {
        // Given
        AuditEvent auditEvent = getAuditEvent();
        givenDefaultAuditService();

        when(mockHandler.publishEvent(
                any(Context.class), eq("access"), auditEventCaptor.capture())).thenReturn(dummyPromise);

        // When
        auditEventPublisher.publish("access", auditEvent);

        // Then
        assertThat(auditEventCaptor.getValue()).isEqualTo(auditEvent.getValue());
    }

    @Test
    public void shouldSuppressExceptionsOnPublish() throws Exception {
        // Given
        AuditEvent auditEvent = getAuditEvent();
        givenSuppressedFailureAuditService();

        // When
        try {
            auditEventPublisher.publish(FAILURE_SUPPRESSED_REALM, "unknownTopic", auditEvent);
        } catch (AuditException e) {
            fail("Audit exceptions should be suppressed when publish fails.");
        }
    }

    @Test(expectedExceptions = AuditException.class)
    public void shouldNotSuppressExceptionsOnPublish() throws Exception {
        // Given
        AuditEvent auditEvent = getAuditEvent();
        givenNonSuppressedFailureAuditService();

        // When
        auditEventPublisher.publish(FAILURE_NOT_SUPPRESSED_REALM, "unknownTopic", auditEvent);

        // Then
        // expect exception
    }

    @Test(expectedExceptions = AuditException.class)
    public void shouldThrowExceptionWhenDefaultAuditServiceShutdown() throws Exception {
        // Given
        AuditEvent auditEvent = getAuditEvent();
        givenDefaultAuditService();
        auditServiceProvider.getDefaultAuditService().shutdown();

        // When
        auditEventPublisher.publish("access", auditEvent);

        // Then
        // expect exception
    }

    @Test(expectedExceptions = AuditException.class)
    public void shouldThrowExceptionWhenRealmAndDefaultAuditServiceShutdown() throws Exception {
        // Given
        AuditEvent auditEvent = getAuditEvent();
        givenDefaultAuditService();
        auditServiceProvider.getDefaultAuditService().shutdown();
        givenNonSuppressedFailureAuditService();
        auditServiceProvider.getAuditService(FAILURE_NOT_SUPPRESSED_REALM).shutdown();

        // When
        auditEventPublisher.publish("access", auditEvent);

        // Then
        // expect exception
    }

    @Test
    public void shouldFallBackToDefaultAuditServiceWhenRealmHasShutDown() throws Exception {
        // Given
        AuditEvent auditEvent = getAuditEvent();
        givenDefaultAuditService();
        when(mockHandler.publishEvent(
                any(Context.class), eq("access"), auditEventCaptor.capture())).thenReturn(dummyPromise);

        AMAuditServiceConfiguration serviceConfig = new AMAuditServiceConfiguration(true, true, false);
        AuditServiceBuilder builder = AuditServiceBuilder.newAuditService()
                .withConfiguration(serviceConfig)
                .withAuditEventHandler(mock(AuditEventHandler.class), "handler", asSet("access"));
        AMAuditService auditService = new RealmAuditServiceProxy(builder.build(), mock(AMAuditService.class),
                serviceConfig);
        auditService.startup();
        auditService.shutdown();
        when(auditServiceProvider.getAuditService("deadRealm")).thenReturn(auditService);

        // When
        auditEventPublisher.publish("deadRealm", "access", auditEvent);

        // Then
        assertThat(auditEventCaptor.getValue()).isEqualTo(auditEvent.getValue());
    }

    private AuditEvent getAuditEvent() {
        return new AMAccessAuditEventBuilder()
                .eventName(EventName.AM_ACCESS_ATTEMPT)
                .transactionId(UUID.randomUUID().toString())
                .authentication("id=amadmin,ou=user,dc=openam,dc=forgerock,dc=org")
                .client("172.16.101.7", 62375)
                .server("216.58.208.36", 80).resourceOperation("/some/path", "CREST", "READ")
                .http("GET", "/some/path", "p1=v1&p2=v2", Collections.<String, List<String>>emptyMap())
                .response(SUCCESS, "200", 42, MILLISECONDS)
                .toEvent();
    }

    private void givenSuppressedFailureAuditService() throws ServiceUnavailableException, AuditException {
        AMAuditServiceConfiguration serviceConfig = new AMAuditServiceConfiguration(true, true, false);
        AuditServiceBuilder builder = AuditServiceBuilder.newAuditService()
                .withConfiguration(serviceConfig)
                .withAuditEventHandler(mockHandler, "handler", asSet("access"));
        AMAuditService auditService = new RealmAuditServiceProxy(builder.build(), mock(AMAuditService.class),
                serviceConfig);
        auditService.startup();
        when(auditServiceProvider.getAuditService(FAILURE_SUPPRESSED_REALM)).thenReturn(auditService);
    }

    private void givenNonSuppressedFailureAuditService() throws ServiceUnavailableException, AuditException {
        AMAuditServiceConfiguration serviceConfig = new AMAuditServiceConfiguration(true, false, false);
        AuditServiceBuilder builder = AuditServiceBuilder.newAuditService()
                .withConfiguration(serviceConfig)
                .withAuditEventHandler(mockHandler, "handler", asSet("access"));
        AMAuditService auditService = new RealmAuditServiceProxy(builder.build(), mock(AMAuditService.class),
                serviceConfig);
        auditService.startup();
        when(auditServiceProvider.getAuditService(FAILURE_NOT_SUPPRESSED_REALM)).thenReturn(auditService);
    }

    private void givenDefaultAuditService() throws ServiceUnavailableException, AuditException {
        AMAuditServiceConfiguration serviceConfig = new AMAuditServiceConfiguration(true, false, false);
        AuditServiceBuilder builder = AuditServiceBuilder.newAuditService()
                .withConfiguration(serviceConfig)
                .withAuditEventHandler(mockHandler, "handler", asSet("access"));
        AMAuditService auditService = new DefaultAuditServiceProxy(builder.build(), serviceConfig);
        auditService.startup();
        when(auditServiceProvider.getDefaultAuditService()).thenReturn(auditService);
    }
}
