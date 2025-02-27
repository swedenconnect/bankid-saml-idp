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
package se.swedenconnect.bankid.rpapi.service;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import se.swedenconnect.bankid.rpapi.LibraryVersion;

/**
 * Class that represents the BankID {@code userVisibleData} and {@code userVisibleDataFormat} parameters that may be
 * used in calls to authenticate and sign.
 *
 * @author Martin Lindström
 */
public class UserVisibleData implements Serializable {

  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /** Constant for the Simple Markdown V1 format. */
  public static final String VISIBLE_DATA_FORMAT_SIMPLE_MARKDOWN_V1 = "simpleMarkdownV1";

  /** The text to be displayed and signed. */
  private String userVisibleData;

  /** Format identifier for formatting the user visible data. */
  private String userVisibleDataFormat;

  /**
   * Default constructor.
   */
  public UserVisibleData() {
  }

  /**
   * Assigns the text that will be displayed to the user.
   * <p>
   * By using this method, the caller can assign the text that will be displayed to the user. The method will take care
   * of Base64-encoding.
   * </p>
   *
   * @param displayText the (non-encoded) display text
   * @see #setUserVisibleData(String)
   */
  public void setDisplayText(final String displayText) {
    this.userVisibleData = Base64.getEncoder().encodeToString(displayText.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Assigns the {@code userVisibleData}. This is the text to be displayed. The text can be formatted using CR, LF and
   * CRLF for new lines. The text must be encoded as UTF-8 and then Base64 encoded.
   * <p>
   * See also {@link #setDisplayText(String)}.
   * </p>
   *
   * @param userVisibleData base64-encoded data to be displayed
   */
  public void setUserVisibleData(final String userVisibleData) {
    this.userVisibleData = userVisibleData;
  }

  /**
   * Returns the text to be displayed. The returned string is Base64 encoded.
   *
   *
   * @return text to be displayed and signed (base64-encoded)
   */
  public String getUserVisibleData() {
    return this.userVisibleData;
  }

  /**
   * Gets the identifier for formatting the user visible data.
   *
   * @return formatting identifier or {@code null} if not assigned
   */
  public String getUserVisibleDataFormat() {
    return this.userVisibleDataFormat;
  }

  /**
   * Assigns the identifier for formatting the user visible data.
   *
   * @param userVisibleDataFormat formatting identifier
   */
  public void setUserVisibleDataFormat(final String userVisibleDataFormat) {
    this.userVisibleDataFormat = userVisibleDataFormat;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("userVisibleData='%s', userVisibleDataFormat=%s", this.userVisibleData,
        Optional.ofNullable(this.userVisibleDataFormat).orElseGet(() -> "<not set>"));
  }
}
