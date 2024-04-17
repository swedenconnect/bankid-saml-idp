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
package se.swedenconnect.bankid.idp.integration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.security.credential.UsageType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.Setter;
import se.swedenconnect.opensaml.common.utils.LocalizedString;
import se.swedenconnect.opensaml.saml2.metadata.build.AssertionConsumerServiceBuilder;
import se.swedenconnect.opensaml.saml2.metadata.build.AttributeConsumingServiceBuilder;
import se.swedenconnect.opensaml.saml2.metadata.build.EntityAttributesBuilder;
import se.swedenconnect.opensaml.saml2.metadata.build.EntityDescriptorBuilder;
import se.swedenconnect.opensaml.saml2.metadata.build.ExtensionsBuilder;
import se.swedenconnect.opensaml.saml2.metadata.build.KeyDescriptorBuilder;
import se.swedenconnect.opensaml.saml2.metadata.build.SPSSODescriptorBuilder;
import se.swedenconnect.opensaml.saml2.request.AuthnRequestGenerator;
import se.swedenconnect.opensaml.saml2.request.AuthnRequestGeneratorContext;
import se.swedenconnect.opensaml.saml2.request.RequestHttpObject;
import se.swedenconnect.opensaml.saml2.response.ResponseProcessingException;
import se.swedenconnect.opensaml.saml2.response.ResponseProcessingInput;
import se.swedenconnect.opensaml.saml2.response.ResponseProcessingResult;
import se.swedenconnect.opensaml.saml2.response.ResponseProcessor;
import se.swedenconnect.opensaml.saml2.response.ResponseProcessorImpl;
import se.swedenconnect.opensaml.saml2.response.ResponseStatusErrorException;
import se.swedenconnect.opensaml.saml2.response.replay.InMemoryReplayChecker;
import se.swedenconnect.opensaml.sweid.saml2.request.SwedishEidAuthnRequestGenerator;
import se.swedenconnect.opensaml.sweid.saml2.signservice.SignMessageEncrypter;
import se.swedenconnect.opensaml.xmlsec.encryption.support.SAMLObjectDecrypter;
import se.swedenconnect.opensaml.xmlsec.encryption.support.SAMLObjectEncrypter;
import se.swedenconnect.security.credential.KeyStoreCredential;
import se.swedenconnect.security.credential.PkiCredential;
import se.swedenconnect.security.credential.opensaml.OpenSamlCredential;

public class TestSp {

  public static final String ENTITY_ID = "https://demo.swedenconnect.se/sp";
  public static final String SIGN_ID = "https://demo.swedenconnect.se/sp/sign";

  private final boolean signMode;
  public static final String ACS = "https://demo.swedenconnect.se/sp/sso";

  private PkiCredential spSignCredential;
  private PkiCredential spEncryptCredential;

  private ResponseProcessor responseProcessor;

  private AuthnRequest authnRequest;
  private String relayState;

  @Setter
  private boolean wantsAssertionsSigned = false;

  @Setter
  private boolean requireEncryptedAssertions = true;

  @Setter
  private List<String> entityCategories;

  private MetadataResolver metadataResolver;

  /**
   * Constructor.
   *
   * @throws Exception for errors
   */
  public TestSp(boolean signMode) throws Exception {
    this.spSignCredential = new KeyStoreCredential(
        new ClassPathResource("test-sp.jks"), "JKS", "secret".toCharArray(), "sign", "secret".toCharArray());
    this.spSignCredential.init();
    this.spEncryptCredential = new KeyStoreCredential(
        new ClassPathResource("test-sp.jks"), "JKS", "secret".toCharArray(), "encrypt", "secret".toCharArray());
    this.spEncryptCredential.init();
    this.signMode = signMode;
  }

