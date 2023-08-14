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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.idp.authn.events.CollectResponseEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderCancellationEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderCompletionEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderResponseEvent;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;

@Component
@ConditionalOnProperty(value = "audit.logging.module", havingValue = "default")
@Slf4j
public class DefaultEventLogger {
  
  @EventListener
  public void handleOrderResponse(final OrderResponseEvent event) {
    final RelyingPartyData relyingPartyData = event.getRequest().getRelyingPartyData();
    log.info("AuditEvent:{}", AuditIdentifierFactory.create(event.getRequest().getRequest(), relyingPartyData, AuditIdentifier.Type.START));
  }

  @EventListener
  public void handleCollectResponse(final CollectResponseEvent event) {
    log.debug("AuditEvent:{}", AuditIdentifierFactory.create(event.getRequest().getRequest(), event.getRequest().getRelyingPartyData(), AuditIdentifier.Type.COLLECT));
  }


  @EventListener
  public void handleCompletion(final OrderCompletionEvent event) {
    log.info("AuditEvent:{}", AuditIdentifierFactory.create(event.getRequest(), event.getData(), AuditIdentifier.Type.SUCCESS));
  }


  @EventListener
  public void handleOrderCancellationEvent(final OrderCancellationEvent event) {
    log.info("AuditEvent:{}", AuditIdentifierFactory.create(event.getRequest(), event.getData(), AuditIdentifier.Type.FAILURE));
  }
}
