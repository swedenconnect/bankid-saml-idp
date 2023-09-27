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
package se.swedenconnect.bankid.idp.authn.api;

import java.util.Objects;
import java.util.Optional;

import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.rpapi.service.QRGenerator;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;

/**
 * Helper class for creating an {@link ApiResponse} object.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class ApiResponseFactory {

  /**
   * Creates an {@link ApiResponse}.
   *
   * @param data      the BankID session data
   * @param generator the QR code generator bean
   * @param showQr    whether to display the QR code
   * @return an {@link ApiResponse}
   */
  public static ApiResponse create(final BankIdSessionData data, final QRGenerator generator, final boolean showQr) {
    if(Objects.nonNull(data.getErrorCode())) {
      if (data.getErrorCode().equals(ErrorCode.USER_CANCEL)) {
        return createUserCancelResponse();
      }
      return createUserErrorResponse(data);
    }
    String qrCode = "";
    // Only generate qr code when it has not been scanned and should be displayed
    if (showQr && Optional.ofNullable(data.getStatus()).map(ProgressStatus.OUTSTANDING_TRANSACTION::equals)
        .orElse(false)) {
      qrCode = generator.generateAnimatedQRCodeBase64Image(data.getQrStartToken(), data.getQrStartSecret(),
          data.getStartTime());
    }
    return new ApiResponse(statusOf(data), qrCode, data.getAutoStartToken(), data.getMessageCode());
  }

  private static ApiResponse createUserErrorResponse(BankIdSessionData data) {
    return new ApiResponse(ApiResponse.Status.ERROR, "", "", data.getMessageCode());
  }

  /**
   * Creates an {@link ApiResponse} indicating a timeout.
   *
   * @return an {@link ApiResponse}
   */
  public static ApiResponse createErrorResponseTimeExpired() {
    return new ApiResponse(ApiResponse.Status.ERROR, "", "", "bankid.msg.error.timeout");
  }

  public static ApiResponse createErrorResponseBankIdServerException() {
    return new ApiResponse(ApiResponse.Status.ERROR, "", "", "bankid.msg.error.server");
  }

  private static ApiResponse.Status statusOf(final BankIdSessionData sessionData) {
    return switch (sessionData.getStatus()) {
      case OUTSTANDING_TRANSACTION:
        yield ApiResponse.Status.NOT_STARTED;
      case COMPLETE:
        yield ApiResponse.Status.COMPLETE;
      default:
        yield ApiResponse.Status.IN_PROGRESS;
    };
  }

  /**
   * Creates an {@link ApiResponse} representing a cancelled operation.
   *
   * @return an {@link ApiResponse}
   */
  public static ApiResponse createUserCancelResponse() {
    return new ApiResponse(ApiResponse.Status.CANCEL, "", "", "bankid.msg.error.userCancel");
  }
}
