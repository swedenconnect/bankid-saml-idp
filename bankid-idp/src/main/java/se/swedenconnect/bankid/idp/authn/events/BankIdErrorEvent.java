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
package se.swedenconnect.bankid.idp.authn.events;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;

/**
 * An event for a BankID error.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class BankIdErrorEvent extends AbstractBankIdEvent {

  /** The error code. */
  @Getter
  final String errorCode;

  /** The error description. */
  @Getter
  final String errorDescription;

  /**
   * Constructor.
   *
   * @param request the servlet request
   * @param data the relying party data
   * @param errorCode the error code
   * @param errorDescription the error description
   */
  public BankIdErrorEvent(final HttpServletRequest request, final RelyingPartyData data,
      final String errorCode, final String errorDescription) {
    super(request, data);
    this.errorCode = errorCode;
    this.errorDescription = errorDescription;
  }

}
