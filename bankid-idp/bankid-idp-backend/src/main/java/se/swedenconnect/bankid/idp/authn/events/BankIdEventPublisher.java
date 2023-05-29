package se.swedenconnect.bankid.idp.authn.events;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import se.swedenconnect.bankid.rpapi.types.CollectResponseJson;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

import javax.servlet.http.HttpServletRequest;

@AllArgsConstructor
@Service
public class BankIdEventPublisher {
  private final ApplicationEventPublisher publisher;




  public EventBuilder orderResponse(HttpServletRequest request, OrderResponse response) {
    return new EventBuilder(new OrderResponseEvent(request, response), publisher);
  }

  public EventBuilder collectResponse(HttpServletRequest request, CollectResponseJson collectResponseJson) {
    return new EventBuilder(new CollectResponseEvent(request, collectResponseJson), publisher);
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
