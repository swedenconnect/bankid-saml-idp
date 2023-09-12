package se.swedenconnect.bankid.idp.integration.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CompletionData;


@NoArgsConstructor
public class CollectResponseBuilder extends CollectResponse {
  private String orderReference;
  private Status status = Status.PENDING;
  private String hintCode;
  private CompletionData completionData;

  public CollectResponseBuilder orderReference(String orderReference) {
    this.orderReference = orderReference;
    return this;
  }

  public CollectResponseBuilder status(Status status) {
    this.status = status;
    return this;
  }

  public CollectResponseBuilder hintCode(String hintCode) {
    this.hintCode = hintCode;
    return this;
  }

  public CollectResponseBuilder completionData(CompletionData completionData) {
    this.completionData = completionData;
    return this;
  }

  public CollectResponse build() {
    CollectResponse collectResponse = new CollectResponse();
    collectResponse.setOrderReference(orderReference);
    collectResponse.setStatus(status);
    collectResponse.setHintCode(hintCode);
    collectResponse.setCompletionData(completionData);
    return collectResponse;
  }
}


