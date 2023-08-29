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

/**
 * The BankID event publisher.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Service
public class BankIdEventPublisher {

  private final ApplicationEventPublisher publisher;

  /**
   * Constructor.
   *
   * @param publisher the event publisher
   */
  public BankIdEventPublisher(final ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  /**
   * Builds an event after we have received an order (auth or sign) repsonse.
   *
   * @param request  the polling request
   * @param response the order response
   * @return an event to be published
   */
  public EventBuilder orderResponse(final PollRequest request, final OrderResponse response) {
    return new EventBuilder(new OrderResponseEvent(request.getRequest(), request.getRelyingPartyData(), request, response), this.publisher);
  }

  /**
   * Builds an event efter we have received a collect reponse after polling.
   *
   * @param request         the polling request
   * @param collectResponse the collect reponse
   * @return an event to be published
   */
  public EventBuilder collectResponse(final PollRequest request, final CollectResponse collectResponse) {
    return new EventBuilder(new CollectResponseEvent(request, collectResponse), this.publisher);
  }

  /**
   * Builds an event after order cancellation.
   *
   * @param request the HTTP servlet request
   * @param data    the RP data
   * @return an event to be published
   */
  public EventBuilder orderCancellation(final HttpServletRequest request, final RelyingPartyData data) {
    return new EventBuilder(new OrderCancellationEvent(request, data), this.publisher);
  }

  /**
   * Builds an event after an order has been completed
   *
   * @param request the HTTP servlet request
   * @param data    the RP data
   * @return an event to be published
   */
  public EventBuilder orderCompletion(final HttpServletRequest request, final RelyingPartyData data) {
    return new EventBuilder(new OrderCompletionEvent(request, data), this.publisher);
  }

  /**
   * Builds an event after data to be displayed in the BankID app has been created.
   *
   * @param request the HTTP servlet request
   * @param data    the data to display
   * @return an event to be published
   */
  public EventBuilder userVisibleData(final HttpServletRequest request, final UserVisibleData data) {
    return new EventBuilder(new UserVisibleDataEvent(request, data), this.publisher);
  }

  /**
   * Builds an event that the authentication has been aborted
   *
   * @param request the HTTP servlet request
   * @return an event to be published
   */
  public EventBuilder abortAuthEvent(HttpServletRequest request) {
    return new EventBuilder(new AbortAuthEvent(request), this.publisher);
  }

  /**
   * Builds an event to inform about bankid error
   */
  public EventBuilder bankIdErrorEvent(final HttpServletRequest request, final RelyingPartyData data) {
    return new EventBuilder(new BankIdErrorEvent(request, data), this.publisher);
  }
  /**
   * Builds an event to inform about received request
   */
  public EventBuilder receivedRequest(HttpServletRequest request, RelyingPartyData relyingPartyData, PollRequest pollRequest) {
    return new EventBuilder(new RecievedRequestEvent(request, relyingPartyData, pollRequest), this.publisher);
  }

  /**
   * Publisher of events.
   *
   * @author Martin Lindström
   * @author Felix Hellman
   */
  @AllArgsConstructor
  public static class EventBuilder {
    private final Object event;
    private final ApplicationEventPublisher publisher;

    public void publish() {
      this.publisher.publishEvent(this.event);
    }
  }

}
