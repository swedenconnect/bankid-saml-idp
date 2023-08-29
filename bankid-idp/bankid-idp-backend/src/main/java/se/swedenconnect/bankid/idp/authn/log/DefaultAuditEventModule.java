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
package se.swedenconnect.bankid.idp.authn.log;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.idp.authn.BankIdAuthenticationProvider;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.events.*;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionReader;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.RedirectForAuthenticationToken;

import java.util.Map;

@Component
@ConditionalOnProperty(value = "audit.event.module", havingValue = "default")
@Slf4j
@AllArgsConstructor
public class DefaultAuditEventModule {

  public enum AuditEventTypes {
    BANKID_RECEIVED_REQUEST("BANKID_RECEIVED_REQUEST"),
    INIT("BANKID_INIT"),
    AUTH_COMPLETE("BANKID_AUTH_COMPLETE"),
    SIGN_COMPLETE("BANKID_SIGN_COMPLETE"),
    BANKID_CANCEL("BANKID_CANCEL"),

    BANKID_ERROR("BANKID_ERROR");

    public final String typeName;
    AuditEventTypes(String typeName) {
      this.typeName = typeName;
    }
  }

  private final BankIdAuthenticationProvider provider;

  private final BankIdSessionReader sessions;

  @EventListener
  @Order(Integer.MIN_VALUE)
  public AuditApplicationEvent handleReceivedRequestEvent(final RecievedRequestEvent event) {
    RedirectForAuthenticationToken token = provider.getTokenRepository().getExternalAuthenticationToken(event.getRequest());
    Map<String, Object> auditIdentifier = AuditIdentifierFactory.createInitialAuditIdentifier(event.getPollRequest(), token);
    AuditEvent auditEvent = new AuditEvent(token.getPrincipal().toString(), AuditEventTypes.BANKID_RECEIVED_REQUEST.typeName, auditIdentifier);
    log.info("Publishing audit event: {}", auditEvent);
    return new AuditApplicationEvent(auditEvent);
  }

  @EventListener
  @Order(Integer.MIN_VALUE)
  public AuditApplicationEvent handleOrderResponse(final OrderResponseEvent event) {
    AuditEvent auditEvent = createAuditEvent(event, AuditEventTypes.INIT.typeName, BankIdSessionData.of(event.getPollRequest(), event.getResponse()));
    log.info("Publishing audit event: {}", auditEvent);
    return new AuditApplicationEvent(auditEvent);
  }

  @EventListener
  @Order(Integer.MIN_VALUE)
  public AuditApplicationEvent handleCompletion(final OrderCompletionEvent event) {
    BankIdSessionData bankIdSessionData = sessions.loadSessionData(event.getRequest()).getBankIdSessionData();
    CollectResponse collectResponse = sessions.loadCompletionData(event.getRequest());
    RedirectForAuthenticationToken token = provider.getTokenRepository().getExternalAuthenticationToken(event.getRequest());
    AuditEvent auditEvent = new AuditEvent(token.getPrincipal().toString(), getCompletionType(bankIdSessionData.getOperation()), AuditIdentifierFactory.createCompleteAuditIdentifier(event.getData(), token, bankIdSessionData, collectResponse));
    log.info("Publishing audit event: {}", auditEvent);
    return new AuditApplicationEvent(auditEvent);
  }

  @EventListener
  @Order(Integer.MIN_VALUE)
  public AuditApplicationEvent handleCancel(final OrderCancellationEvent event) {
    AuditEvent auditEvent = createAuditEvent(event, AuditEventTypes.BANKID_CANCEL.typeName, sessions.loadSessionData(event.getRequest()).getBankIdSessionData());
    log.info("Publishing audit event: {}", auditEvent);
    return new AuditApplicationEvent(auditEvent);
  }

  @EventListener
  @Order(Integer.MIN_VALUE)
  public AuditApplicationEvent handleError(final BankIdErrorEvent event) {
    AuditEvent auditEvent = createAuditEvent(event, AuditEventTypes.BANKID_ERROR.typeName, sessions.loadSessionData(event.getRequest()).getBankIdSessionData());
    log.info("Publishing audit event: {}", auditEvent);
    return new AuditApplicationEvent(auditEvent);
  }

  private String getCompletionType(final BankIdOperation operation) {
    if (operation.equals(BankIdOperation.SIGN)) {
      return AuditEventTypes.SIGN_COMPLETE.typeName;
    }
    return AuditEventTypes.AUTH_COMPLETE.typeName;
  }

  private AuditEvent createAuditEvent(final AbstractBankIdEvent event, final String auditEventType, final BankIdSessionData bankIdSessionData) {
    RedirectForAuthenticationToken token = provider.getTokenRepository().getExternalAuthenticationToken(event.getRequest());
    Map<String, Object> auditIdentifier = AuditIdentifierFactory.createAuditIdentifier(event.getData(), token, bankIdSessionData);
    return new AuditEvent(token.getPrincipal().toString(), auditEventType, auditIdentifier);
  }

}
