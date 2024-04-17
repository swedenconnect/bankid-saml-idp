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
package se.swedenconnect.bankid.rpapi.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.rpapi.service.AuthenticateRequest;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.DataToSign;
import se.swedenconnect.bankid.rpapi.service.QRGenerator;
import se.swedenconnect.bankid.rpapi.service.SignatureRequest;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.BankIDException;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.ErrorResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;
import se.swedenconnect.bankid.rpapi.types.Requirement;

/**
 * An implementation of the BankID Relying Party API methods.
 *
 * @author Martin Lindström
 */
public class BankIDClientImpl implements BankIDClient {

  /** Class logger. */
  private static final Logger log = LoggerFactory.getLogger(BankIDClientImpl.class);

  /** The executor service. */
  public final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

  /**
   * The unique client identifier.
   */
  private final String identifier;

  /**
   * The {@link WebClient} that we use to send requests to the BankID server.
   */
  private final WebClient webClient;

  /**
   * The QR code generator.
   */
  private final QRGenerator qrGenerator;

  /**
   * Object mapper for JSON.
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public String getIdentifier() {
    return this.identifier;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Mono<OrderResponse> authenticate(final AuthenticateRequest request) throws BankIDException {

    Assert.hasText(request.getEndUserIp(), "'endUserIp' must not be null or empty");

    // Set up the request data.
    //
    final AuthnRequest authnRequest =
        new AuthnRequest(request.getEndUserIp(), request.getRequirement(), request.getUserVisibleData());
    log.debug("{}: authenticate. request: [{}] [path: {}]", this.identifier, request, AUTH_PATH);
    try {
      log.debug("Request serialized {}", objectMapper.writerFor(AuthnRequest.class).writeValueAsString(authnRequest));
    }
    catch (final JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    try {
      return this.webClient.post()
          .uri(AUTH_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(authnRequest)
          .retrieve()
          .onRawStatus(StatusCodePredicates.userError(), BankIdErrorBodyExtractors.userErrorBodyExtractor())
          .onRawStatus(StatusCodePredicates.serverError(), BankIdErrorBodyExtractors.serverErrorBodyExtractor())
          .bodyToMono(OrderResponse.class)
          .map(m -> {
            log.debug("{}: authenticate. response: [{}]", this.identifier, m.toString());
            return m;
          })
          .doOnError(e -> log.info("Error in request to bankid: " + request.toString(), e));
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

  /**
   * {@inheritDoc}
   */
  @Override
  public Mono<OrderResponse> sign(final SignatureRequest request) throws BankIDException {
    Assert.hasText(request.getEndUserIp(), "'endUserIp' must not be null or empty");
    Assert.notNull(request.getDataToSign(), "'dataToSign' must not be null");
    Assert.hasText(request.getDataToSign().getUserVisibleData(), "'dataToSign.userVisibleData' must not be null");

    final SignRequest signRequest =
        new SignRequest(request.getEndUserIp(), request.getRequirement(), request.getDataToSign());
    log.debug("{}: sign. request: [{}] [path: {}]", this.identifier, signRequest, SIGN_PATH);

    return this.webClient.post()
        .uri(SIGN_PATH)
        .bodyValue(signRequest)
        .retrieve()
        .onRawStatus(StatusCodePredicates.userError(), BankIdErrorBodyExtractors.userErrorBodyExtractor())
        .onRawStatus(StatusCodePredicates.serverError(), BankIdErrorBodyExtractors.serverErrorBodyExtractor())
        .bodyToMono(OrderResponse.class)
        .onErrorComplete()
        .doOnError(e -> {
          if (e instanceof final WebClientResponseException webClientResponseException) {
            log.info("{}: Sign. Error during sign-call - {} - {} - {}",
                this.identifier, webClientResponseException.getMessage(), webClientResponseException.getStatusCode(),
                webClientResponseException.getResponseBodyAsString());
            throw new BankIDException(this.getErrorResponse(webClientResponseException), "Sign-call failed",
                webClientResponseException);
          }
          else {
            log.error("{}: Sign. Error during sign-call - {}", this.identifier, e.getMessage(), e);
            throw new BankIDException(ErrorCode.UNKNOWN_ERROR, "Unknown error during sign", e);
          }
        });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Mono<Void> cancel(final String orderReference) throws BankIDException {
    Assert.hasText(orderReference, "'orderReference' must not be null or empty");
    log.debug("{}: cancel: Request for cancelling order {}", this.identifier, orderReference);

    final OrderRefRequest request = new OrderRefRequest(orderReference);

    return this.webClient.post()
        .uri(CANCEL_PATH)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnSuccess(n -> log.debug("{}: cancel. Order {} successfully cancelled", this.identifier, orderReference))
        .doOnError(e -> {
          if (e instanceof final WebClientResponseException webClientResponseException) {
            log.info("{}: cancel. Error during cancel-call - {} - {} - {}",
                this.identifier, webClientResponseException.getMessage(), webClientResponseException.getStatusCode(),
                webClientResponseException.getResponseBodyAsString());
            throw new BankIDException(this.getErrorResponse(webClientResponseException), "Cancel-call failed",
                webClientResponseException);
          }
          else {
            log.error("{}: cancel. Error during cancel-call - {}", this.identifier, e.getMessage(), e);
            throw new BankIDException(ErrorCode.UNKNOWN_ERROR, "Unknown error during collect", e);
          }
        });

  }

  @SuppressWarnings("unused")
  private Mono<? extends Throwable> defaultErrorHandler(final ClientResponse clientResponse) {
    return clientResponse.body(BodyExtractors.toMono(HashMap.class)).map(m -> {
      return new BankIDException("Error to communicate with BankID API response:" + m.toString());
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Mono<? extends CollectResponse> collect(final String orderReference) throws BankIDException {
    Assert.hasText(orderReference, "'orderReference' must not be null or empty");
    log.debug("{}: collect: Request for collecting order {}", this.identifier, orderReference);

    final OrderRefRequest request = new OrderRefRequest(orderReference);
    final WebClient.ResponseSpec retrieve = this.webClient.post()
        .uri(COLLECT_PATH)
        .bodyValue(request)
        .retrieve();
    return retrieve
        .onRawStatus(StatusCodePredicates.userError(), BankIdErrorBodyExtractors.userErrorBodyExtractor())
        .onRawStatus(StatusCodePredicates.serverError(), BankIdErrorBodyExtractors.serverErrorBodyExtractor())
        .bodyToMono(CollectResponse.class)
        .doOnSuccess(c -> log.debug("{}: collect. response: [{}]", this.identifier, c.toString()))
        .doOnError(e -> {
          if (e instanceof final WebClientResponseException webClientResponseException) {
            log.info("{}: collect. Error during collect-call - {} - {} - {}",
                this.identifier, webClientResponseException.getMessage(), webClientResponseException.getStatusCode(),
                webClientResponseException.getResponseBodyAsString());
            throw new BankIDException(this.getErrorResponse(webClientResponseException), "Collect-call failed",
                webClientResponseException);
          }
          else {
            log.error("{}: collect. Error during collect-call - {}", this.identifier, e.getMessage(), e);
            throw new BankIDException(ErrorCode.UNKNOWN_ERROR, "Unknown error during collect", e);
          }
        });
  }

  /**
   * {@inheritDoc}
   */
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
  @SuppressWarnings("unused")
  private static class AuthnRequest {

    private String endUserIp;
    private Requirement requirement;
    private String userVisibleData;
    private String userVisibleDataFormat;

    public AuthnRequest(final String endUserIp, final Requirement requirement, final UserVisibleData userVisibleData) {
      this.endUserIp = endUserIp;
      this.requirement = requirement;
      this.userVisibleData = Optional.ofNullable(userVisibleData).map(UserVisibleData::getUserVisibleData).orElse(null);
      this.userVisibleDataFormat =
          Optional.ofNullable(userVisibleData).map(UserVisibleData::getUserVisibleDataFormat).orElse(null);
    }

    public AuthnRequest() {
    }

    @Override
    public String toString() {
      return String.format(
          "endUserIp='%s', requirement=[%s], userVisibleData='%s', userVisibleDataFormat='%s'",
          this.endUserIp, this.requirement,
          Optional.ofNullable(this.userVisibleData).orElseGet(() -> "not-set"),
          Optional.ofNullable(this.userVisibleDataFormat).orElseGet(() -> "not-set"));
    }

    public void setEndUserIp(final String endUserIp) {
      this.endUserIp = endUserIp;
    }

    public void setRequirement(final Requirement requirement) {
      this.requirement = requirement;
    }

    public void setUserVisibleData(final String userVisibleData) {
      this.userVisibleData = userVisibleData;
    }

    public void setUserVisibleDataFormat(final String userVisibleDataFormat) {
      this.userVisibleDataFormat = userVisibleDataFormat;
    }
  }

  /**
   * Represents the data sent in a /sign call.
   */
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(Include.NON_NULL)
  private static class SignRequest extends AuthnRequest {

    private final String userNonVisibleData;

    public SignRequest(final String endUserIp, final Requirement requirement, final DataToSign dataToSign) {
      super(endUserIp, requirement, dataToSign);
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
  @SuppressWarnings("unused")
  private static class OrderRefRequest {

    private final String orderRef;

    public OrderRefRequest(final String orderRef) {
      this.orderRef = orderRef;
    }
  }

}
