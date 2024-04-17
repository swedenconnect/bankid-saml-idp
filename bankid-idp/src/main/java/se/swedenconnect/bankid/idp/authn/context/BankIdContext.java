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
package se.swedenconnect.bankid.idp.authn.context;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import se.swedenconnect.bankid.idp.ApplicationVersion;
import se.swedenconnect.spring.saml.idp.extensions.SignatureMessageExtension;

/**
 * The context for a BankID operation.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankIdContext implements Serializable {

  private static final long serialVersionUID = ApplicationVersion.SERIAL_VERSION_UID;

  /**
   * The ID of the context. This is the same as the ID on the SAML AuthnRequest message that we are processing.
   */
  @Setter
  @Getter
  @JsonProperty(value = "client-id", required = true)
  private String id;

  /**
   * The client ID.
   */
  @Setter
  @Getter
  @JsonProperty(value = "client-id", required = true)
  private String clientId;

  /**
   * The operation - auth or sign.
   */
  @Setter
  @Getter
  @JsonProperty(value = "operation", required = true)
  private BankIdOperation operation;

  /**
   * The personal identity number. Assigned if known at the beginning of the operation.
   */
  @Setter
  @Getter
  @JsonProperty(value = "personal-number", required = false)
  private String personalNumber;

  /**
   * Holds information about whether the user selected "this device" or "other device" the
   * last time. Will only be assigned if operation is "sign".
   */
  @Setter
  @Getter
  @JsonProperty(value = "previous-device", required = false)
  private PreviousDeviceSelection previousDeviceSelection;

  /**
   * Holds the sign message (if the operation is sign and the extension has been set).
   */
  @Setter
  @Getter
  @JsonProperty(value = "sign-message", required = false)
  private SignatureMessageExtension signMessage;

}
