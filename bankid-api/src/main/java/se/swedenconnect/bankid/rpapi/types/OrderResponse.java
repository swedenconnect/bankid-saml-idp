/*
 * Copyright 2023-2025 Sweden Connect
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
package se.swedenconnect.bankid.rpapi.types;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an order response, i.e., the response message received from an auth or sign request.
 *
 * @author Martin Lindström
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponse {

  /** The order reference string. */
  @JsonProperty(value = "orderRef", required = true)
  private String orderReference;

  /** The auto start token. */
  @JsonProperty(value = "autoStartToken")
  private String autoStartToken;

  /** Used to compute an animated QR code. */
  @JsonProperty(value = "qrStartToken")
  private String qrStartToken;

  /** Used to compute an animated QR code. */
  @JsonProperty(value = "qrStartSecret")
  private String qrStartSecret;

  /**
   * The orderTime property is used when generating "animated" QR codes. The property is instantiated with the current
   * time when the object is created.
   */
  @JsonIgnore
  private final Instant orderTime = Instant.now();

  /**
   * Returns the order reference string.
   *
   * @return the order reference
   */
  public String getOrderReference() {
    return this.orderReference;
  }

  /**
   * Assigns the order reference string.
   *
   * @param orderReference the order reference
   */
  public void setOrderReference(final String orderReference) {
    this.orderReference = orderReference;
  }

  /**
   * Returns the auto start token.
   *
   * @return the auto start token
   */
  public String getAutoStartToken() {
    return this.autoStartToken;
  }

  /**
   * Assigns the auto start token.
   *
   * @param autoStartToken the auto start token
   */
  public void setAutoStartToken(final String autoStartToken) {
    this.autoStartToken = autoStartToken;
  }

  /**
   * Gets the QR start token used to compute an animated QR code.
   * <p>
   * Available for BankID RP API v5.1 and later.
   * </p>
   *
   * @return QR start token, or null if not available (pre v5.1)
   */
  public String getQrStartToken() {
    return this.qrStartToken;
  }

  /**
   * Assigns the QR start token used to compute an animated QR code.
   * <p>
   * Available for BankID RP API v5.1 and later.
   * </p>
   *
   * @param qrStartToken the QR start token
   */
  public void setQrStartToken(final String qrStartToken) {
    this.qrStartToken = qrStartToken;
  }

  /**
   * Gets the QR start secret string.
   * <p>
   * Available for BankID RP API v5.1 and later.
   * </p>
   *
   * @return the QR start secrret, or null if not available (pre v5.1)
   */
  public String getQrStartSecret() {
    return this.qrStartSecret;
  }

  /**
   * Assigns the QR start secret string.
   * <p>
   * Available for BankID RP API v5.1 and later.
   * </p>
   *
   * @param qrStartSecret the QR start secret string
   */
  public void setQrStartSecret(final String qrStartSecret) {
    this.qrStartSecret = qrStartSecret;
  }

  /**
   * Gets the orderTime property that is used when generating "animated" QR codes. The property is instantiated with the
   * current time when the object is created.
   *
   * @return the instant when the response object was instantited
   */
  public Instant getOrderTime() {
    return this.orderTime;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("orderRef='%s', autoStartToken='%s', qrStartToken='%s', qrStartSecret='%s', orderTime='%s'",
        this.orderReference, this.autoStartToken, this.qrStartToken, this.qrStartSecret, this.orderTime.toString());
  }

}
