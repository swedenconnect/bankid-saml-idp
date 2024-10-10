/*
 * Copyright 2023-2024 Sweden Connect
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
package se.swedenconnect.bankid.idp.authn.service;

import jakarta.servlet.http.HttpServletRequest;
import org.mockito.Mockito;
import se.swedenconnect.bankid.idp.authn.DisplayText;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.config.BankIdRequirement;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

import java.util.List;

public class BankIdResponseFixture {
  public static OrderResponse createOrderResponse(final int index) {
    final OrderResponse data = new OrderResponse(); // TODO: 2023-08-18 Builder
    data.setQrStartToken("qrs-" + index);
    data.setOrderReference("or-" + index);
    data.setAutoStartToken("ast-" + index);
    data.setQrStartSecret("qss-" + index);
    return data;
  }

  public static CollectResponse createInitial(final OrderResponse response) {
    final CollectResponse collectResponse = new CollectResponse();
    collectResponse.setOrderReference(response.getOrderReference());
    collectResponse.setHintCode("hint");
    collectResponse.setStatus(CollectResponse.Status.PENDING);
    return collectResponse;
  }

  public static CollectResponse createStartFailed(final CollectResponse previous) {
    final CollectResponse collectResponse = new CollectResponse();
    collectResponse.setOrderReference(previous.getOrderReference());
    collectResponse.setHintCode("hint");
    collectResponse.setStatus(CollectResponse.Status.FAILED);
    collectResponse.setHintCode(ErrorCode.START_FAILED.getValue());
    return collectResponse;
  }

  public static CollectResponse createTransactionExpired(final CollectResponse previous) {
    final CollectResponse collectResponse = new CollectResponse();
    collectResponse.setOrderReference(previous.getOrderReference());
    collectResponse.setHintCode("hint");
    collectResponse.setStatus(CollectResponse.Status.FAILED);
    collectResponse.setHintCode(ErrorCode.EXPIRED_TRANSACTION.getValue());
    return collectResponse;
  }

  public static BankIdContext createAuth() {
    final BankIdContext context = new BankIdContext(); // TODO: 2023-08-18 Builder
    context.setOperation(BankIdOperation.AUTH);
    return context;
  }

  public static BankIdSessionState create(final PollRequest request, final OrderResponse response, final String nonce) {
    final BankIdSessionState sessionState = new BankIdSessionState();
    sessionState.push(BankIdSessionData.initialize(request, response, nonce));
    return sessionState;
  }

  public static BankIdSessionState update(final BankIdSessionState state, final CollectResponse response) {
    final BankIdSessionData bankIdSessionData = state.getBankIdSessionData();
    final BankIdSessionData data = BankIdSessionData.updateFromResponse(bankIdSessionData, response);
    state.pop();
    state.push(data);
    return state;
  }

  public static PollRequest createPollRequest(BankIDClient client) {
    PollRequest.PollRequestBuilder builder = getPollRequestBuilder(client);
    return builder.build();
  }

  public static PollRequest createPollrequest(BankIDClient client, BankIdSessionState state) {
    return getPollRequestBuilder(client).state(state).build();
  }

  private static PollRequest.PollRequestBuilder getPollRequestBuilder(final BankIDClient client) {
    final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
    Mockito.when(servletRequest.getRemoteAddr()).thenReturn("1.1.1.1");
    final UserVisibleData userVisibleData = new UserVisibleData();
    userVisibleData.setUserVisibleData("Uservisibledata");
    return PollRequest.builder()
        .qr(false)
        .request(servletRequest)
        .context(BankIdResponseFixture.createAuth())
        .data(userVisibleData)
        .relyingPartyData(new RelyingPartyData(client, List.of("relying-party"), new DisplayText(), new DisplayText(),
            null, new BankIdRequirement()));
  }
}
