package se.swedenconnect.bankid.idp;

import se.swedenconnect.bankid.idp.authn.ApiResponse;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.rpapi.service.QRGenerator;

public class ApiResponseFactory {
  public static ApiResponse create(BankIdSessionData data, QRGenerator generator, boolean showQr) {
    String qrCode = "";
    if (showQr) {
      qrCode = generator.generateAnimatedQRCodeBase64Image(data.getQrStartToken(), data.getQrStartSecret(), data.getStartTime());
    }
    return new ApiResponse(statusOf(data), qrCode, data.getAutoStartToken(), data.getMessageCode());
  }

  public static ApiResponse createErrorResponseTimeExpired() {
    return new ApiResponse(ApiResponse.Status.ERROR, "", "", "bankid.msg.error.timeout");
  }

  private static ApiResponse.Status statusOf(BankIdSessionData sessionData) {
    return switch (sessionData.getStatus()) {
      case COMPLETE:
        yield ApiResponse.Status.COMPLETE;
      default:
        yield ApiResponse.Status.IN_PROGRESS;
    };
  }
}
