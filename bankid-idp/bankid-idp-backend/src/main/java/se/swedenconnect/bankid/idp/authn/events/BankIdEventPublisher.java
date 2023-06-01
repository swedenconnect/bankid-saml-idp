package se.swedenconnect.bankid.idp.authn.events;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

import javax.servlet.http.HttpServletRequest;

@AllArgsConstructor
@Service
public class BankIdEventPublisher {
  private final ApplicationEventPublisher publisher;




  public EventBuilder orderResponse(HttpServletRequest request, OrderResponse response, Boolean showQr) {
    return new EventBuilder(new OrderResponseEvent(request, response, showQr), publisher);
  }

  public EventBuilder collectResponse(HttpServletRequest request, CollectResponse collectResponse) {
    return new EventBuilder(new CollectResponseEvent(request, collectResponse), publisher);
  }

  public EventBuilder orderCancellation(HttpServletRequest request) {
    return new EventBuilder(new OrderCancellationEvent(request), publisher);
  }

  public EventBuilder orderCompletion(HttpServletRequest request) {
    return new EventBuilder(new OrderCompletionEvent(request), publisher);
  }

  @AllArgsConstructor
  public static class EventBuilder {
    private final Object event;
    private final ApplicationEventPublisher publisher;

    public void publish() {
      publisher.publishEvent(event);
    }
  }
}
