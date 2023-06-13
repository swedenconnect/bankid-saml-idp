package se.swedenconnect.bankid.idp.authn.session;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.idp.authn.events.CollectResponseEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderCancellationEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderCompletionEvent;
import se.swedenconnect.bankid.idp.authn.events.OrderResponseEvent;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CompletionData;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class BankIdSessionDataListener {

    public static final Map<Boolean, PreviousDeviceSelection> PREVIOUS_DEVICE_SELECTION_MAP = Map.of(true, PreviousDeviceSelection.OTHER, false, PreviousDeviceSelection.THIS_DEVICE);
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
            writer.save(event.getRequest(), event.getCollectResponse());
        }
    }

    @EventListener
    public void handleCompletion(OrderCompletionEvent event) {
        BankIdSessionState sessionState = reader.loadSessionData(event.getRequest());
        Boolean otherDevice = sessionState.getBankIdSessionData().getShowQr();
        PreviousDeviceSelection previousDeviceSelection = PREVIOUS_DEVICE_SELECTION_MAP.get(otherDevice);
        writer.save(event.getRequest(), previousDeviceSelection);
        writer.delete(event.getRequest());
    }

    @EventListener
    public void handleOrderCancellationEvent(OrderCancellationEvent event) {
        writer.delete(event.getRequest());
    }
}
