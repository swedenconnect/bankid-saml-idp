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

package se.swedenconnect.bankid.idp.authn.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representation of an API request message body.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiRequest {

  /**
   * Whether to display the QR code.
   */
  @JsonProperty(value = "display-qr", required = false, defaultValue = "false")
  private Boolean displayQr = Boolean.FALSE;

  /**
   * Whether the BankID app should be autostarted using a link.
   */
  @JsonProperty(value = "autostart-with-return-url", required = false, defaultValue = "false")
  private Boolean autoStartWithReturnUrl = Boolean.FALSE;

  /**
   * The user agent string.
   */
  @JsonProperty(value = "user-agent", required = false)
  private String userAgent;

  /**
   * The nonce that was received by the frontend. This is set when we are returning to the frontend after have
   * autostarted the app (using a return URL).
   */
  @JsonProperty(value = "nonce", required = false)
  private String nonce;

}
