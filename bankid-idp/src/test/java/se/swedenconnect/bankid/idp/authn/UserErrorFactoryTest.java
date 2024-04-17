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
package se.swedenconnect.bankid.idp.authn;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import jakarta.servlet.http.HttpServletRequest;
import se.swedenconnect.bankid.idp.authn.error.BankIdTraceableException;
import se.swedenconnect.bankid.idp.authn.error.UserErrorFactory;
import se.swedenconnect.spring.saml.idp.error.UnrecoverableSaml2IdpError;
import se.swedenconnect.spring.saml.idp.error.UnrecoverableSaml2IdpException;

class UserErrorFactoryTest {

  public static final String EXPECTED_CONTEX_PATH = "/context/path";

  @Test
  void defaultErrorRoute() {
    final UserErrorFactory userErrorFactory =
        new UserErrorFactory(UserErrorPropertiesFixture.EMPTY_PROPERTIES);
    final String redirect = userErrorFactory.getRedirect(getMockRequest(), new RuntimeException());
    final String redirectView = userErrorFactory.getRedirectView(new RuntimeException());
    Assertions.assertEquals("/context/path/bankid#/error/bankid.msg.error.unknown", redirect);
    Assertions.assertEquals("redirect:/bankid#/error/bankid.msg.error.unknown", redirectView);
  }

  private static HttpServletRequest getMockRequest() {
    HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
    Mockito.when(servletRequest.getContextPath()).thenReturn(EXPECTED_CONTEX_PATH);
    return servletRequest;
  }

  @ParameterizedTest
  @MethodSource("unrecoverableSaml2IpdExceptions")
  void errorRouteUnrecoverableSaml2IdpException(UnrecoverableSaml2IdpException e) {
    final UserErrorFactory userErrorFactory =
        new UserErrorFactory(UserErrorPropertiesFixture.EMPTY_PROPERTIES);
    final String redirect = userErrorFactory.getRedirect(getMockRequest(), e);
    final String redirectView = userErrorFactory.getRedirectView(e);
    Assertions.assertEquals("/context/path/bankid#/error/" + e.getError().getMessageCode(), redirect);
    Assertions.assertEquals("redirect:/bankid#/error/" + e.getError().getMessageCode(), redirectView);
  }

  @Test
  void showEmailNoTrace() {
    final UserErrorFactory userErrorFactory =
        new UserErrorFactory(UserErrorPropertiesFixture.SHOW_EMAIL_NO_TRACE);
    final String redirect = userErrorFactory.getRedirect(getMockRequest(), new RuntimeException());
    final String redirectView = userErrorFactory.getRedirectView(new RuntimeException());
    Assertions.assertEquals("/context/path/bankid#/error/bankid.msg.error.unknown", redirect);
    Assertions.assertEquals("redirect:/bankid#/error/bankid.msg.error.unknown", redirectView);
  }

  @Test
  void showEmailShowTrace() {
    final UserErrorFactory userErrorFactory =
        new UserErrorFactory(UserErrorPropertiesFixture.SHOW_EMAIL_SHOW_TRACE);
    final String redirect = userErrorFactory.getRedirect(getMockRequest(),new RuntimeException());
    final String redirectView = userErrorFactory.getRedirectView(new RuntimeException());
    final String BASE_REGEX = "bankid#/error/bankid.msg.error.unknown/";
    final String UUID_REGEX = "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}";
    assertThat(redirect, matchesPattern("/context/path/" + BASE_REGEX + UUID_REGEX));
    assertThat(redirectView, matchesPattern("redirect:/" + BASE_REGEX + UUID_REGEX));
  }

  @Test
  void showEmailShowTrace_fromException() {
    final UserErrorFactory userErrorFactory =
        new UserErrorFactory(UserErrorPropertiesFixture.SHOW_EMAIL_SHOW_TRACE);
    final BankIdTraceableException ex = new BankIdTraceableException("order-ref", "Error msg");
    final String expectedId = ex.getTraceId();
    final String redirect = userErrorFactory.getRedirect(getMockRequest(), ex);
    final String redirectView = userErrorFactory.getRedirectView(ex);
    final String BASE_REGEX = "bankid#\\/error\\/bankid.msg.error.unknown\\/";
    assertThat(redirect, matchesPattern("/context/path/" + BASE_REGEX + expectedId));
    assertThat(redirectView, matchesPattern("redirect:/" + BASE_REGEX + expectedId));
  }

  private static Stream<Arguments> unrecoverableSaml2IpdExceptions() {
    return Stream.of(
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.ENDPOINT_CHECK_FAILURE, null)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.INVALID_SESSION, null)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.FAILED_DECODE, null)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.INVALID_ASSERTION_CONSUMER_SERVICE, null)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.INVALID_AUTHNREQUEST_FORMAT, null)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.INTERNAL, null)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.REPLAY_DETECTED, null)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.MESSAGE_TOO_OLD, null)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.INVALID_AUTHNREQUEST_SIGNATURE, null)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.MISSING_AUTHNREQUEST_SIGNATURE, null)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.UNKNOWN_PEER, null))
    );
  }
}
