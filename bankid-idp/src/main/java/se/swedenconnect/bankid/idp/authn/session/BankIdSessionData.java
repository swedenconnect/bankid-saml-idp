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
package se.swedenconnect.bankid.idp.authn.session;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.swedenconnect.bankid.idp.ApplicationVersion;
import se.swedenconnect.bankid.idp.authn.api.StatusCodeFactory;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.error.BankIdSecurityViolationException;
import se.swedenconnect.bankid.idp.authn.service.PollRequest;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;

/**
 * Representation of an ongoing BankID session.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class BankIdSessionData implements Serializable {

  @Serial
  private static final long serialVersionUID = ApplicationVersion.SERIAL_VERSION_UID;

  /**
   * The autostart token.
   */
  private String autoStartToken;

  /**
   * The start token.
   */
  private String qrStartToken;

  /**
   * The start secret.
   */
  private String qrStartSecret;

  /**
   * The autostart nonce (created by backend).
   */
  private String nonce;

  /**
   * The nonce value as received by the frontend (when the app directed back to the browser).
   */
  private String receivedNonce;

  /**
   * The start time.
   */
  private Instant startTime;

  /**
   * The BankID order reference.
   */
  private String orderReference;

  /**
   * The latest progress status (hint).
   */
  private ProgressStatus status;

  /**
   * The latest error code (hint)
   */
  private ErrorCode errorCode;

  /**
   * Whether the start has failed or not
   */
  private Boolean startFailed;

  /**
   * Whether the session has expired.
   */
  private Boolean sessionExpired;

  /**
   * The BankID message code (to display next).
   */
  private String messageCode;

  /**
   * Whether we should display a QR code.
   */
  private boolean showQr;

  /**
   * Whether we should autostart the app and supply a return URL.
   */
  private boolean autoStartWithReturnUrl;

  /**
   * Sign or auth operation
   */
  private BankIdOperation operation;

  /**
   * Creates a {@link BankIdSessionData} given a {@link PollRequest} and an {@link OrderResponse}.
   *
   * @param request the {@link PollRequest}
   * @param response the {@link OrderResponse}
   * @param nonce the none for autostart of app (may be {@code null})
   * @return a {@link BankIdSessionData}
   */
  @Nonnull
  public static BankIdSessionData initialize(
      @Nonnull final PollRequest request, @Nonnull final OrderResponse response, @Nullable final String nonce) {
    return BankIdSessionData.builder()
        .autoStartToken(response.getAutoStartToken())
        .qrStartToken(response.getQrStartToken())
        .qrStartSecret(response.getQrStartSecret())
        .nonce(nonce)
        .receivedNonce(null) // This is init - impossible that we have received a nonce
        .startTime(response.getOrderTime())
        .orderReference(response.getOrderReference())
        .status(ProgressStatus.STARTED)
        .startFailed(false)
        .sessionExpired(false)
        .messageCode(request.getContext().getOperation() == BankIdOperation.AUTH
            ? "bankid.msg.rfa21-auth"
            : "bankid.msg.rfa21-sign")
        .showQr(request.isQr())
        .autoStartWithReturnUrl(request.isAutoStartWithReturnUrl())
        .operation(request.getContext().getOperation())
        .build();
  }

  /**
   * Creates a new {@link BankIdSessionData} given a received {@link PollRequest}.
   *
   * @param previous the original {@link BankIdSessionData}
   * @param pollRequest the {@link PollRequest}
   * @return an updated {@link BankIdSessionData}
   * @throws BankIdSecurityViolationException for nonce mismatch errors
   */
  @Nonnull
  public static BankIdSessionData updateFromPolling(
      @Nonnull final BankIdSessionData previous, @Nonnull final PollRequest pollRequest) {

    final BankIdSessionDataBuilder builder = previous.toBuilder()
        .showQr(pollRequest.isQr());

    if (pollRequest.getReceivedNonce() != null) {
      builder.receivedNonce(pollRequest.getReceivedNonce());
    }
    return builder.build();
  }

  /**
   * Creates a {@link BankIdSessionData} given a previous {@link BankIdSessionData} and an {@link CollectResponse}.
   *
   * @param previous the previous {@link BankIdSessionData}
   * @param response the {@link CollectResponse}
   * @return a {@link BankIdSessionData}
   */
  @Nonnull
  public static BankIdSessionData updateFromResponse(@Nonnull final BankIdSessionData previous,
      @Nonnull final CollectResponse response) {

    return previous.toBuilder()
        .status(Optional.ofNullable(response.getProgressStatus()).orElse(previous.getStatus()))
        .startFailed(response.getErrorCode() == ErrorCode.START_FAILED)
        .sessionExpired(response.getErrorCode() == ErrorCode.EXPIRED_TRANSACTION)
        .messageCode(StatusCodeFactory.statusCode(response, previous.isShowQr(), previous.getOperation()))
        .errorCode(response.getErrorCode())
        .build();
  }
}