  public void setupResponseProcessor(final MetadataResolver metadataResolver) throws Exception {

    final ResponseProcessorImpl p = new ResponseProcessorImpl();
    p.setMetadataResolver(metadataResolver);
    p.setMessageReplayChecker(new InMemoryReplayChecker());
    p.setDecrypter(new SAMLObjectDecrypter(new OpenSamlCredential(this.spEncryptCredential)));
    p.setRequireEncryptedAssertions(this.requireEncryptedAssertions);
    p.initialize();

    this.metadataResolver = metadataResolver;
    this.responseProcessor = p;
  }

  /**
   * Gets the SAML metadata for the SP
   *
   * @return an {@link EntityDescriptor}
   */
  public EntityDescriptor getSpMetadata() {

    final EntityAttributes entityAttributes = this.getEntityAttributes();
    final Extensions extensions = entityAttributes != null
        ? ExtensionsBuilder.builder()
            .extension(entityAttributes)
            .build()
        : null;

    return EntityDescriptorBuilder.builder()
        .entityID(getEntityId())
        .extensions(extensions)
        .roleDescriptors(
            SPSSODescriptorBuilder.builder()
                .wantAssertionsSigned(this.wantsAssertionsSigned)
                .keyDescriptors(
                    KeyDescriptorBuilder.builder()
                        .keyName("sign")
                        .use(UsageType.SIGNING)
                        .certificate(this.spSignCredential.getCertificate())
                        .build(),
                    KeyDescriptorBuilder.builder()
                        .keyName("encrypt")
                        .use(UsageType.ENCRYPTION)
                        .certificate(this.spEncryptCredential.getCertificate())
                        .encryptionMethods("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p",
                            "http://www.w3.org/2009/xmlenc11#aes256-gcm",
                            "http://www.w3.org/2009/xmlenc11#aes192-gcm",
                            "http://www.w3.org/2009/xmlenc11#aes128-gcm")
                        .build())
                .nameIDFormats(NameID.PERSISTENT, NameID.TRANSIENT)
                .assertionConsumerServices(
                    AssertionConsumerServiceBuilder.builder()
                        .binding(SAMLConstants.SAML2_POST_BINDING_URI)
                        .location(ACS)
                        .isDefault(true)
                        .index(0)
                        .build())
                .attributeConsumingServices(
                    AttributeConsumingServiceBuilder.builder()
                        .descriptions(new LocalizedString("Test SP", Locale.ENGLISH))
                        // TODO
                        .build())
                .build())
        .build();
  }

  private EntityAttributes getEntityAttributes() {
    if (this.entityCategories == null || this.entityCategories.isEmpty()) {
      return null;
    }
    return EntityAttributesBuilder.builder()
        .entityCategoriesAttribute(this.entityCategories)
        .build();
  }

