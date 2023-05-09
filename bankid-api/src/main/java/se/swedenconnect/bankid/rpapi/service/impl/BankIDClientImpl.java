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
package se.swedenconnect.bankid.rpapi.service.impl;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.DataToSign;
import se.swedenconnect.bankid.rpapi.service.QRGenerator;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.BankIDException;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CollectResponseJson;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.ErrorResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;
import se.swedenconnect.bankid.rpapi.types.Requirement;
import se.swedenconnect.bankid.rpapi.types.UserCancelException;

/**
 * An implementation of the BankID Relying Party API methods.
 *
 * @author Martin Lindström
 */
public class BankIDClientImpl implements BankIDClient {

  /** Class logger. */
  private static final Logger log = LoggerFactory.getLogger(BankIDClientImpl.class);

  /** The unique client identifier. */
  private final String identifier;

  /** The {@link WebClient} that we use to send requests to the BankID server. */
  private final WebClient webClient;

  /** The QR code generator. */
  private final QRGenerator qrGenerator;

  /** Object mapper for JSON. */
  private static ObjectMapper objectMapper = new ObjectMapper();

  private static final String AUTH_PATH = "/auth";
  private static final String SIGN_PATH = "/sign";
  private static final String CANCEL_PATH = "/cancel";
  private static final String COLLECT_PATH = "/collect";

  /**
   * Constructor.
   *
   * @param identifier the unique client identifier
   * @param webClient the {@link WebClient} that we use to send requests to the BankID server
   * @param qrGenerator the QR code generator (may be {@code null} if QR codes are not used)
   */
  public BankIDClientImpl(final String identifier, final WebClient webClient, final QRGenerator qrGenerator) {
    Assert.hasText(identifier, "identifier must be set");
    this.identifier = identifier;
    this.webClient = Objects.requireNonNull(webClient, "'webClient' must be not be null");
    this.qrGenerator = qrGenerator;
  }

  /** {@inheritDoc} */
  @Override
  public String getIdentifier() {
    return this.identifier;
  }

