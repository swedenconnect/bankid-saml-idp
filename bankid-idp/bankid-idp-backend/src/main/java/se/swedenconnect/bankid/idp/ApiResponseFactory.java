package se.swedenconnect.bankid.idp;

import se.swedenconnect.bankid.idp.authn.ApiResponse;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.rpapi.service.QRGenerator;

import java.util.Optional;

public class ApiResponseFactory {
  public static ApiResponse create(final BankIdSessionData data, final QRGenerator generator, final boolean showQr) {
    String qrCode = "";
    // Only generate qr code when it has not been scanned and should be displayed
    if (showQr && Optional.ofNullable(data.getHintCode()).map("outstandingTransaction"::equals).orElse(false)) {
      qrCode = generator.generateAnimatedQRCodeBase64Image(data.getQrStartToken(), data.getQrStartSecret(), data.getStartTime());
    }
    return new ApiResponse(statusOf(data), qrCode, data.getAutoStartToken(), data.getMessageCode());
  }

  public static ApiResponse createErrorResponseTimeExpired() {
    return new ApiResponse(ApiResponse.Status.ERROR, "", "", "bankid.msg.error.timeout");
  }

  private static ApiResponse.Status statusOf(final BankIdSessionData sessionData) {
    return switch (sessionData.getStatus()) {
      case COMPLETE:
        yield ApiResponse.Status.COMPLETE;
      default:
        yield ApiResponse.Status.IN_PROGRESS;
    };
  }

  public static ApiResponse createUserCancelResponse() {
    return new ApiResponse(ApiResponse.Status.CANCEL, "", "", "bankid.msg.error.userCancel");
  }
}
