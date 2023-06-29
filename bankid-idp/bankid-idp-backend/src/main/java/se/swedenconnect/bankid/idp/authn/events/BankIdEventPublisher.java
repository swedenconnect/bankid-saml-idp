package se.swedenconnect.bankid.idp.authn.events;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import se.swedenconnect.bankid.idp.authn.service.PollRequest;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

import javax.servlet.http.HttpServletRequest;

@AllArgsConstructor
@Service
public class BankIdEventPublisher {
  private final ApplicationEventPublisher publisher;


  public EventBuilder orderResponse(final PollRequest request, final OrderResponse response) {
    return new EventBuilder(new OrderResponseEvent(request, response), publisher);
  }

  public EventBuilder collectResponse(final PollRequest request, final CollectResponse collectResponse) {
    return new EventBuilder(new CollectResponseEvent(request, collectResponse), publisher);
  }

  public EventBuilder orderCancellation(final HttpServletRequest request, final RelyingPartyData data) {
    return new EventBuilder(new OrderCancellationEvent(request, data), publisher);
  }

  public EventBuilder orderCompletion(final HttpServletRequest request, final RelyingPartyData data) {
    return new EventBuilder(new OrderCompletionEvent(request, data), publisher);
  }

  public EventBuilder userVisibleData(final UserVisibleData data, final HttpServletRequest request) {
    return new EventBuilder(new UserVisibleDataEvent(data, request), publisher);
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
