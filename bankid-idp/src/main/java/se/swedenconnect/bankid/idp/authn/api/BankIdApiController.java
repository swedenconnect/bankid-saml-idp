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
package se.swedenconnect.bankid.idp.authn.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.idp.authn.BankIdAuthenticationProvider;
import se.swedenconnect.bankid.idp.authn.UserVisibleDataFactory;
import se.swedenconnect.bankid.idp.authn.annotations.ApiController;
import se.swedenconnect.bankid.idp.authn.api.overrides.FrontendOverrideResponse;
import se.swedenconnect.bankid.idp.authn.api.overrides.OverrideService;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.idp.authn.error.BankIdSecurityViolationException;
import se.swedenconnect.bankid.idp.authn.error.NoSuchRelyingPartyException;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.service.BankIdService;
import se.swedenconnect.bankid.idp.authn.service.PollRequest;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionReader;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyRepository;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.service.impl.BankIdServerException;
import se.swedenconnect.bankid.rpapi.service.impl.BankIdUserException;
import se.swedenconnect.bankid.rpapi.types.BankIDException;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;
import se.swedenconnect.opensaml.sweid.saml2.attribute.AttributeConstants;
import se.swedenconnect.opensaml.sweid.saml2.metadata.entitycategory.EntityCategoryConstants;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.RedirectForAuthenticationToken;
import se.swedenconnect.spring.saml.idp.error.UnrecoverableSaml2IdpError;
import se.swedenconnect.spring.saml.idp.error.UnrecoverableSaml2IdpException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for the BankID backend API.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@RestController
@ApiController
@Slf4j
public class BankIdApiController {

  /** Relying parties that we serve. */
  private final RelyingPartyRepository rpRepository;

  /** The authentication provider that is the "manager" for this authentication. */
  private final BankIdAuthenticationProvider provider;

  /** For loading session data. */
  private final BankIdSessionReader sessionReader;

  /** For publishing events. */
  private final BankIdEventPublisher eventPublisher;

  /** The BankID service that communicates with the BankID server. */
  private final BankIdService service;

  /** Factory for handling customer contacts in the UI. */
  private final CustomerContactInformationFactory customerContactInformationFactory;

  /** Service for generating overrides for front-end content. */
  private final OverrideService overrides;

  /** Provides UI information to the frontend. */
  private final UiInformationProvider uiInformation;

  /**
   * Constructor.
   *
   * @param rpRepository the relying parties that we serve
   * @param provider the authentication provider that is the "manager" for this authentication
   * @param sessionReader for loading session data
   * @param eventPublisher for publishing events
   * @param service the BankID service that communicates with the BankID server
   * @param customerContactInformationFactory factory for handling customer contacts in the UI
   * @param overrides service for generating overrides for front-end content
   * @param uiInformation provides UI information to the frontend
   */
  public BankIdApiController(final RelyingPartyRepository rpRepository, final BankIdAuthenticationProvider provider,
      final BankIdSessionReader sessionReader, final BankIdEventPublisher eventPublisher, final BankIdService service,
      final CustomerContactInformationFactory customerContactInformationFactory, final OverrideService overrides,
      final UiInformationProvider uiInformation) {
    this.rpRepository = rpRepository;
    this.provider = provider;
    this.sessionReader = sessionReader;
    this.eventPublisher = eventPublisher;
    this.service = service;
    this.customerContactInformationFactory = customerContactInformationFactory;
    this.overrides = overrides;
    this.uiInformation = uiInformation;
  }

  /**
   * Gets information about the selected device.
   *
   * @param request the HTTP servlet request
   * @return selected device information
   */
  @GetMapping("/api/device")
  public Mono<SelectedDeviceInformation> getSelectedDevice(final HttpServletRequest request) {
    final BankIdContext bankIdContext = this.getContext(request);
    final boolean sign = bankIdContext.getOperation() == BankIdOperation.SIGN;
    PreviousDeviceSelection previousDeviceSelection = bankIdContext.getPreviousDeviceSelection();
    if (previousDeviceSelection == null) {
      previousDeviceSelection = PreviousDeviceSelection.UNKNOWN;
    }
    log.debug("Previously selected device: {}", previousDeviceSelection);
    return Mono.just(new SelectedDeviceInformation(sign, previousDeviceSelection.getValue()));
  }

