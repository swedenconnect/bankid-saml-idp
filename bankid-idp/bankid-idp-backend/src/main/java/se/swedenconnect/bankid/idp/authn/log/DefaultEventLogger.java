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

@Slf4j
@Component
@ConditionalOnProperty(value = "audit.logging.module", havingValue = "default")
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
