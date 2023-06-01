package se.swedenconnect.bankid.idp.authn.session;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import se.swedenconnect.bankid.idp.authn.events.CollectResponseEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderCancellationEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderCompletionEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderResponseEvent;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CompletionData;

import javax.servlet.http.HttpSession;

@Service
@Slf4j
@AllArgsConstructor
public class BankIdSessionDataListener {

    private final BankIdSessionWriter writer;

    private final BankIdSessionReader reader;

    @EventListener
    public void handleOrderResponse(OrderResponseEvent event) {
        log.info("Order response event was published {} for session {}", event.getOrderResponse(), event.getRequest().getSession().getId());
        BankIdSessionData bankIdSessionData = BankIdSessionData.of(event.getOrderResponse(), event.getShowQr());
        writer.save(event.getRequest(), bankIdSessionData);
    }

    @EventListener
    public void handleCollectResponse(CollectResponseEvent event) {
        HttpSession session = event.getRequest().getSession();
        log.info("Collect response event was published {} for session {}", event.getCollectResponse(), session.getId());
        BankIdSessionData previous = reader.loadSessionData(event.getRequest()).getBankIdSessionData();
        writer.save(event.getRequest(), BankIdSessionData.of(previous, event.getCollectResponse()));
        if (event.getCollectResponse().getStatus().equals(CollectResponse.Status.COMPLETE)) {
            CompletionData completionData = event.getCollectResponse().getCompletionData();
            writer.save(event.getRequest(), completionData);
        }
    }

    @EventListener
    public void handleCompletion(OrderCompletionEvent event) {
        writer.delete(event.getRequest());
    }

    @EventListener
    public void handleOrderCancellationEvent(OrderCancellationEvent event) {
        writer.delete(event.getRequest());
    }
}
