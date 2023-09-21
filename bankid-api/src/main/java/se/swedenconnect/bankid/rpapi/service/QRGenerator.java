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
package se.swedenconnect.bankid.rpapi.service;

import java.io.IOException;
import java.time.Instant;

import se.swedenconnect.bankid.rpapi.types.OrderResponse;

/**
 * Interface for generating a QR code.
 * <p>
 * Section 4.1 of the BankID Relying Party Guidelines describes how to generate a static QR code based on an auto start
 * token and how to present it for the user, and section 4.2 describes how to generate an "animated" code.
 * </p>
 *
 * @author Martin Lindström
 */
public interface QRGenerator {

  /**
   * Generates a (static) QR code image.
   * <p>
   * The auto start token will be used to build an URI according to section 4.1 of the BankID Relying Party Guidelines.
   * </p>
   *
   * @param autoStartToken the BankID autostart token
   * @param size the width and height of the generated QR code (in pixels)
   * @param format the format for the generated QR code
   * @return an byte array representing the generated QR code
   * @throws IOException for errors during generation
   * @see #generateQRCodeImage(String)
   */
  byte[] generateQRCodeImage(final String autoStartToken, final int size, final ImageFormat format)
      throws IOException;

  /**
   * Generates a (static) QR code image using default settings for size and the image format.
   *
   * @param autoStartToken the BankID autostart token
   * @return an byte array representing the generated QR code
   * @throws IOException for errors during generation
   * @see #generateQRCodeImage(String, int, ImageFormat)
   */
  byte[] generateQRCodeImage(final String autoStartToken) throws IOException;

  /**
   * Generates an "animated" QR code image.
   * <p>
   * The QR-code will be build according to section 4.2 of the BankID Relying Party Guidelines.
   * </p>
   *
   * @param qrStartToken the QR start token (see {@link OrderResponse#getQrStartToken()})
   * @param qrStartSecret the QR start secret (see {@link OrderResponse#getQrStartSecret()})
   * @param orderTime the instant when the result from an {@link BankIDClient#authenticate(AuthenticateRequest)} or
   *          {@link BankIDClient#sign(SignatureRequest)} call was received
   * @param size the width and height of the generated QR code (in pixels)
   * @param format the format for the generated QR code
   * @return an byte array representing the generated QR code
   * @throws IOException for errors during generation
   * @see #generateAnimatedQRCodeImage(String, String, Instant)
   */
  byte[] generateAnimatedQRCodeImage(final String qrStartToken, final String qrStartSecret, final Instant orderTime,
      final int size, final ImageFormat format) throws IOException;

  /**
   * Generates an "animated" QR code image using default settings for size and the image format.
   *
   * @param qrStartToken the QR start token (see {@link OrderResponse#getQrStartToken()})
   * @param qrStartSecret the QR start secret (see {@link OrderResponse#getQrStartSecret()})
   * @param orderTime the instant when the result from an {@link BankIDClient#authenticate(AuthenticateRequest)} or a
   *          {@link BankIDClient#sign(SignatureRequest)} call was received
   * @return an byte array representing the generated QR code
   * @throws IOException for errors during generation
   * @see #generateAnimatedQRCodeImage(String, String, Instant, int, ImageFormat)
   */
  byte[] generateAnimatedQRCodeImage(final String qrStartToken, final String qrStartSecret, final Instant orderTime)
      throws IOException;

  /**
   * Generates a (static) QR code image as a Base64 encoded image.
   * <p>
   * For example:
   * </p>
   *
   * <pre>
   * {@code data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUA
   * AAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO
   * 9TXL0Y4OHwAAAABJRU5ErkJggg==
   * }
   * </pre>
   *
   * <p>
   * The image may then be directly inserted in HTML code as:
   * </p>
   *
   * <pre>
   * {@code <img src="data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUA
   * AAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO
   * 9TXL0Y4OHwAAAABJRU5ErkJggg==" scale="0">
   * }
   * </pre>
   *
   * @param autoStartToken the BankID autostart token
   * @param size the width and height of the generated QR code (in pixels)
   * @param format the format for the generated QR code
   * @return the base64 encoded image
   * @throws IOException for errors during generation
   * @see #generateQRCodeBase64Image(String)
   */
  String generateQRCodeBase64Image(final String autoStartToken, final int size, final ImageFormat format)
      throws IOException;

  /**
   * Generates a (static) QR code image as a Base64 encoded image using default settings for size and the image format.
   *
   * @param autoStartToken the BankID autostart token
   * @return the base64 encoded image
   * @throws IOException for errors during generation
   * @see #generateQRCodeBase64Image(String, int, ImageFormat)
   */
  String generateQRCodeBase64Image(final String autoStartToken) throws IOException;

  /**
   * Generates an "animated" QR code image as a Base64 encoded image.
   * <p>
   * For example:
   * </p>
   *
   * <pre>
   * {@code data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUA
   * AAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO
   * 9TXL0Y4OHwAAAABJRU5ErkJggg==
   * }
   * </pre>
   *
   * <p>
   * The image may then be directly inserted in HTML code as:
   * </p>
   *
   * <pre>
   * {@code <img src="data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUA
   * AAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO
   * 9TXL0Y4OHwAAAABJRU5ErkJggg==" scale="0">
   * }
   * </pre>
   *
   * @param qrStartToken the QR start token (see {@link OrderResponse#getQrStartToken()})
   * @param qrStartSecret the QR start secret (see {@link OrderResponse#getQrStartSecret()})
   * @param orderTime the instant when the result from an {@link BankIDClient#authenticate(AuthenticateRequest)} or a
   *          {@link BankIDClient#sign(SignatureRequest)} call was received
   * @param size the width and height of the generated QR code (in pixels)
   * @param format the format for the generated QR code
   * @return the base64 encoded image
   * @throws IOException for errors during generation
   * @see #generateAnimatedQRCodeImage(String, String, Instant)
   */
  String generateAnimatedQRCodeBase64Image(final String qrStartToken, final String qrStartSecret,
      final Instant orderTime, final int size, final ImageFormat format) throws IOException;

  /**
   * Generates an "animated" QR code image as a Base64 encoded image using default settings for size and the image
   * format.
   *
   * @param qrStartToken the QR start token (see {@link OrderResponse#getQrStartToken()})
   * @param qrStartSecret the QR start secret (see {@link OrderResponse#getQrStartSecret()})
   * @param orderTime the instant when the order was received
   * @return the base64 encoded image
   * @see #generateAnimatedQRCodeBase64Image(String, String, Instant, int, ImageFormat)
   */
  String generateAnimatedQRCodeBase64Image(final String qrStartToken, final String qrStartSecret,
      final Instant orderTime);

  /**
   * Enum representing an image format.
   */
  public enum ImageFormat {
    JPG("JPG"), PNG("PNG"), SVG("SVG");

    /**
     * Returns the image format in text format.
     *
     * @return the image format
     */
    public String getImageFormatName() {
      return this.imageFormatName;
    }

    /**
     * Parses an image format string into an {@code ImageFormat} instance.
     *
     * @param formatName the string to parse
     * @return an ImageFormat or null if there is no matching format
     */
    public static ImageFormat parse(final String formatName) {
      for (ImageFormat i : ImageFormat.values()) {
        if (i.getImageFormatName().equals(formatName)) {
          return i;
        }
      }
      throw new IllegalArgumentException("Unsupported image format");
    }

    /**
     * Hidden constructor.
     *
     * @param imageFormatName the image format name
     */
    private ImageFormat(final String imageFormatName) {
      this.imageFormatName = imageFormatName;
    }

    /** The image format name. */
    private final String imageFormatName;
  }

}