  /** {@inheritDoc} */
  @Override
  public OrderResponse authenticate(final String personalIdentityNumber, final String endUserIp,
      final UserVisibleData userVisibleData, final Requirement requirement) throws BankIDException {

    Assert.hasText(endUserIp, "'endUserIp' must not be null or empty");

    // Set up the request data.
    //
    final AuthnRequest request = new AuthnRequest(personalIdentityNumber, endUserIp, requirement, userVisibleData);
    log.debug("{}: authenticate. request: [{}] [path: {}]", this.identifier, request, AUTH_PATH);

    try {
      final OrderResponse response = this.webClient.post()
          .uri(AUTH_PATH)
          .body(request, AuthnRequest.class)
          .retrieve()
          .bodyToMono(OrderResponse.class)
          .block();

      log.debug("{}: authenticate. response: [{}]", this.identifier, response);
      return response;
    }
    catch (final WebClientResponseException e) {
      log.info("{}: authenticate. Error during auth-call - {} - {} - {}",
          this.identifier, e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
      throw new BankIDException(this.getErrorResponse(e), "Auth-call failed", e);
    }
    catch (final Exception e) {
      log.error("{}: authenticate. Error during auth-call - {}", this.identifier, e.getMessage(), e);
      throw new BankIDException(ErrorCode.UNKNOWN_ERROR, "Unknown error during auth", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public OrderResponse sign(final String personalIdentityNumber, final String endUserIp,
      final DataToSign dataToSign, final Requirement requirement) throws BankIDException {

    Assert.hasText(endUserIp, "'endUserIp' must not be null or empty");
    Assert.notNull(dataToSign, "'dataToSign' must not be null");
    Assert.hasText(dataToSign.getUserVisibleData(), "'dataToSign.userVisibleData' must not be null");

    final SignRequest request = new SignRequest(personalIdentityNumber, endUserIp, requirement, dataToSign);
    log.debug("{}: sign. request: [{}] [path: {}]", this.identifier, request, SIGN_PATH);

    try {
      final OrderResponse response = this.webClient.post()
          .uri(SIGN_PATH)
          .body(request, SignRequest.class)
          .retrieve()
          .bodyToMono(OrderResponse.class)
          .block();

      log.debug("{}: sign. response: [{}]", this.identifier, response);
      return response;
    }
    catch (final WebClientResponseException e) {
      log.info("{}: sign. Error during sign-call - {} - {} - {}", 
          this.identifier, e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
      throw new BankIDException(this.getErrorResponse(e), "Sign-call failed", e);
    }
    catch (final Exception e) {
      log.error("{}: sign. Error during sign-call - {}", this.identifier, e.getMessage(), e);
      throw new BankIDException(ErrorCode.UNKNOWN_ERROR, "Unknown error during sign", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void cancel(final String orderReference) throws BankIDException {
    Assert.hasText(orderReference, "'orderReference' must not be null or empty");

    log.debug("{}: cancel: Request for cancelling order {}", this.identifier, orderReference);

    final OrderRefRequest request = new OrderRefRequest(orderReference);

    try {
      this.webClient.post()
          .uri(CANCEL_PATH)
          .body(request, OrderRefRequest.class)
          .retrieve()
          .bodyToMono(Void.class)
          .block();

      log.info("{}: cancel. Order {} successfully cancelled", this.identifier, orderReference);
    }
    catch (final WebClientResponseException e) {
      log.info("{}: cancel. Error during cancel-call - {} - {} - {}",
          this.identifier, e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
      throw new BankIDException(this.getErrorResponse(e), "Cancel-call failed", e);
    }
    catch (final Exception e) {
      log.error("{}: cancel. Error during cancel-call - {}", this.identifier, e.getMessage(), e);
      throw new BankIDException(ErrorCode.UNKNOWN_ERROR, "Unknown error during cancel", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public CollectResponse collect(final String orderReference) throws UserCancelException, BankIDException {
    Assert.hasText(orderReference, "'orderReference' must not be null or empty");

    log.debug("{}: collect: Request for collecting order {}", this.identifier, orderReference);

    final OrderRefRequest request = new OrderRefRequest(orderReference);
    try {
      final CollectResponseJson response = this.webClient.post()
          .uri(COLLECT_PATH)
          .body(request, OrderRefRequest.class)
          .retrieve()
          .bodyToMono(CollectResponseJson.class)
          .block();

      log.info("{}: collect. response: [{}]", this.identifier, response);

      if (CollectResponseJson.Status.FAILED.equals(response.getStatus())) {
        throw new BankIDException(response.getErrorCode(),
            String.format("Order '%s' failed with code '%s'", orderReference, response
                .getErrorCode()
                .getValue()));
      }
      return response;
    }
    catch (final WebClientResponseException e) {
      log.info("{}: collect. Error during collect-call - {} - {} - {}",
          this.identifier, e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
      throw new BankIDException(this.getErrorResponse(e), "Collect-call failed", e);
    }
    catch (final Exception e) {
      log.error("{}: collect. Error during collect-call - {}", this.identifier, e.getMessage(), e);
      throw new BankIDException(ErrorCode.UNKNOWN_ERROR, "Unknown error during collect", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public QRGenerator getQRGenerator() {
    return this.qrGenerator;
  }

  /**
   * Given an HTTP status error the method returns its contents as an {@link ErrorResponse}.
   *
   * @param exception the exception
   * @return an ErrorResponse
   */
  private ErrorResponse getErrorResponse(final WebClientResponseException exception) {
    final byte[] body = exception.getResponseBodyAsByteArray();
    if (body == null) {
      return new ErrorResponse(ErrorCode.UNKNOWN_ERROR, null);
    }
    try {
      return objectMapper.readValue(body, ErrorResponse.class);
    }
    catch (final IOException e) {
      log.error("{}: Failed to deserialize error response {} into ErrorResponse structure",
          this.identifier, exception.getResponseBodyAsString(), e);
      return new ErrorResponse(ErrorCode.UNKNOWN_ERROR, null);
    }
  }

  /**
   * Represents the data sent in an /auth call.
   */
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(Include.NON_NULL)
  private static class AuthnRequest {

    private final String personalNumber;
    private final String endUserIp;
    private final Requirement requirement;
    private final String userVisibleData;
    private final String userVisibleDataFormat;

    public AuthnRequest(final String personalNumber, final String endUserIp,
        final Requirement requirement, final UserVisibleData userVisibleData) {
      this.personalNumber = personalNumber;
      this.endUserIp = endUserIp;
      this.requirement = requirement;
      this.userVisibleData = Optional.ofNullable(userVisibleData).map(UserVisibleData::getUserVisibleData).orElse(null);
      this.userVisibleDataFormat =
          Optional.ofNullable(userVisibleData).map(UserVisibleData::getUserVisibleDataFormat).orElse(null);
    }

    @Override
    public String toString() {
      return String.format(
          "personalNumber='%s', endUserIp='%s', requirement=[%s], userVisibleData='%s', userVisibleDataFormat='%s'",
          this.personalNumber, this.endUserIp, this.requirement,
          Optional.ofNullable(this.userVisibleData).orElseGet(() -> "not-set"),
          Optional.ofNullable(this.userVisibleDataFormat).orElseGet(() -> "not-set"));
    }
  }

  /**
   * Represents the data sent in a /sign call.
   */
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(Include.NON_NULL)
  private static class SignRequest extends AuthnRequest {

    private final String userNonVisibleData;

    public SignRequest(final String personalNumber, final String endUserIp, final Requirement requirement,
        final DataToSign dataToSign) {
      super(personalNumber, endUserIp, requirement, dataToSign);
      this.userNonVisibleData = dataToSign.getUserNonVisibleData();
    }

    @Override
    public String toString() {
      return String.format("%s, userNonVisibleData='%s'",
          super.toString(), Optional.ofNullable(this.userNonVisibleData).orElseGet(() -> "not-set"));
    }
  }

  /**
   * Represents the data sent in /collect and /cancel calls.
   */
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  private static class OrderRefRequest {
    @SuppressWarnings("unused")
    private final String orderRef;

    public OrderRefRequest(final String orderRef) {
      this.orderRef = orderRef;
    }
  }

}