  public ResponseProcessingResult processSamlResponse(final MvcResult result)
      throws ResponseStatusErrorException, ResponseProcessingException {

    try {
      final Document html = Jsoup.parse(result.getResponse().getContentAsString());
      final Element formElement = html.getElementsByTag("form").stream()
          .findFirst()
          .orElseThrow(() -> new ResponseProcessingException("Not a POST form"));
      final String destination = formElement.attr("action");
      final String samlResponse = formElement.getElementsByAttributeValue("name", "SAMLResponse").stream()
          .map(e -> e.attr("value"))
          .findFirst()
          .orElseThrow(() -> new ResponseProcessingException("Missing SAMLResponse"));
      final String receivedRelayState = formElement.getElementsByAttributeValue("name", "RelayState").stream()
          .map(e -> e.attr("value"))
          .findFirst()
          .orElse(null);

      return this.responseProcessor.processSamlResponse(samlResponse, receivedRelayState,
          new ResponseProcessingInput() {

            @Override
            public String getRequestRelayState(final String id) {
              return relayState;
            }

            @Override
            public String getReceiveURL() {
              return destination;
            }

            @Override
            public Instant getReceiveInstant() {
              return Instant.now();
            }

            @Override
            public String getClientIpAddress() {
              return "127.0.0.1";
            }

            @Override
            public X509Certificate getClientCertificate() {
              return null;
            }

            @Override
            public AuthnRequest getAuthnRequest(final String id) {
              return authnRequest;
            }
          }, null);
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public RequestBuilder generateRequest(final String idpEntityId, final AuthnRequestGenerator generator,
      final AuthnRequestGeneratorContext context, final String relayState, final MockHttpSession session,
      int serverPort) throws Exception {
    final RequestHttpObject<AuthnRequest> requestObject =
        generator.generateAuthnRequest(idpEntityId, relayState, context);
    this.authnRequest = requestObject.getRequest();
    this.authnRequest
        .setDestination("https://local.dev.swedenconnect.se:%d/idp/saml2/post/authn".formatted(serverPort));
    this.relayState = relayState;
    final UriComponents components = UriComponentsBuilder
        .fromUriString("https://local.dev.swedenconnect.se:%d/idp/saml2/post/authn".formatted(serverPort)).build();
    MockHttpServletRequestBuilder builder;

    if ("GET".equals(requestObject.getMethod())) {
      final MultiValueMap<String, String> params = new MultiValueMapAdapter<String, String>(
          requestObject.getRequestParameters().entrySet().stream()
              .collect(Collectors.toMap(Map.Entry::getKey, e -> List.of(e.getValue()))));

      builder = MockMvcRequestBuilders.get(Objects.requireNonNull(components.getPath()))
          .queryParams(params);
    }
    else {
      builder = MockMvcRequestBuilders.post(components.getPath());
      requestObject.getRequestParameters().entrySet().stream()
          .forEach(e -> builder.param(e.getKey(), e.getValue()));
    }

    if (session != null) {
      builder.session(session);
    }

    return builder.with(r -> {
      r.setScheme("https");
      r.setServerPort(serverPort);
      r.setServerName("local.dev.swedenconnect.se");
      return r;
    }).contextPath("/idp");
  }

  public AuthnRequestGenerator createAuthnRequestGenerator(final EntityDescriptor idpMetadata) throws Exception {
    return this.createAuthnRequestGenerator(this.spSignCredential, idpMetadata);
  }

  public AuthnRequestGenerator createAuthnRequestGenerator(final PkiCredential credential,
      final EntityDescriptor idpMetadata) throws Exception {

    final SwedishEidAuthnRequestGenerator generator = new SwedishEidAuthnRequestGenerator(getEntityId(),
        Optional.ofNullable(credential)
            .map(OpenSamlCredential::new)
            .orElse(null),
        this.metadataResolver);
    final SignMessageEncrypter sme = new SignMessageEncrypter(new SAMLObjectEncrypter(this.metadataResolver));
    generator.setSignMessageEncrypter(sme);

    return generator;
  }

  public void writeSpMetadata() throws MarshallingException, InitializationException {
    InitializationService.initialize();
    org.w3c.dom.Element marshall = OpenSamlTestBase.marshall(this.getSpMetadata());
    try (FileOutputStream output = new FileOutputStream(getFilePath())) {
      writeXml(marshall.getOwnerDocument(), output);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void writeXml(org.w3c.dom.Document doc, OutputStream output) throws TransformerException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(output);

    transformer.transform(source, result);
  }

  public String getFilePath() {
    if (signMode) {
      return getSignFilePath();
    }
    return getAuthFilePath();
  }

  public String getAuthFilePath() {
    return new File("").getAbsolutePath() + "/src/test/resources/sp-metadata.xml";
  }

  public String getSignFilePath() {
    return new File("").getAbsolutePath() + "/src/test/resources/sp-sign-metadata.xml";
  }

  public String getResourcePath() {
    if (signMode) {
      return "classpath:/sp-sign-metadata.xml";
    }
    return "classpath:/sp-metadata.xml";
  }

  public String getEntityId() {
    if (signMode) {
      return SIGN_ID;
    }
    return ENTITY_ID;
  }

}
