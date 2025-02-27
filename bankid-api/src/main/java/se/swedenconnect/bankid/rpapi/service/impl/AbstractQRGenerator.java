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
package se.swedenconnect.bankid.rpapi.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.util.Assert;

import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.QRGenerator;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

/**
 * Abstract base class for QR generation.
 *
 * @author Martin Lindström
 */
public abstract class AbstractQRGenerator implements QRGenerator {

  /** The default width and height (in pixels) to use for generated QR images. */
  public static final int DEFAULT_SIZE = 300;

  /** The default image format to use for generated QR images. */
  public static final ImageFormat DEFAULT_IMAGE_FORMAT = ImageFormat.PNG;

  /** The configured default width and height (in pixels) to use for generated QR images. */
  private int defaultSize = DEFAULT_SIZE;

  /** The configured default image format to use for generated QR images. */
  private ImageFormat defaultImageFormat = DEFAULT_IMAGE_FORMAT;

  /**
   * Builds the URI that is used as input for the static QR generation.
   *
   * @param autoStartToken the BankID autostart token
   * @return an URI string
   */
  protected String buildInput(final String autoStartToken) {
    return "bankid:///?autostarttoken=" + autoStartToken;
  }

  /**
   * Generates the QR data for an "animated" QR code.
   *
   * @param qrStartToken the QR start token (see {@link OrderResponse#getQrStartToken()})
   * @param qrStartSecret the QR start secret (see {@link OrderResponse#getQrStartSecret()})
   * @param orderTime the instant when the result from an
   *          {@link BankIDClient#authenticate(se.swedenconnect.bankid.rpapi.service.AuthenticateRequest)} or
   *          {@link BankIDClient#sign(se.swedenconnect.bankid.rpapi.service.SignatureRequest)} call was received
   * @return the QR data
   * @throws IOException for errors calculating the code
   */
  protected String buildAnimatedInput(final String qrStartToken, final String qrStartSecret, final Instant orderTime)
      throws IOException {
    try {
      final String qrTime = Long.toString(orderTime.until(Instant.now(), ChronoUnit.SECONDS));

      final Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(qrStartSecret.getBytes(StandardCharsets.US_ASCII), "HmacSHA256"));
      mac.update(qrTime.getBytes(StandardCharsets.US_ASCII));

      final String qrAuthCode = String.format("%064x", new BigInteger(1, mac.doFinal()));

      return String.join(".", "bankid", qrStartToken, qrTime, qrAuthCode);
    }
    catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException e) {
      throw new IOException("Failed to compute HMAC", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public byte[] generateQRCodeImage(final String autoStartToken) throws IOException {
    return this.generateQRCodeImage(autoStartToken, this.defaultSize, this.defaultImageFormat);
  }

  /** {@inheritDoc} */
  @Override
  public byte[] generateAnimatedQRCodeImage(
      final String qrStartToken, final String qrStartSecret, final Instant orderTime) throws IOException {
    return this.generateAnimatedQRCodeImage(
        qrStartToken, qrStartSecret, orderTime, this.defaultSize, this.defaultImageFormat);
  }

  /** {@inheritDoc} */
  @Override
  public String generateQRCodeBase64Image(final String autoStartToken, final int size, final ImageFormat format)
      throws IOException {

    final byte[] imageBytes = this.generateQRCodeImage(autoStartToken, size, format);
    return String.format("data:image/%s;base64, %s",
        format.getImageFormatName().toLowerCase(), Base64.getEncoder().encodeToString(imageBytes));
  }

  /** {@inheritDoc} */
  @Override
  public String generateQRCodeBase64Image(final String autoStartToken) throws IOException {
    return this.generateQRCodeBase64Image(autoStartToken, this.defaultSize, this.defaultImageFormat);
  }

  /** {@inheritDoc} */
  @Override
  public String generateAnimatedQRCodeBase64Image(final String qrStartToken, final String qrStartSecret,
      final Instant orderTime, final int size, final ImageFormat format) {
    try {
      final byte[] imageBytes = this.generateAnimatedQRCodeImage(qrStartToken, qrStartSecret, orderTime, size, format);
      return String.format("data:image/%s;base64, %s", format.getImageFormatName().toLowerCase(),
          Base64.getEncoder().encodeToString(imageBytes));
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }

  }

  /** {@inheritDoc} */
  @Override
  public String generateAnimatedQRCodeBase64Image(
      final String qrStartToken, final String qrStartSecret, final Instant orderTime) {
    return this.generateAnimatedQRCodeBase64Image(qrStartToken, qrStartSecret, orderTime,
        this.defaultSize, this.defaultImageFormat);
  }

  /**
   * Assigns the default width and height (in pixels) to use for generated QR images.
   * <p>
   * If not assigned, {@link #DEFAULT_SIZE} will be used.
   * </p>
   *
   * @param defaultSize default width and height
   */
  public void setDefaultSize(final int defaultSize) {
    this.defaultSize = defaultSize;
  }

  /**
   * Assigns the configured default image format to use for generated QR images.
   * <p>
   * If not assigned, {@link #DEFAULT_IMAGE_FORMAT} will be used.
   * </p>
   *
   * @param defaultImageFormat the default format
   */
  public void setDefaultImageFormat(final ImageFormat defaultImageFormat) {
    Assert.notNull(defaultImageFormat, "defaultImageFormat must not be null");
    this.defaultImageFormat = defaultImageFormat;
  }

}
