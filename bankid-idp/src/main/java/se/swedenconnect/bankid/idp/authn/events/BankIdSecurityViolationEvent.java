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

package se.swedenconnect.bankid.idp.authn.events;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import se.swedenconnect.bankid.idp.authn.error.BankIdSecurityViolationError;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;

import java.util.Optional;

/**
 * Event for security violations.
 *
 * @author Martin Lindström
 */
public class BankIdSecurityViolationEvent extends BankIdErrorEvent {

  /**
   * Constructor.
   *
   * @param request the servlet request
   * @param data the relying party data
   */
  public BankIdSecurityViolationEvent(@Nonnull final HttpServletRequest request,
      @Nonnull final RelyingPartyData data, @Nonnull final BankIdSecurityViolationError errorCode,
      @Nullable final String errorDescription) {
    super(request, data, errorCode.getValue(), Optional.ofNullable(errorDescription)
        .orElseGet(() -> "BankID Security Violation: " + errorCode.getValue()));
  }

}
