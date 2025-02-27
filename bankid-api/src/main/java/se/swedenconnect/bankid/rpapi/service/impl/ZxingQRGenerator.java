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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * A QR generator implementation based on the ZXing open source library.
 * <p>
 * <b>Note:</b> This implementation does not support the SVG image format.
 * </p>
 *
 * @author Martin Lindström
 */
public class ZxingQRGenerator extends AbstractQRGenerator {

  /** Class logger. */
  private final Logger log = LoggerFactory.getLogger(ZxingQRGenerator.class);

  /** {@inheritDoc} */
  @Override
  public byte[] generateQRCodeImage(final String autoStartToken, final int size, final ImageFormat format)
      throws IOException {
    return this.generateQRCode(this.buildInput(autoStartToken), size, format);
  }

  /** {@inheritDoc} */
  @Override
  public byte[] generateAnimatedQRCodeImage(final String qrStartToken, final String qrStartSecret,
      final Instant orderTime, final int size, final ImageFormat format) throws IOException {
    return this.generateQRCode(this.buildAnimatedInput(qrStartToken, qrStartSecret, orderTime), size, format);
  }

  /**
   * Generates the QR code image based on the supplied input string.
   *
   * @param input the input
   * @param size the width and height of the generated QR code (in pixels)
   * @param format the format for the generated QR code
   * @return an byte array representing the generated QR code
   * @throws IOException for errors during generation
   */
  private byte[] generateQRCode(final String input, final int size, final ImageFormat format) throws IOException {
    if (ImageFormat.SVG.equals(format)) {
      throw new IOException("Image format SVG is not supported by " + this.getClass().getSimpleName());
    }
    try {
      log.debug("Generating QR code in {} format based on {}", format, input);
      final QRCodeWriter writer = new QRCodeWriter();
      final BitMatrix bytes = writer.encode(input, BarcodeFormat.QR_CODE, size, size);
      final ByteArrayOutputStream stream = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(bytes, format.getImageFormatName(), stream);
      return stream.toByteArray();
    }
    catch (WriterException e) {
      throw new IOException("Failed to generate QR code: " + e.getMessage(), e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setDefaultImageFormat(final ImageFormat defaultImageFormat) {
    if (ImageFormat.SVG.equals(defaultImageFormat)) {
      throw new IllegalArgumentException(
        "Image format SVG is not supported by " + this.getClass().getSimpleName());
    }
    super.setDefaultImageFormat(defaultImageFormat);
  }

}
