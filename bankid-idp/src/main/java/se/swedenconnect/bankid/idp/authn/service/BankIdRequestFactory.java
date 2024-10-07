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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.config.BankIdRequirement;
import se.swedenconnect.bankid.rpapi.service.AuthenticateRequest;
import se.swedenconnect.bankid.rpapi.service.DataToSign;
import se.swedenconnect.bankid.rpapi.service.SignatureRequest;
import se.swedenconnect.bankid.rpapi.types.Requirement;

import java.util.Optional;

/**
 * Component for sending authentication and signature requests to the BankID server.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Component
public class BankIdRequestFactory {

  /**
   * Creates a BankID authentication request.
   *
   * @param request the {@link PollRequest}
   * @param returnUrl the return URL for app autostart (may be {@code null})
   * @param nonce the nonce for app autostart (may be {@code null})
   * @return an {@link AuthenticateRequest}
   */
  @Nonnull
  public AuthenticateRequest createAuthenticateRequest(@Nonnull final PollRequest request,
      @Nullable final String returnUrl, @Nullable final String nonce) {
    return new AuthenticateRequest(request.getRequest().getRemoteAddr(), request.getData(),
        createRequirement(request, request.getContext().getPersonalNumber(), BankIdOperation.AUTH),
        returnUrl, nonce);
  }

  /**
   * Creates a BankID signature request.
   *
   * @param request the {@link PollRequest}
   * @param returnUrl the return URL for app autostart (may be {@code null})
   * @param nonce the nonce for app autostart (may be {@code null})
   * @return an {@link AuthenticateRequest}
   */
  @Nonnull
  public SignatureRequest createSignRequest(@Nonnull final PollRequest request,
      @Nullable final String returnUrl, @Nullable final String nonce) {
    if (request.getData() instanceof final DataToSign dataToSign) {
      return new SignatureRequest(request.getRequest().getRemoteAddr(), dataToSign,
          createRequirement(request, request.getContext().getPersonalNumber(), BankIdOperation.SIGN),
          returnUrl, nonce);
    }
    else {
      throw new IllegalArgumentException(
          "Message was not of type DataToSign but was " + request.getData().getClass().getCanonicalName());
    }
  }

  private static Requirement createRequirement(final PollRequest request, final String personalIdentityNumber,
      final BankIdOperation operation) {
    final Optional<BankIdRequirement> requirement = Optional.ofNullable(request.getRelyingPartyData().getRequirement());
    final Requirement.RequirementBuilder builder = requirement.isPresent()
        ? Requirement.builder(fromEntityRequirement(requirement.get(), operation))
        : Requirement.builder(fromEntityRequirement(new BankIdRequirement(), operation));

    if (personalIdentityNumber != null) {
      builder.personalNumber(personalIdentityNumber);
    }
    return builder.build();
  }

  private static Requirement fromEntityRequirement(final BankIdRequirement entityRequirement,
      final BankIdOperation operation) {
    final Requirement requirement = new Requirement();
    requirement.setPinCode(operation == BankIdOperation.AUTH
        ? entityRequirement.isPinCodeAuth()
        : entityRequirement.isPinCodeSign());
    Optional.ofNullable(entityRequirement.getMrtd()).ifPresent(requirement::setMrtd);
    Optional.ofNullable(entityRequirement.getCardReader()).ifPresent(requirement::setCardReader);
    Optional.ofNullable(entityRequirement.getCertificatePolicies()).ifPresent(requirement::setCertificatePolicies);
    return requirement;
  }

}
