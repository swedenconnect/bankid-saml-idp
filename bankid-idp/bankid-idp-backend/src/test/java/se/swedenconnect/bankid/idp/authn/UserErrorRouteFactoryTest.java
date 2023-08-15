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

import se.swedenconnect.bankid.idp.authn.error.BankIdTraceableException;
import se.swedenconnect.bankid.idp.authn.error.UserErrorRouteFactory;

class UserErrorRouteFactoryTest {

  @Test
  void defaultErrorRoute() {
    final UserErrorRouteFactory userErrorRouteFactory =
        new UserErrorRouteFactory(UserErrorPropertiesFixture.EMPTY_PROPERTIES);
    final String redirect = userErrorRouteFactory.getRedirect(new RuntimeException());
    final String redirectView = userErrorRouteFactory.getRedirectView(new RuntimeException());
    Assertions.assertEquals("bankid#/error/unknown", redirect);
    Assertions.assertEquals("redirect:/bankid#/error/unknown", redirectView);
  }

  @Test
  void showEmailNoTrace() {
    final UserErrorRouteFactory userErrorRouteFactory =
        new UserErrorRouteFactory(UserErrorPropertiesFixture.SHOW_EMAIL_NO_TRACE);
    final String redirect = userErrorRouteFactory.getRedirect(new RuntimeException());
    final String redirectView = userErrorRouteFactory.getRedirectView(new RuntimeException());
    Assertions.assertEquals("bankid#/error/unknown", redirect);
    Assertions.assertEquals("redirect:/bankid#/error/unknown", redirectView);
  }

  @Test
  void showEmailShowTrace() {
    final UserErrorRouteFactory userErrorRouteFactory =
        new UserErrorRouteFactory(UserErrorPropertiesFixture.SHOW_EMAIL_SHOW_TRACE);
    final String redirect = userErrorRouteFactory.getRedirect(new RuntimeException());
    final String redirectView = userErrorRouteFactory.getRedirectView(new RuntimeException());
    final String BASE_REGEX = "bankid#\\/error\\/unknown\\/";
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
    final String BASE_REGEX = "bankid#\\/error\\/unknown\\/";
    assertThat(redirect, matchesPattern(BASE_REGEX + expectedId));
    assertThat(redirectView, matchesPattern("redirect:/" + BASE_REGEX + expectedId));
  }

}