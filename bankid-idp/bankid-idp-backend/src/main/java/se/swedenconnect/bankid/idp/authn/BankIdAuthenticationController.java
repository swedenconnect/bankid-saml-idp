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

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import lombok.AllArgsConstructor;
import se.swedenconnect.bankid.idp.authn.error.NoSuchRelyingPartyException;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionReader;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyRepository;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.AbstractAuthenticationController;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatus;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatusException;

/**
 * The controller to which the Spring Security SAML IdP flow directs the user to initiate BankID
 * authentication/signature.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Controller
@AllArgsConstructor
public class BankIdAuthenticationController extends AbstractAuthenticationController<BankIdAuthenticationProvider> {

  /** The path to where the Spring Security SAML IdP flow directs the user agent to. */
  public static final String AUTHN_PATH = "/bankid"; // TODO: 2023-09-05 Make configurable

  /** Relying parties that we serve. */
  private final RelyingPartyRepository rpRepository;

  /** The authentication provider that is the "manager" for this authentication. */
  private final BankIdAuthenticationProvider provider;

  /** For loading session data. */
  private final BankIdSessionReader sessionReader;

  /** For publishing events. */
  private final BankIdEventPublisher eventPublisher;

  /**
   * Method for completing the operation and returning to the SAML engine.
   *
   * @param request the HTTP servlet request
   * @return a {@link ModelAndView}
   */
  @GetMapping("/view/complete")
  public ModelAndView complete(final HttpServletRequest request) {
    final CollectResponse data = this.sessionReader.loadCompletionData(request);
    final Saml2UserAuthenticationInputToken authnInputToken = this.getInputToken(request).getAuthnInputToken();
    final String entityId = authnInputToken.getAuthnRequestToken().getEntityId();
    final RelyingPartyData relyingParty = Optional.ofNullable(this.rpRepository.getRelyingParty(entityId))
        .orElseThrow(() -> new NoSuchRelyingPartyException(entityId));
    this.eventPublisher.orderCompletion(request, relyingParty).publish();
    return this.complete(request, new BankIdAuthenticationToken(data));
  }

  /**
   * Method for completing the operation and returning to the SAML engine in cases of failed authentication.
   *
   * @param request the HTTP servlet request
   * @return a {@link ModelAndView}
   */
  @GetMapping("/view/error")
  public ModelAndView completeWithError(final HttpServletRequest request) {
    eventPublisher.abortAuthEvent(request).publish();
    return this.complete(request, new Saml2ErrorStatusException(Saml2ErrorStatus.AUTHN_FAILED));
  }

  /**
   * Method for completing the operation and returning to the SAML engine in cases of cancelled operation.
   *
   * @param request the HTTP servlet request
   * @return a {@link ModelAndView}
   */
  @GetMapping("/view/cancel")
  public ModelAndView cancelView(final HttpServletRequest request) {
    eventPublisher.abortAuthEvent(request).publish();
    return this.complete(request, new Saml2ErrorStatusException(Saml2ErrorStatus.CANCEL));
  }

  /** {@inheritDoc} */
  @Override
  protected BankIdAuthenticationProvider getProvider() {
    return this.provider;
  }

}
