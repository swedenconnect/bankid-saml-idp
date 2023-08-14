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
package se.swedenconnect.bankid.idp.authn;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyRepository;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;

@Slf4j
@AllArgsConstructor
@Component
public class BankIdSessionLoader {

  private final RelyingPartyRepository repository;

  public BankIdUserData load(final HttpServletRequest request, final Saml2UserAuthenticationInputToken token) {
    final RelyingPartyData relyingParty = this.getRelyingParty(token.getAuthnRequestToken().getEntityId());
    final BankIdSessionData sessionData = (BankIdSessionData) request.getSession().getAttribute("BANKID-STATE");
    return new BankIdUserData(sessionData, relyingParty);
  }

  public void save(final HttpServletRequest request, final BankIdSessionData data) {
    request.getSession().setAttribute("BANKID-STATE", data);
  }

  private RelyingPartyData getRelyingParty(final String entityId) {
    final RelyingPartyData rp = this.repository.getRelyingParty(entityId);
    if (rp == null) {
      log.info("SAML SP '{}' is not a registered BankID Relying Party", entityId);
      throw new IllegalArgumentException("The service provider with entityId:%s is not registered".formatted(entityId));
    }
    return rp;
  }

  public void delete(final HttpServletRequest request) {
    request.getSession().setAttribute("BANKID-STATE", null);
    request.getSession().setAttribute("BANKID-COMPLETION-DATA", null);
  }
}