  /**
   * API method for making a BankID polling request.
   *
   * @param request the HTTP servlet request
   * @param apiRequest the API request body
   * @return an {@link ApiResponse}
   */
  @PostMapping("/api/poll")
  public Mono<ApiResponse> poll(final HttpServletRequest request,
      @RequestBody final ApiRequest apiRequest) {

    final BankIdSessionState state = this.sessionReader.loadSessionData(request);
    final BankIdContext bankIdContext = this.getContext(request);
    final RelyingPartyData relyingParty = this.getRelyingParty(bankIdContext.getClientId());
    final BankIDClient client = relyingParty.getClient();
    if (state != null && state.getBankIdSessionData().getStatus() == ProgressStatus.COMPLETE) {

      final BankIdSessionData sessionData = state.getBankIdSessionData();

      // This covers the special case where autostart was used (including a return URL and nonce),
      // the app was started, AND, the user managed to complete the operation before the first
      // poll request from the frontend ...
      //
      if (apiRequest.getNonce() != null) {
        sessionData.setReceivedNonce(apiRequest.getNonce());
      }

      return Mono.just(
          ApiResponseFactory.create(sessionData, client.getQRGenerator(), apiRequest.getDisplayQr()));
    }
    else {
      final PollRequest pollRequest = PollRequest.builder()
          .request(request)
          .relyingPartyData(relyingParty)
          .qr(apiRequest.getDisplayQr())
          .autoStartWithReturnUrl(apiRequest.getAutoStartWithReturnUrl())
          .context(bankIdContext)
          .data(this.getMessage(request, bankIdContext, relyingParty))
          .state(state)
          .receivedNonce(apiRequest.getNonce())
          .build();
      return this.service.poll(pollRequest)
          // TODO: or return error to SP only?
//          .onErrorResume(BankIdSecurityViolationException.class::isInstance,
//              e -> Mono.just(ApiResponseFactory.createErrorSecurityViolation()))
          .onErrorResume(BankIdServerException.class::isInstance,
              e -> Mono.just(ApiResponseFactory.createErrorResponseBankIdServerException()))
          .onErrorResume(e -> e instanceof BankIdUserException
                  && ((BankIdUserException) e).getErrorCode() == ErrorCode.INVALID_PARAMETERS,
              e -> Mono.just(ApiResponseFactory.createUnknownError()))
          .onErrorResume(BankIDException.class::isInstance,
              e -> Mono.just(ApiResponseFactory.createErrorResponseTimeExpired()));
    }
  }

  /**
   * Gets the {@link FrontendOverrideResponse} telling the front-end about customizations.
   *
   * @return {@link FrontendOverrideResponse}
   */
  @GetMapping("/api/overrides")
  public Mono<FrontendOverrideResponse> getFrontendOverrides() {
    return Mono.just(this.overrides.generateOverrides());
  }

  /**
   * Gets the provider logotype to be displayed.
   *
   * @return SVG image as bytes
   * @throws IOException see {@link IOUtils#toByteArray(InputStream)}
   */
  @GetMapping(value = "/logo.svg", produces = "image/svg+xml")
  @ResponseBody
  public byte[] getProviderLogotype() throws IOException {
    return this.uiInformation.getProviderLogo();
  }

  /**
   * Gets the provider SVG favicon to be displayed.
   *
   * @return SVG image as bytes
   * @throws IOException see {@link IOUtils#toByteArray(InputStream)}
   */
  @GetMapping(value = "/favicon.svg", produces = "image/svg+xml")
  @ResponseBody
  public byte[] getProviderSvgFavicon() throws IOException {
    return this.uiInformation.getProviderSvgFavicon();
  }

  /**
   * Gets the provider PNG favicon to be displayed.
   *
   * @return PNG image as bytes
   * @throws IOException see {@link IOUtils#toByteArray(InputStream)}
   */
  @GetMapping(value = "/favicon.png", produces = "image/png")
  @ResponseBody
  public byte[] getProviderPngFavicon() throws IOException {
    return this.uiInformation.getProviderPngFavicon();
  }

  /**
   * Lazy load of message, if no message is set, it is calculated and published to be persisted.
   *
   * @param request current http servlet
   * @param context current bankid context
   * @param relyingParty relaying party who wants the authentication
   * @return message to be displayed in app or {@code null} if none is available
   */
  private UserVisibleData getMessage(final HttpServletRequest request, final BankIdContext context,
      final RelyingPartyData relyingParty) {
    return Optional.ofNullable(this.sessionReader.loadUserVisibleData(request))
        .orElseGet(() -> {
          final UserVisibleData userVisibleData = UserVisibleDataFactory.constructMessage(context, relyingParty);
          if (userVisibleData != null) {
            this.eventPublisher.userVisibleData(request, userVisibleData).publish();
          }
          return userVisibleData;
        });
  }

