package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.swedenconnect.bankid.rpapi.types.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BankIdSessionData {
  private String autoStartToken;
  private String qrStartToken;
  private String qrStartSecret;
  private Instant startTime;
  private String orderReference;
  private ProgressStatus status;
  private Boolean expired;

  public static BankIdSessionData of(OrderResponse response) {
    return new BankIdSessionData(
        response.getAutoStartToken(),
        response.getQrStartToken(),
        response.getQrStartSecret(),
        response.getOrderTime(),
        response.getOrderReference(),
        ProgressStatus.STARTED,
        false);
  }

  public static BankIdSessionData of(BankIdSessionData previous, CollectResponseJson json) {
    return new BankIdSessionData(
        previous.autoStartToken,
        previous.qrStartToken,
        previous.qrStartSecret,
        previous.getStartTime(),
        previous.getOrderReference(),
        json.getProgressStatus(),
        json.getErrorCode() == ErrorCode.START_FAILED
    );
  }
}
