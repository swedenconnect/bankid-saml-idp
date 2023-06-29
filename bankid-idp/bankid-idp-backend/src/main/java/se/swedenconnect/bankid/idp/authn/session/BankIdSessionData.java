package se.swedenconnect.bankid.idp.authn.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.swedenconnect.bankid.idp.authn.StatusCodeFactory;
import se.swedenconnect.bankid.idp.authn.service.PollRequest;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;

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
  private String hintCode;
  private String messageCode;
  private Boolean showQr;

  public static BankIdSessionData of(final PollRequest request, final OrderResponse response) {
    return new BankIdSessionData(
        response.getAutoStartToken(),
        response.getQrStartToken(),
        response.getQrStartSecret(),
        response.getOrderTime(),
        response.getOrderReference(),
        ProgressStatus.STARTED,
        false,
        "",
        "bankid.msg.rfa21",
        request.getQr()
    );
  }

  public static BankIdSessionData of(final BankIdSessionData previous, final CollectResponse json) {
    return new BankIdSessionData(
        previous.autoStartToken,
        previous.qrStartToken,
        previous.qrStartSecret,
        previous.getStartTime(),
        previous.getOrderReference(),
        json.getProgressStatus(),
        json.getErrorCode() == ErrorCode.START_FAILED,
        json.getHintCode(),
        StatusCodeFactory.statusCode(json, previous.getShowQr()),
        previous.getShowQr()
    );
  }
}
