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
import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.idp.authn.BankIdAuthenticationProvider;
import se.swedenconnect.bankid.idp.authn.events.CollectResponseEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderCancellationEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderCompletionEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderResponseEvent;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.RedirectForAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;

@Component
@ConditionalOnProperty(value = "audit.logging.module", havingValue = "default")
@Slf4j
@AllArgsConstructor
public class DefaultEventLogger {

  private final BankIdAuthenticationProvider provider;

  @EventListener
  public AuditApplicationEvent handleOrderResponse(final OrderResponseEvent event) {
    final RelyingPartyData relyingPartyData = event.getRequest().getRelyingPartyData();
    HttpServletRequest request = event.getRequest().getRequest();
    RedirectForAuthenticationToken token = provider.getTokenRepository().getExternalAuthenticationToken(request);
    String authRequestId = ((Saml2UserAuthenticationInputToken) token.getAuthnInputToken()).getAuthnRequestToken().getAuthnRequest().getID();
    log.info("AuditEvent:{}", AuditIdentifierFactory.create(event.getRequest().getRequest(), relyingPartyData, AuditIdentifier.Type.START, authRequestId));
    AuditIdentifier auditIdentifier = AuditIdentifierFactory.create(event.getRequest().getRequest(), relyingPartyData, AuditIdentifier.Type.SUCCESS, authRequestId);
    AuditEvent auditEvent = new AuditEvent(Instant.now(), token.getPrincipal().toString(), "type", Map.of("data", auditIdentifier));
    return new AuditApplicationEvent(auditEvent);
  }

  @EventListener
  public void handleCollectResponse(final CollectResponseEvent event) {
    HttpServletRequest request = event.getRequest().getRequest();
    RedirectForAuthenticationToken token = provider.getTokenRepository().getExternalAuthenticationToken(request);
    String authRequestId = ((Saml2UserAuthenticationInputToken) token.getAuthnInputToken()).getAuthnRequestToken().getAuthnRequest().getID();
    log.debug("AuditEvent:{}", AuditIdentifierFactory.create(event.getRequest().getRequest(), event.getRequest().getRelyingPartyData(), AuditIdentifier.Type.COLLECT, authRequestId));
  }


  @EventListener
  public AuditApplicationEvent handleCompletion(final OrderCompletionEvent event) {
    HttpServletRequest request = event.getRequest();
    RedirectForAuthenticationToken token = provider.getTokenRepository().getExternalAuthenticationToken(request);
    String authRequestId = ((Saml2UserAuthenticationInputToken) token.getAuthnInputToken()).getAuthnRequestToken().getAuthnRequest().getID();
    AuditIdentifier auditIdentifier = AuditIdentifierFactory.create(event.getRequest(), event.getData(), AuditIdentifier.Type.SUCCESS, authRequestId);
    log.info("AuditEvent:{}", auditIdentifier);
    AuditEvent auditEvent = new AuditEvent(Instant.now(), token.getPrincipal().toString(), "type", Map.of("data", auditIdentifier));
    return new AuditApplicationEvent(auditEvent);
  }


  @EventListener
  public void handleOrderCancellationEvent(final OrderCancellationEvent event) {
    HttpServletRequest request = event.getRequest();
    RedirectForAuthenticationToken token = provider.getTokenRepository().getExternalAuthenticationToken(request);
    String authRequestId = ((Saml2UserAuthenticationInputToken) token.getAuthnInputToken()).getAuthnRequestToken().getAuthnRequest().getID();
    log.info("AuditEvent:{}", AuditIdentifierFactory.create(event.getRequest(), event.getData(), AuditIdentifier.Type.FAILURE, authRequestId));
  }
}
