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
package se.swedenconnect.bankid.idp.authn.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;

/**
 * Represents a request sent to the BankID server.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Builder
@AllArgsConstructor
@Data
public class PollRequest {

  /** The HTTP servlet request. */
  private final HttpServletRequest request;

  /** Whether QR code should be displayed. */
  private final boolean qr;

  /** Whether to autostart app with return URL. */
  private final boolean autoStartWithReturnUrl;

  /** The session state. */
  private final BankIdSessionState state;

  /** The Relying Party. */
  private final RelyingPartyData relyingPartyData;

  /** The data to display in the BankID app. */
  private final UserVisibleData data;

  /** The nonce as received by the frontend. */
  private final String receivedNonce;

  /** The BankID context. */
  private final BankIdContext context;
}
