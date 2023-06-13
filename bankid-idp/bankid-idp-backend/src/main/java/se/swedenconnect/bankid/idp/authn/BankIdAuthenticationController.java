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

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.idp.NoSuchRelyingPartyException;
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
import se.swedenconnect.bankid.rpapi.service.DataToSign;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.opensaml.sweid.saml2.attribute.AttributeConstants;
import se.swedenconnect.opensaml.sweid.saml2.metadata.entitycategory.EntityCategoryConstants;
import se.swedenconnect.opensaml.sweid.saml2.signservice.dss.SignMessageMimeTypeEnum;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.AbstractAuthenticationController;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatus;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatusException;
import se.swedenconnect.spring.saml.idp.response.Saml2ResponseAttributes;

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
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @return a {@link ModelAndView}
   */
  @GetMapping(AUTHN_PATH)
  public ModelAndView view() {
    return new ModelAndView("index");
  }

  @GetMapping("/api/device")
  public Mono<SelectedDeviceInformation> getSelectedDevice(HttpServletRequest request) {
    Saml2UserAuthenticationInputToken authnInputToken = this.getInputToken(request).getAuthnInputToken();
    boolean sign = authnInputToken.getAuthnRequirements().getEntityCategories().contains("http://id.elegnamnden.se/st/1.0/sigservice");
    return Mono.just(new SelectedDeviceInformation(sign, SelectedDeviceInformation.SignDevice.SAME));
  }

  @PostMapping("/api/poll")
  public Mono<ApiResponse> poll(final HttpServletRequest request,
      @RequestParam(value = "qr", defaultValue = "false") Boolean qr) {
    BankIdSessionState state = sessionReader.loadSessionData(request);
    Saml2UserAuthenticationInputToken authnInputToken = this.getInputToken(request).getAuthnInputToken();
    BankIdContext bankIdContext = this.buildInitialContext(authnInputToken, request);
    final RelyingPartyData relyingParty = this.getRelyingParty(authnInputToken.getAuthnRequestToken().getEntityId());
    BankIDClient client = relyingParty.getClient();
    return service.poll(request, qr, state, authnInputToken, bankIdContext, client,
        getMessage(bankIdContext, authnInputToken, relyingParty));
  }

  // TODO: Wouldn't it be better if the message was calculated once and assigned to the context?
  //
  private UserVisibleData getMessage(final BankIdContext context, final Saml2UserAuthenticationInputToken token,
      final RelyingPartyData relyingParty) {
    if (context.getOperation() == BankIdOperation.SIGN) {
      final DataToSign message = new DataToSign();
      if (token.getAuthnRequirements().getSignatureMessageExtension() != null) {
        message.setUserVisibleData(token.getAuthnRequirements().getSignatureMessageExtension().getMessage());
        if (SignMessageMimeTypeEnum.TEXT_MARKDOWN
            .equals(token.getAuthnRequirements().getSignatureMessageExtension().getMimeType())) {
          message.setUserVisibleDataFormat(UserVisibleData.VISIBLE_DATA_FORMAT_SIMPLE_MARKDOWN_V1);
        }
      }
      else {
        message.setDisplayText(relyingParty.getFallbackSignText().getText());
        if (DisplayText.TextFormat.SIMPLE_MARKDOWN_V1.equals(relyingParty.getFallbackSignText().getFormat())) {
          message.setUserVisibleDataFormat(UserVisibleData.VISIBLE_DATA_FORMAT_SIMPLE_MARKDOWN_V1);
        }
      }
      // TODO: Build userNonVisibleData according to 4.2.1.2 of "Implementation Profile for BankID Identity Providers
      // within the Swedish eID Framework".
      //
      message.setUserNonVisibleData(Base64.getEncoder().encodeToString("TODO".getBytes()));
      
      return message;
    }
    else {
      if (relyingParty.getLoginText() == null) {
        return null;
      }
      final UserVisibleData message = new UserVisibleData();
      message.setDisplayText(relyingParty.getLoginText().getText());
      if (DisplayText.TextFormat.SIMPLE_MARKDOWN_V1.equals(relyingParty.getLoginText().getFormat())) {
        message.setUserVisibleDataFormat(UserVisibleData.VISIBLE_DATA_FORMAT_SIMPLE_MARKDOWN_V1);
      }
      return message;
    }
  }

  @PostMapping("/api/cancel")
  public Mono<Void> cancelRequest(HttpServletRequest request) {
    BankIdSessionState state = sessionReader.loadSessionData(request);
    BankIdSessionData bankIdSessionData = state.getBankIdSessionData();
    if (Objects.nonNull(bankIdSessionData)) {
      Saml2UserAuthenticationInputToken authnInputToken = getInputToken(request).getAuthnInputToken();
      String entityId = authnInputToken.getAuthnRequestToken().getEntityId();
      BankIDClient client = this.getRelyingParty(entityId).getClient();
      return service.cancel(request, state, client);
    }
    return Mono.empty();
  }

  @GetMapping("/view/complete")
  public ModelAndView complete(final HttpServletRequest request) {
    CollectResponse data = (CollectResponse) request.getSession().getAttribute("BANKID-COMPLETION-DATA");
    eventPublisher.orderCompletion(request).publish();
    return complete(request, new BankIdAuthenticationToken(data));
  }

  @GetMapping("/view/cancel")
  public ModelAndView cancelView(final HttpServletRequest request) {
    return complete(request, new Saml2ErrorStatusException(Saml2ErrorStatus.CANCEL));
  }

  @GetMapping(value = "/api/sp", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<SpInformation> spInformation(final HttpServletRequest request) {
    Saml2ResponseAttributes attribute = (Saml2ResponseAttributes) request.getSession()
        .getAttribute("se.swedenconnect.spring.saml.idp.web.filters.ResponseAttributes");
    Map<String, String> displayNames = attribute.getPeerMetadata().getOrganization().getDisplayNames()
        .stream()
        .filter(v -> v.getXMLLang() != null && v.getValue() != null)
        .collect(Collectors.toMap(LangBearing::getXMLLang, XSString::getValue));
    List<LogoImpl> images = Optional.ofNullable(attribute.getPeerMetadata().getRoleDescriptors().get(0))
        .flatMap(d -> Optional.ofNullable(d.getExtensions().getUnknownXMLObjects().get(0)))
        .flatMap(e -> Optional.ofNullable(e.getOrderedChildren()))
        .map(c -> c.stream()
            .filter(x -> x instanceof LogoImpl)
            .map(LogoImpl.class::cast)
            .toList())
        .orElseGet(List::of);
    SpInformation data =
        new SpInformation(displayNames, Optional.ofNullable(images.get(0)).map(XSURIImpl::getURI).orElse(""));
    return Mono.just(data);
  }

  /**
   * When a SAML {@code AuthnRequest} is received we set up an initial {@link BankIdContext}.
   *
   * @param token the input token
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
    final PreviousDeviceSelection previousDeviceSelection =
        Optional.ofNullable(request.getSession().getAttribute(PREVIOUS_DEVICE_SESSION_ATTRIBUTE))
            .map(String.class::cast)
            .map(d -> {
              try {
                return PreviousDeviceSelection.forValue(d);
              }
              catch (final IllegalArgumentException e) {
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
