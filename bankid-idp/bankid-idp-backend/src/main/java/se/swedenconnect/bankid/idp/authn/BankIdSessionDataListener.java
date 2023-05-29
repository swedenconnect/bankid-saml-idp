package se.swedenconnect.bankid.idp.authn;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import se.swedenconnect.bankid.idp.authn.events.CollectResponseEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderCancellationEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderCompletionEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderResponseEvent;
import se.swedenconnect.bankid.rpapi.types.CollectResponseJson;
import se.swedenconnect.bankid.rpapi.types.CompletionData;

import javax.servlet.http.HttpSession;

@Service
@Slf4j
public class BankIdSessionDataListener {
  @EventListener
  public void handleOrderResponse(OrderResponseEvent event) {
    log.info("Order response event was published {} for session {}", event.getOrderResponse(), event.getRequest().getSession().getId());
    event.getRequest().getSession().setAttribute("BANKID-STATE", BankIdSessionData.of(event.getOrderResponse()));
  }

  @EventListener
  public void handleCollectResponse(CollectResponseEvent event) {
    HttpSession session = event.getRequest().getSession();
    log.info("Collect response event was published {} for session {}", event.getCollectResponse(), session.getId());
    BankIdSessionData bankIdSessionData = (BankIdSessionData) session.getAttribute("BANKID-STATE");
    session.setAttribute("BANKID-STATE", BankIdSessionData.of(bankIdSessionData, event.getCollectResponse()));
    if (event.getCollectResponse().getStatus().equals(CollectResponseJson.Status.COMPLETE)) {
      CompletionData completionData = event.getCollectResponse().getCompletionData();
      session.setAttribute("BANKID-COMPLETION-DATA", completionData);
    }
  }

  @EventListener
  public void handleCompletion(OrderCompletionEvent event) {
    deleteBankIdSession(event.getRequest().getSession());
  }

  @EventListener
  public void handleOrderCancellationEvent(OrderCancellationEvent event) {
    deleteBankIdSession(event.getRequest().getSession());
  }

  private static void deleteBankIdSession(HttpSession session) {
    session.setAttribute("BANKID-COMPLETION-DATA", null);
    session.setAttribute("BANKID-STATE", null);
  }
}
