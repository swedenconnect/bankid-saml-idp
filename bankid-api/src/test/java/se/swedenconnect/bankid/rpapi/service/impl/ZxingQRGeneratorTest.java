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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import se.swedenconnect.bankid.rpapi.service.QRGenerator;
import se.swedenconnect.bankid.rpapi.service.QRGenerator.ImageFormat;

/**
 * Test cases for the {@code ZxingQRGenerator} class.
 *
 * @author Martin Lindström
 */
public class ZxingQRGeneratorTest {

  @Test
  public void testGenerate() throws Exception {
    final QRGenerator generator = new ZxingQRGenerator();

    final String autoStartToken = "46f6aa68-a520-49d8-9be7-f0726d038c26";

    byte[] bytes = generator.generateQRCodeImage(autoStartToken, 300, ImageFormat.PNG);
    String textInQR = decodeQRBytes(bytes);
    Assertions.assertTrue(textInQR.endsWith(autoStartToken));

    bytes = generator.generateQRCodeImage(autoStartToken, 100, ImageFormat.JPG);
    textInQR = decodeQRBytes(bytes);
    Assertions.assertTrue(textInQR.endsWith(autoStartToken));

    try {
      generator.generateQRCodeImage(autoStartToken, 100, ImageFormat.SVG);
      Assertions.fail("SVG should not be accepted");
    }
    catch (final IOException e) {
    }
  }

  @Test
  public void testGenerateAnimated() throws Exception {
    final QRGenerator generator = new ZxingQRGenerator();

    final String qrStartToken = "67df3917-fa0d-44e5-b327-edcc928297f8";
    final String qrStartSecret = "d28db9a7- 4cde-429e-a983-359be676944c";

    final byte[] bytes =
        generator.generateAnimatedQRCodeImage(qrStartToken, qrStartSecret, Instant.now(), 300, ImageFormat.PNG);
    final String textInQR = decodeQRBytes(bytes);
    Assertions.assertTrue(textInQR.startsWith("bankid." + qrStartToken + "."));
  }

  @Test
  public void testGenerateEmbedded() throws Exception {
    final QRGenerator generator = new ZxingQRGenerator();

    final String autoStartToken = "46f6aa68-a520-49d8-9be7-f0726d038c26";

    final String image = generator.generateQRCodeBase64Image(autoStartToken, 300, ImageFormat.PNG);
    Assertions.assertTrue(image.startsWith("data:image/png;base64, "));

    final String base64 = image.substring("data:image/png;base64, ".length());
    final byte[] bytes = Base64.getDecoder().decode(base64);

    final String textInQR = decodeQRBytes(bytes);
    Assertions.assertTrue(textInQR.endsWith(autoStartToken));
  }

  /**
   * Decodes the QR code bytes into a string.
   *
   * @param bytes the bytes to decode
   * @return the encoded string
   * @throws Exception for errors
   */
  private static String decodeQRBytes(final byte[] bytes) throws Exception {
    final BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
    final LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
    final BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

    return new MultiFormatReader().decode(bitmap).getText();
  }

}