  /**
   * API method for cancelling a request
   *
   * @param request the HTTP servlet request
   * @return nothing
   */
  @PostMapping("/api/cancel")
  public Mono<Void> cancelRequest(final HttpServletRequest request) {
    final BankIdSessionState state = this.sessionReader.loadSessionData(request);
    if (Objects.nonNull(state) && Objects.nonNull(state.getBankIdSessionData())) {
      final Saml2UserAuthenticationInputToken authnInputToken = this.getInputToken(request);
      final RelyingPartyData relyingParty = this.getRelyingParty(authnInputToken.getAuthnRequestToken().getEntityId());
      return this.service.cancel(request, state, relyingParty);
    }
    else {
      return Mono.empty();
    }
  }

  /**
   * API method for delivering status information.
   *
   * @return the service information
   */
  @GetMapping(value = "/api/status")
  public Mono<ServiceInformation> serviceInformation() {
    return this.service.getServiceInformation();
  }

  /**
   * API method for delivering UI display information.
   *
   * @param request the HTTP servlet request
   * @return UI information
   */
  @GetMapping(value = "api/ui", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<UiInformation> uiInformation(final HttpServletRequest request) {

    final Saml2UserAuthenticationInputToken authnInputToken = this.getInputToken(request);
    final RelyingPartyData relyingParty = this.getRelyingParty(authnInputToken.getAuthnRequestToken().getEntityId());

    return Mono.just(this.uiInformation.getUiInformation(this.getInputToken(request).getUiInfo(), relyingParty));
  }

  /**
   * API method for delivering contact information for customers.
   *
   * @return a {@link CustomerContactInformation}
   */
  @GetMapping(value = "/api/contact", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<CustomerContactInformation> customerContactInormationMono() {
    return Mono.just(this.customerContactInformationFactory.getContactInformation());
  }

  /**
   * Gets the {@link Saml2UserAuthenticationInputToken} for the current operation.
   *
   * @param request the HTTP servlet request
   * @return a {@link Saml2UserAuthenticationInputToken}
   */
  private Saml2UserAuthenticationInputToken getInputToken(final HttpServletRequest request) {
    return Optional.ofNullable(this.provider.getTokenRepository().getExternalAuthenticationToken(request))
        .map(RedirectForAuthenticationToken::getAuthnInputToken)
        .orElseThrow(() -> new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.INVALID_SESSION,
            "No input token available", null));
  }

  /**
   * Creates a {@link BankIdContext}.
   *
   * @param request the HTTP servlet request
   * @return a {@link BankIdContext}
   */
  private BankIdContext getContext(final HttpServletRequest request) {

    final Saml2UserAuthenticationInputToken token = this.getInputToken(request);

    final BankIdContext context = new BankIdContext();
    context.setId(token.getAuthnRequestToken().getAuthnRequest().getID());
    context.setClientId(token.getAuthnRequestToken().getEntityId());

    // Authentication or signature?
    //
    final boolean signService = token.getAuthnRequirements().getEntityCategories()
        .contains(EntityCategoryConstants.SERVICE_TYPE_CATEGORY_SIGSERVICE.getUri());
    context.setOperation(signService ? BankIdOperation.SIGN : BankIdOperation.AUTH);

    if (signService) {
      context.setSignMessage(token.getAuthnRequirements().getSignatureMessageExtension());
    }

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
    final PreviousDeviceSelection previousDeviceSelection = this.sessionReader.loadPreviousSelectedDevice(request);
    context.setPreviousDeviceSelection(previousDeviceSelection);
    return context;
  }

  /**
   * Gets the {@link RelyingPartyData} matching the SAML entityID of the calling entity.
   *
   * @param entityId the SAML entityID
   * @return the {@link RelyingPartyData}
   */
  private RelyingPartyData getRelyingParty(final String entityId) {
    final RelyingPartyData rp = this.rpRepository.getRelyingParty(entityId);
    if (rp == null) {
      log.info("SAML SP '{}' is not a registered BankID Relying Party", entityId);
      throw new NoSuchRelyingPartyException(entityId);
    }
    return rp;
  }

}
