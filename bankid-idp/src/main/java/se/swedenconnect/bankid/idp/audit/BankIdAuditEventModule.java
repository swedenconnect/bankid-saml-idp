/*
 * Copyright 2023 Sweden Connect
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
package se.swedenconnect.bankid.idp.audit;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import se.swedenconnect.bankid.idp.authn.BankIdAuthenticationProvider;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.events.AbstractBankIdEvent;
import se.swedenconnect.bankid.idp.authn.events.BankIdErrorEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderCancellationEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderCompletionEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderResponseEvent;
import se.swedenconnect.bankid.idp.authn.events.RecievedRequestEvent;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionReader;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CompletionData;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;

/**
 * Bean responsible of creating BankID audit events by listening to BankID events.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Component
@Slf4j
public class BankIdAuditEventModule {

  /** The BankID authentication provider. */
  private final BankIdAuthenticationProvider provider;

  /** The session handler. */
  private final BankIdSessionReader sessions;

  /**
   * Constructor.
   *
   * @param provider the BankID authentication provider
   * @param sessions the session handler
   */
  public BankIdAuditEventModule(final BankIdAuthenticationProvider provider, final BankIdSessionReader sessions) {
    this.provider = Objects.requireNonNull(provider, "provider must not be null");
    this.sessions = Objects.requireNonNull(sessions, "sessions must not be null");
  }

  /**
   * Translates a {@link RecievedRequestEvent} to a {@link BankIdAuditEventTypes#BANKID_RECEIVED_REQUEST} audit event.
   *
   * @param event the event
   * @return the audit event
   */
  @EventListener
  @Order(Integer.MIN_VALUE)
  public AuditApplicationEvent handleReceivedRequestEvent(final RecievedRequestEvent event) {

    final Saml2UserAuthenticationInputToken token =
        this.provider.getTokenRepository().getExternalAuthenticationToken(event.getRequest()).getAuthnInputToken();

    final String principal = token.getAuthnRequestToken().getAuthnRequest().getIssuer().getValue();

    final Map<String, Object> auditIdentifier = createAuditIdentifier(
        event.getPollRequest().getRelyingPartyData(), event.getPollRequest().getContext().getOperation(),
        null, token);

    final AuditEvent auditEvent = new AuditEvent(principal,
        BankIdAuditEventTypes.BANKID_RECEIVED_REQUEST.getTypeName(), auditIdentifier);

    log.info("Publishing audit event: {} - {}", auditEvent.getType(), auditEvent.getPrincipal());
    return new AuditApplicationEvent(auditEvent);
  }

  /**
   * Translates an {@link OrderResponseEvent} to a {@link BankIdAuditEventTypes#INIT} audit event.
   *
   * @param event the event
   * @return the audit event
   */
  @EventListener
  @Order(Integer.MIN_VALUE)
  public AuditApplicationEvent handleOrderResponse(final OrderResponseEvent event) {

    final AuditEvent auditEvent = this.createAuditEvent(event, BankIdAuditEventTypes.INIT,
        BankIdSessionData.of(event.getPollRequest(), event.getResponse()));

    log.info("Publishing audit event: {} - {}", auditEvent.getType(), auditEvent.getPrincipal());
    return new AuditApplicationEvent(auditEvent);
  }

  /**
   * Translates an {@link OrderCompletionEvent} to a {@link BankIdAuditEventTypes#AUTH_COMPLETE} or
   * {@link BankIdAuditEventTypes#SIGN_COMPLETE} audit event.
   *
   * @param event the event
   * @return the audit event
   */
  @EventListener
  @Order(Integer.MIN_VALUE)
  public AuditApplicationEvent handleCompletion(final OrderCompletionEvent event) {

    final Saml2UserAuthenticationInputToken token =
        this.provider.getTokenRepository().getExternalAuthenticationToken(event.getRequest()).getAuthnInputToken();
    final String principal = token.getAuthnRequestToken().getAuthnRequest().getIssuer().getValue();

    final BankIdSessionData bankIdSessionData =
        this.sessions.loadSessionData(event.getRequest()).getBankIdSessionData();

    final BankIdAuditEventTypes type = bankIdSessionData.getOperation() == BankIdOperation.SIGN
        ? BankIdAuditEventTypes.SIGN_COMPLETE
        : BankIdAuditEventTypes.AUTH_COMPLETE;

    final Map<String, Object> auditIdentifier = createAuditIdentifier(event.getData(),
        bankIdSessionData.getOperation(), bankIdSessionData.getOrderReference(), token);

    final CollectResponse collectResponse = this.sessions.loadCompletionData(event.getRequest());
    final CompletionData completionData = collectResponse.getCompletionData();

    final Map<String, Object> user = new HashMap<>();
    user.put("personal-number", completionData.getUser().getPersonalNumber());
    user.put("name", completionData.getUser().getName());

    final Map<String, String> device = new HashMap<>();
    if (completionData.getDevice().getIpAddress() != null) {
      device.put("ip-address", completionData.getDevice().getIpAddress());
    }
    if (completionData.getDevice().getUhi() != null) {
      device.put("uhi", completionData.getDevice().getUhi());
    }
    if (!device.isEmpty()) {
      user.put("device", device);
    }

    auditIdentifier.put("user", user);

    final AuditEvent auditEvent = new AuditEvent(principal, type.getTypeName(), auditIdentifier);

    log.info("Publishing audit event: {} - {}", auditEvent.getType(), auditEvent.getPrincipal());
    return new AuditApplicationEvent(auditEvent);
  }

  /**
   * Translates an {@link OrderCancellationEvent} to a {@link BankIdAuditEventTypes#BANKID_CANCEL} audit event.
   *
   * @param event the event
   * @return the audit event
   */
  @EventListener
  @Order(Integer.MIN_VALUE)
  public AuditApplicationEvent handleCancel(final OrderCancellationEvent event) {

    final AuditEvent auditEvent = this.createAuditEvent(event, BankIdAuditEventTypes.BANKID_CANCEL,
        this.sessions.loadSessionData(event.getRequest()).getBankIdSessionData());

    log.info("Publishing audit event: {} - {}", auditEvent.getType(), auditEvent.getPrincipal());
    return new AuditApplicationEvent(auditEvent);
  }

  /**
   * Translates a {@link BankIdErrorEvent} to a {@link BankIdAuditEventTypes#BANKID_ERROR} audit event.
   *
   * @param event the event
   * @return the audit event
   */
  @EventListener
  @Order(Integer.MIN_VALUE)
  public AuditApplicationEvent handleError(final BankIdErrorEvent event) {

    final Saml2UserAuthenticationInputToken token =
        this.provider.getTokenRepository().getExternalAuthenticationToken(event.getRequest()).getAuthnInputToken();
    final String principal = token.getAuthnRequestToken().getAuthnRequest().getIssuer().getValue();

    final BankIdSessionData bankIdSessionData =
        this.sessions.loadSessionData(event.getRequest()).getBankIdSessionData();

    final Map<String, Object> auditIdentifier = createAuditIdentifier(event.getData(),
        bankIdSessionData.getOperation(), bankIdSessionData.getOrderReference(), token);
    auditIdentifier.put("error-code", event.getErrorCode());
    if (event.getErrorDescription() != null) {
      auditIdentifier.put("error-description", event.getErrorDescription());
    }
    final AuditEvent auditEvent = new AuditEvent(principal, BankIdAuditEventTypes.BANKID_ERROR.getTypeName(), auditIdentifier);

    log.info("Publishing audit event: {} - {}", auditEvent.getType(), auditEvent.getPrincipal());
    return new AuditApplicationEvent(auditEvent);
  }

  private AuditEvent createAuditEvent(
      final AbstractBankIdEvent event, final BankIdAuditEventTypes type, final BankIdSessionData sessionData) {

    final Saml2UserAuthenticationInputToken token =
        this.provider.getTokenRepository().getExternalAuthenticationToken(event.getRequest()).getAuthnInputToken();

    final String principal = token.getAuthnRequestToken().getAuthnRequest().getIssuer().getValue();
    final Map<String, Object> auditIdentifier = createAuditIdentifier(event.getData(), sessionData.getOperation(),
        sessionData.getOrderReference(), token);
    return new AuditEvent(principal, type.getTypeName(), auditIdentifier);
  }

  private static Map<String, Object> createAuditIdentifier(final RelyingPartyData data,
      final BankIdOperation operation, final String orderRef, final Saml2UserAuthenticationInputToken token) {

    final Map<String, Object> map = new HashMap<>();
    map.put("rp", Optional.ofNullable(data.getId()).orElseGet(() -> "unknown"));
    map.put("sp-entity-id", token.getAuthnRequestToken().getAuthnRequest().getIssuer().getValue());
    map.put("authn-request-id", token.getAuthnRequestToken().getAuthnRequest().getID());
    if (orderRef != null) {
      map.put("order-ref", orderRef);
    }
    map.put("operation", operation);
    return map;
  }

}
