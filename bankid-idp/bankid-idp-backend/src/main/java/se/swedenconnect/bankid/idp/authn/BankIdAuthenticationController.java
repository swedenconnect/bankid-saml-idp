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
import org.opensaml.core.xml.LangBearing;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSURIImpl;
import org.opensaml.saml.ext.saml2mdui.impl.LogoImpl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.idp.ApiResponseFactory;
import se.swedenconnect.bankid.idp.NoSuchRelyingPartyException;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.context.BankIdState;
import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.service.BankIdService;
import se.swedenconnect.bankid.idp.authn.service.PollRequest;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionReader;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.config.UiConfigurationProperties.Language;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyRepository;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;
import se.swedenconnect.opensaml.sweid.saml2.attribute.AttributeConstants;
import se.swedenconnect.opensaml.sweid.saml2.metadata.entitycategory.EntityCategoryConstants;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.AbstractAuthenticationController;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatus;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatusException;
import se.swedenconnect.spring.saml.idp.response.Saml2ResponseAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
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
   * @return a {@link ModelAndView}
   */
  @GetMapping(AUTHN_PATH)
  public ModelAndView view() {
    return new ModelAndView("index");
  }

  @GetMapping("/api/device")
  public Mono<SelectedDeviceInformation> getSelectedDevice(final HttpServletRequest request) {
    final Saml2UserAuthenticationInputToken authnInputToken = this.getInputToken(request).getAuthnInputToken();
    final BankIdContext bankIdContext = buildInitialContext(authnInputToken, request);
    final boolean sign = bankIdContext.getOperation().equals(BankIdOperation.SIGN);
    PreviousDeviceSelection previousDeviceSelection = bankIdContext.getPreviousDeviceSelection();
    if (previousDeviceSelection == null) {
      log.warn("Failed to find previous selected device for user");
      previousDeviceSelection = PreviousDeviceSelection.UNKNOWN;
    }
    return Mono.just(new SelectedDeviceInformation(sign, previousDeviceSelection.getValue()));
  }

  @PostMapping("/api/poll")
  public Mono<ApiResponse> poll(final HttpServletRequest request, @RequestParam(value = "qr", defaultValue = "false") final Boolean qr) {
    final BankIdSessionState state = sessionReader.loadSessionData(request);
    final Saml2UserAuthenticationInputToken authnInputToken = this.getInputToken(request).getAuthnInputToken();
    final BankIdContext bankIdContext = this.buildInitialContext(authnInputToken, request);
    final RelyingPartyData relyingParty = this.getRelyingParty(authnInputToken.getAuthnRequestToken().getEntityId());
    final BankIDClient client = relyingParty.getClient();
    if (state != null && state.getBankIdSessionData().getStatus().equals(ProgressStatus.COMPLETE)) {
      return Mono.just(ApiResponseFactory.create(state.getBankIdSessionData(), client.getQRGenerator(), qr));
    }
    final PollRequest pollRequest = PollRequest.builder()
        .request(request)
        .relyingPartyData(relyingParty)
        .qr(qr)
        .context(bankIdContext)
        .data(this.getMessage(request, bankIdContext, authnInputToken, relyingParty))
        .state(state)
        .build();
    return service.poll(pollRequest);
  }


  private BankIdContext getBankIdContext(final Saml2UserAuthenticationInputToken token, final HttpServletRequest request) {
    final BankIdContext bankIdContext = this.buildInitialContext(token, request);
    final BankIdContext context = sessionReader.loadContext(request);
    return bankIdContext;
  }

  /**
   * Lazy load of message, if no message is set, it is calculated and published to be persisted
   *
   * @param request      Current http servlet
   * @param context      Current bankid context
   * @param token        Current SAML token
   * @param relyingParty Relaying party who wants the authentication
   * @return Message to be displayed in app
   */
  private UserVisibleData getMessage(final HttpServletRequest request, final BankIdContext context, final Saml2UserAuthenticationInputToken token,
                                     final RelyingPartyData relyingParty) {
    return Optional.ofNullable(sessionReader.loadUserVisibleData(request))
        .orElseGet(() -> {
          final UserVisibleData userVisibleData = UserVisibleDataFactory.constructMessage(context, token, relyingParty);
          eventPublisher.userVisibleData(userVisibleData, request).publish();
          return userVisibleData;
        });
  }

  @PostMapping("/api/cancel")
  public Mono<Void> cancelRequest(final HttpServletRequest request) {
    final BankIdSessionState state = sessionReader.loadSessionData(request);
    final BankIdSessionData bankIdSessionData = state.getBankIdSessionData();
    if (Objects.nonNull(bankIdSessionData)) {
      final Saml2UserAuthenticationInputToken authnInputToken = getInputToken(request).getAuthnInputToken();
      final String entityId = authnInputToken.getAuthnRequestToken().getEntityId();
      final RelyingPartyData relyingParty = this.getRelyingParty(entityId);
      return service.cancel(request, state, relyingParty);
    }
    return Mono.empty();
  }

  @GetMapping("/view/complete")
  public ModelAndView complete(final HttpServletRequest request) {
    final CollectResponse data = sessionReader.laodCompletionData(request);
    final Saml2UserAuthenticationInputToken authnInputToken = getInputToken(request).getAuthnInputToken();
    final String entityId = authnInputToken.getAuthnRequestToken().getEntityId();
    final RelyingPartyData relyingParty = getRelyingParty(entityId);
    eventPublisher.orderCompletion(request, relyingParty).publish();
    return complete(request, new BankIdAuthenticationToken(data));
  }

  @GetMapping("/view/cancel")
  public ModelAndView cancelView(final HttpServletRequest request) {
    return complete(request, new Saml2ErrorStatusException(Saml2ErrorStatus.CANCEL));
  }

  @GetMapping(value = "/api/sp", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<SpInformation> spInformation(final HttpServletRequest request) {
    final Saml2ResponseAttributes attribute = (Saml2ResponseAttributes) request.getSession()
        .getAttribute("se.swedenconnect.spring.saml.idp.web.filters.ResponseAttributes");
    final Map<String, String> displayNames = attribute.getPeerMetadata().getOrganization().getDisplayNames()
        .stream()
        .filter(v -> v.getXMLLang() != null && v.getValue() != null)
        .collect(Collectors.toMap(LangBearing::getXMLLang, XSString::getValue));
    final List<LogoImpl> images = Optional.ofNullable(attribute.getPeerMetadata().getRoleDescriptors().get(0))
        .flatMap(d -> Optional.ofNullable(d.getExtensions().getUnknownXMLObjects().get(0)))
        .flatMap(e -> Optional.ofNullable(e.getOrderedChildren()))
        .map(c -> c.stream()
            .filter(x -> x instanceof LogoImpl)
            .map(LogoImpl.class::cast)
            .toList())
        .orElseGet(List::of);
    final SpInformation data =
        new SpInformation(displayNames, Optional.ofNullable(images.get(0)).map(XSURIImpl::getURI).orElse(""));
    return Mono.just(data);
  }

  /**
   * When a SAML {@code AuthnRequest} is received we set up an initial {@link BankIdContext}.
   *
   * @param token   the input token
   * @param request the HTTP servlet request
   * @return a {@link BankIdContext}
   */
  private BankIdContext buildInitialContext(final Saml2UserAuthenticationInputToken token,
                                            final HttpServletRequest request) {

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
    final PreviousDeviceSelection previousDeviceSelection = sessionReader.loadPreviousSelectedDevice(request);
    context.setPreviousDeviceSelection(previousDeviceSelection);
    return context;
  }

  private RelyingPartyData getRelyingParty(final String entityId) {
    final RelyingPartyData rp = this.rpRepository.getRelyingParty(entityId);
    if (rp == null) {
      log.info("SAML SP '{}' is not a registered BankID Relying Party", entityId);
      throw new NoSuchRelyingPartyException();
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
