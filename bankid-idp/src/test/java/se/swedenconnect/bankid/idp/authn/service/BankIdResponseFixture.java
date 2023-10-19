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
package se.swedenconnect.bankid.idp.authn.service;

import java.util.List;

import org.mockito.Mockito;

import jakarta.servlet.http.HttpServletRequest;
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

public class BankIdResponseFixture {
  public static OrderResponse createOrderResponse(int index) {
    OrderResponse data = new OrderResponse(); // TODO: 2023-08-18 Builder
    data.setQrStartToken("qrs-" + index);
    data.setOrderReference("or-" + index);
    data.setAutoStartToken("ast-" + index);
    data.setQrStartSecret("qss-" + index);
    return data;
  }

  public static CollectResponse createInitial(OrderResponse response) {
    CollectResponse collectResponse = new CollectResponse();
    collectResponse.setOrderReference(response.getOrderReference());
    collectResponse.setHintCode("hint");
    collectResponse.setStatus(CollectResponse.Status.PENDING);
    return collectResponse;
  }

  public static CollectResponse createStartFailed(CollectResponse previous) {
    CollectResponse collectResponse = new CollectResponse();
    collectResponse.setOrderReference(previous.getOrderReference());
    collectResponse.setHintCode("hint");
    collectResponse.setStatus(CollectResponse.Status.FAILED);
    collectResponse.setHintCode(ErrorCode.START_FAILED.getValue());
    return collectResponse;
  }

  public static CollectResponse createTransactionExpired(CollectResponse previous) {
    CollectResponse collectResponse = new CollectResponse();
    collectResponse.setOrderReference(previous.getOrderReference());
    collectResponse.setHintCode("hint");
    collectResponse.setStatus(CollectResponse.Status.FAILED);
    collectResponse.setHintCode(ErrorCode.EXPIRED_TRANSACTION.getValue());
    return collectResponse;
  }

  public static BankIdContext createAuth() {
    BankIdContext context = new BankIdContext(); // TODO: 2023-08-18 Builder
    context.setOperation(BankIdOperation.AUTH);
    return context;
  }

  public static BankIdSessionState create(PollRequest request, OrderResponse response) {
    BankIdSessionState sessionState = new BankIdSessionState();
    sessionState.push(BankIdSessionData.of(request, response));
    return sessionState;
  }

  public static BankIdSessionState update(BankIdSessionState state, CollectResponse response) {
    BankIdSessionData bankIdSessionData = state.getBankIdSessionData();
    BankIdSessionData data = BankIdSessionData.of(bankIdSessionData, response, bankIdSessionData.getShowQr());
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

  private static PollRequest.PollRequestBuilder getPollRequestBuilder(BankIDClient client) {
    HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
    Mockito.when(servletRequest.getRemoteAddr()).thenReturn("1.1.1.1");
    UserVisibleData userVisibleData = new UserVisibleData();
    userVisibleData.setUserVisibleData("Uservisibledata");
    PollRequest.PollRequestBuilder builder = PollRequest.builder()
        .qr(false)
        .request(servletRequest)
        .context(BankIdResponseFixture.createAuth())
        .data(userVisibleData)
        .relyingPartyData(new RelyingPartyData(client, List.of("relying-party"), new DisplayText(), new DisplayText(),
            null, new BankIdRequirement()));
    return builder;
  }
}
