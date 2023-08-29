package se.swedenconnect.bankid.idp.authn.service;

import org.mockito.Mockito;
import se.swedenconnect.bankid.idp.authn.DisplayText;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.config.EntityRequirement;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
    BankIdSessionData data = BankIdSessionData.of(state.getBankIdSessionData(), response);
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
        .relyingPartyData(new RelyingPartyData(client, List.of("relying-party"), new DisplayText(), new DisplayText(), new EntityRequirement()));
    return builder;
  }
}
