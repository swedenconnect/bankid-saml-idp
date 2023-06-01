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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.idp.BankIdService;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.context.BankIdState;
import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionReader;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.config.UiConfigurationProperties.Language;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyRepository;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.QRGenerator;
import se.swedenconnect.bankid.rpapi.types.CompletionData;
import se.swedenconnect.opensaml.sweid.saml2.attribute.AttributeConstants;
import se.swedenconnect.opensaml.sweid.saml2.metadata.entitycategory.EntityCategoryConstants;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.AbstractAuthenticationController;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatus;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatusException;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The controller to which the Spring Security SAML IdP flow directs the user to initiate BankID
 * authentication/signature.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@RestController
@Slf4j
@AllArgsConstructor
public class BankIdAuthenticationController extends AbstractAuthenticationController<BankIdAuthenticationProvider> {

  /**
   * The path to where the Spring Security SAML IdP flow directs the user agent to.
   */
  public static final String AUTHN_PATH = "/bankid";

  /**
   * The session attribute where we store whether we selected "this device" or "other device".
   */
  private static final String PREVIOUS_DEVICE_SESSION_ATTRIBUTE =
      BankIdAuthenticationController.class.getPackageName() + ".DeviceSelection";

  /**
   * Relying parties that we serve.
   */
  private final RelyingPartyRepository rpRepository;

  /**
   * The authentication provider that is the "manager" for this authentication.
   */

  private final BankIdAuthenticationProvider provider;

  /**
   * Possible languages for the UI.
   */
  private final List<Language> languages;


  private final BankIdSessionReader sessionReader;

  private final BankIdEventPublisher eventPublisher;

  private final BankIdService service;

  /**
   * The entry point for the BankID authentication/signature process.
   *
   * @param request  the HTTP servlet request
   * @param response the HTTP servlet response
   * @return a {@link ModelAndView}
   */
  @GetMapping(AUTHN_PATH)
  public ModelAndView view() {
    return new ModelAndView("index");
  }


  @GetMapping("/api/poll") // TODO: 2023-05-23 POST
  public Mono<PollResponse> poll(final HttpServletRequest request, @RequestParam(value = "qr", defaultValue = "false") Boolean qr) {
    BankIdSessionState state = sessionReader.loadSessionData(request);
    Saml2UserAuthenticationInputToken authnInputToken = this.getInputToken(request).getAuthnInputToken();
    BankIdContext bankIdContext = this.buildInitialContext(authnInputToken, request);
    final RelyingPartyData relyingParty = this.getRelyingParty(authnInputToken.getAuthnRequestToken().getEntityId());
    BankIDClient client = relyingParty.getClient();
    return service.poll(request, qr, state, authnInputToken, bankIdContext, client);
  }

  @GetMapping("/view/complete")
  public ModelAndView complete(final HttpServletRequest request) {
    CompletionData data = (CompletionData) request.getSession().getAttribute("BANKID-COMPLETION-DATA");
    eventPublisher.orderCompletion(request).publish();
    return complete(request, new BankIdAuthenticationToken(data));
  }

  @GetMapping("/view/cancel")
  public ModelAndView cancelView(final HttpServletRequest request) {
    return complete(request, new Saml2ErrorStatusException(Saml2ErrorStatus.CANCEL));
  }

  @GetMapping("/api/cancel") // TODO: 2023-05-29 post
  public Mono<Void> cancelRequest(HttpServletRequest request) {
    BankIdSessionState state = sessionReader.loadSessionData(request);
    BankIdSessionData bankIdSessionData = state.getBankIdSessionData();
    if (Objects.nonNull(bankIdSessionData)) {
      final RelyingPartyData relyingParty = this.getRelyingParty(getInputToken(request).getAuthnInputToken().getAuthnRequestToken().getEntityId());
      BankIDClient client = relyingParty.getClient();
      eventPublisher.orderCancellation(request).publish();
      return client.cancel(state.getBankIdSessionData().getOrderReference());
    }
    return Mono.empty();
  }

  /**
   * Updates the MVC model with common attributes such as possible languages.
   *
   * @param model the model
   */
  @ModelAttribute
  public void updateModel(final Model model) {
    final Locale locale = LocaleContextHolder.getLocale();

    model.addAttribute("languages", this.languages.stream()
        .filter(lang -> !lang.getTag().equals(locale.getLanguage()))
        .collect(Collectors.toList()));
  }

  /**
   * When a SAML {@code AuthnRequest} is received we set up an initial {@link BankIdContext}.
   *
   * @param token   the input token
   * @param request the HTTP servlet request
   * @return a {@link BankIdContext}
   */
  private BankIdContext buildInitialContext(final Saml2UserAuthenticationInputToken token, final HttpServletRequest request) {

    final BankIdContext context = new BankIdContext();
    context.setState(BankIdState.INIT);
    context.setClientId(token.getAuthnRequestToken().getEntityId());

    // Authentication or signature?
    //
    final boolean signService = token.getAuthnRequirements().getEntityCategories()
        .contains(EntityCategoryConstants.SERVICE_TYPE_CATEGORY_SIGSERVICE.getUri());
    context.setOperation(signService ? BankIdOperation.SIGN : BankIdOperation.AUTH);

    // Do we have a personal identity number given in the request?
    //
    final String personalIdNumber = token.getAuthnRequirements().getPrincipalSelectionAttributes().stream()
        .filter(u -> AttributeConstants.ATTRIBUTE_NAME_PERSONAL_IDENTITY_NUMBER.equals(u.getId()))
        .filter(u -> !u.getValues().isEmpty())
        .map(u -> u.getValues().get(0))
        .map(String.class::cast)
        .findFirst()
        .orElse(null);
    context.setPersonalNumber(personalIdNumber);

    // Device selection
    //
    final PreviousDeviceSelection previousDeviceSelection =
        Optional.ofNullable(request.getSession().getAttribute(PREVIOUS_DEVICE_SESSION_ATTRIBUTE))
            .map(String.class::cast)
            .map(d -> {
              try {
                return PreviousDeviceSelection.forValue(d);
              } catch (final IllegalArgumentException e) {
                return null;
              }
            })
            .orElse(null);
    context.setPreviousDeviceSelection(previousDeviceSelection);

    return context;
  }

  private RelyingPartyData getRelyingParty(final String entityId) {
    final RelyingPartyData rp = this.rpRepository.getRelyingParty(entityId);
    if (rp == null) {
      log.info("SAML SP '{}' is not a registered BankID Relying Party", entityId);
      throw new RuntimeException("Not registered"); // TODO: handle
    }
    return rp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected BankIdAuthenticationProvider getProvider() {
    return this.provider;
  }

}