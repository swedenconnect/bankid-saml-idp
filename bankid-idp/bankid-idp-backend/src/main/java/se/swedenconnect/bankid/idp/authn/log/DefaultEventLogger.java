package se.swedenconnect.bankid.idp.authn.log;

import org.springframework.context.event.EventListener;
import se.swedenconnect.bankid.idp.authn.events.*;

public class DefaultEventLogger {

    @EventListener
    public void handleOrderResponse(OrderResponseEvent event) {

    }

    @EventListener
    public void handleCollectResponse(CollectResponseEvent event) {

    }


    @EventListener
    public void handleCompletion(OrderCompletionEvent event) {

    }


    @EventListener
    public void handleOrderCancellationEvent(OrderCancellationEvent event) {

    }


    @EventListener
    public void handleUserVisibleDataEvent(UserVisibleDataEvent event) {
    }
}
