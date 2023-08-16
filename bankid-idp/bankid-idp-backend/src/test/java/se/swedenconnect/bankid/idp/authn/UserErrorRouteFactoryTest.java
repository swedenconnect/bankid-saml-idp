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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.swedenconnect.bankid.idp.authn.error.BankIdTraceableException;
import se.swedenconnect.bankid.idp.authn.error.UserErrorRouteFactory;
import se.swedenconnect.spring.saml.idp.error.UnrecoverableSaml2IdpError;
import se.swedenconnect.spring.saml.idp.error.UnrecoverableSaml2IdpException;

import java.util.stream.Stream;

class UserErrorRouteFactoryTest {

  @Test
  void defaultErrorRoute() {
    final UserErrorRouteFactory userErrorRouteFactory =
        new UserErrorRouteFactory(UserErrorPropertiesFixture.EMPTY_PROPERTIES);
    final String redirect = userErrorRouteFactory.getRedirect(new RuntimeException());
    final String redirectView = userErrorRouteFactory.getRedirectView(new RuntimeException());
    Assertions.assertEquals("bankid#/error/bankid.msg.error.unknown", redirect);
    Assertions.assertEquals("redirect:/bankid#/error/bankid.msg.error.unknown", redirectView);
  }
  @ParameterizedTest
  @MethodSource("unrecoverableSaml2IpdExceptions")
  void errorRouteUnrecoverableSaml2IdpException(UnrecoverableSaml2IdpException e) {
    final UserErrorRouteFactory userErrorRouteFactory =
        new UserErrorRouteFactory(UserErrorPropertiesFixture.EMPTY_PROPERTIES);
    final String redirect = userErrorRouteFactory.getRedirect(e);
    final String redirectView = userErrorRouteFactory.getRedirectView(e);
    Assertions.assertEquals("bankid#/error/" + e.getError().getMessageCode(), redirect);
    Assertions.assertEquals("redirect:/bankid#/error/" + e.getError().getMessageCode(), redirectView);
  }

  @Test
  void showEmailNoTrace() {
    final UserErrorRouteFactory userErrorRouteFactory =
        new UserErrorRouteFactory(UserErrorPropertiesFixture.SHOW_EMAIL_NO_TRACE);
    final String redirect = userErrorRouteFactory.getRedirect(new RuntimeException());
    final String redirectView = userErrorRouteFactory.getRedirectView(new RuntimeException());
    Assertions.assertEquals("bankid#/error/bankid.msg.error.unknown", redirect);
    Assertions.assertEquals("redirect:/bankid#/error/bankid.msg.error.unknown", redirectView);
  }

  @Test
  void showEmailShowTrace() {
    final UserErrorRouteFactory userErrorRouteFactory =
        new UserErrorRouteFactory(UserErrorPropertiesFixture.SHOW_EMAIL_SHOW_TRACE);
    final String redirect = userErrorRouteFactory.getRedirect(new RuntimeException());
    final String redirectView = userErrorRouteFactory.getRedirectView(new RuntimeException());
    final String BASE_REGEX = "bankid#\\/error\\/bankid.msg.error.unknown\\/";
    final String UUID_REGEX = "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}";
    assertThat(redirect, matchesPattern(BASE_REGEX + UUID_REGEX));
    assertThat(redirectView, matchesPattern("redirect:/" + BASE_REGEX + UUID_REGEX));
  }

  @Test
  void showEmailShowTrace_fromException() {
    final UserErrorRouteFactory userErrorRouteFactory =
        new UserErrorRouteFactory(UserErrorPropertiesFixture.SHOW_EMAIL_SHOW_TRACE);
    final BankIdTraceableException ex = new BankIdTraceableException("order-ref", "Error msg");
    final String expectedId = ex.getTraceId();
    final String redirect = userErrorRouteFactory.getRedirect(ex);
    final String redirectView = userErrorRouteFactory.getRedirectView(ex);
    final String BASE_REGEX = "bankid#\\/error\\/bankid.msg.error.unknown\\/";
    assertThat(redirect, matchesPattern(BASE_REGEX + expectedId));
    assertThat(redirectView, matchesPattern("redirect:/" + BASE_REGEX + expectedId));
  }

  private static Stream<Arguments> unrecoverableSaml2IpdExceptions() {
    return Stream.of(
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.ENDPOINT_CHECK_FAILURE)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.INVALID_SESSION)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.FAILED_DECODE)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.INVALID_ASSERTION_CONSUMER_SERVICE)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.INVALID_AUTHNREQUEST_FORMAT)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.INTERNAL)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.REPLAY_DETECTED)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.MESSAGE_TOO_OLD)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.INVALID_AUTHNREQUEST_SIGNATURE)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.MISSING_AUTHNREQUEST_SIGNATURE)),
        Arguments.of(new UnrecoverableSaml2IdpException(UnrecoverableSaml2IdpError.UNKNOWN_PEER))
    );
  }
}