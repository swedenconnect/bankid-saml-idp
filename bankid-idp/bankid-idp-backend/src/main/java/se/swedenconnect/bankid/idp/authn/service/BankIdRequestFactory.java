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
package se.swedenconnect.bankid.idp.authn.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import se.swedenconnect.bankid.idp.config.EntityRequirement;
import se.swedenconnect.bankid.rpapi.service.AuthenticateRequest;
import se.swedenconnect.bankid.rpapi.service.DataToSign;
import se.swedenconnect.bankid.rpapi.service.SignatureRequest;
import se.swedenconnect.bankid.rpapi.types.Requirement;

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
   * @return an {@link AuthenticateRequest}
   */
  public AuthenticateRequest createAuthenticateRequest(final PollRequest request) {
    return new AuthenticateRequest(request.getContext().getPersonalNumber(),
        request.getRequest().getRemoteAddr(),
        request.getData(),
        createRequirement(request));
  }

  /**
   * Creates a BankID signature request.
   * 
   * @param request the {@link PollRequest}
   * @return an {@link AuthenticateRequest}
   */
  public SignatureRequest createSignRequest(final PollRequest request) {
    if (request.getData() instanceof final DataToSign dataToSign) {
      return new SignatureRequest(request.getContext().getPersonalNumber(),
          request.getRequest().getRemoteAddr(),
          dataToSign,
          createRequirement(request));
    }
    else {
      throw new IllegalArgumentException(
          "Message was not of type DataToSign but was " + request.getData().getClass().getCanonicalName());
    }
  }

  private static Requirement createRequirement(final PollRequest request) {
    final Optional<EntityRequirement> requirement = Optional.ofNullable(request.getRelyingPartyData().getRequirement());
    return requirement.map(e -> fromEntityRequirement(e))
        .orElseGet(() -> new Requirement());
  }

  private static Requirement fromEntityRequirement(final EntityRequirement entityRequirement) {
    final Requirement requirement = new Requirement();
    Optional.ofNullable(entityRequirement.getUseFingerPrint()).ifPresent(requirement::setAllowFingerprint);
    Optional.ofNullable(entityRequirement.getIssuerCn()).ifPresent(requirement::setIssuerCn);
    Optional.ofNullable(entityRequirement.getTokenStartRequired()).ifPresent(requirement::setTokenStartRequired);
    Optional.ofNullable(entityRequirement.getCardReader()).ifPresent(requirement::setCardReader);
    Optional.ofNullable(entityRequirement.getCertificatePolicies()).ifPresent(requirement::setCertificatePolicies);
    return requirement;
  }

}
