package se.swedenconnect.bankid.idp.authn;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

class UserErrorRouteFactoryTest {

  @Test
  void defaultErrorRoute() {
    UserErrorRouteFactory userErrorRouteFactory = new UserErrorRouteFactory(UserErrorPropertiesFixture.EMPTY_PROPERTIES);
    String redirect = userErrorRouteFactory.getRedirect(new RuntimeException());
    String redirectView = userErrorRouteFactory.getRedirectView(new RuntimeException());
    Assertions.assertEquals("bankid#/error/unknown", redirect);
    Assertions.assertEquals("redirect:/bankid#/error/unknown", redirectView);
  }

  @Test
  void showEmailNoTrace() {
    UserErrorRouteFactory userErrorRouteFactory = new UserErrorRouteFactory(UserErrorPropertiesFixture.SHOW_EMAIL_NO_TRACE);
    String redirect = userErrorRouteFactory.getRedirect(new RuntimeException());
    String redirectView = userErrorRouteFactory.getRedirectView(new RuntimeException());
    Assertions.assertEquals("bankid#/error/unknown", redirect);
    Assertions.assertEquals("redirect:/bankid#/error/unknown", redirectView);
  }

  @Test
  void showEmailShowTrace() {
    UserErrorRouteFactory userErrorRouteFactory = new UserErrorRouteFactory(UserErrorPropertiesFixture.SHOW_EMAIL_SHOW_TRACE);
    String redirect = userErrorRouteFactory.getRedirect(new RuntimeException());
    String redirectView = userErrorRouteFactory.getRedirectView(new RuntimeException());
    String BASE_REGEX = "bankid#\\/error\\/unknown\\/";
    String UUID_REGEX = "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}";
    assertThat(redirect, matchesPattern(BASE_REGEX + UUID_REGEX));
    assertThat(redirectView, matchesPattern("redirect:/" + BASE_REGEX + UUID_REGEX));
  }

  @Test
  void showEmailShowTrace_fromException() {
    UserErrorRouteFactory userErrorRouteFactory = new UserErrorRouteFactory(UserErrorPropertiesFixture.SHOW_EMAIL_SHOW_TRACE);
    String expectedId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaaaaaa";
    String redirect = userErrorRouteFactory.getRedirect(new BankIdAuthenticationException(expectedId));
    String redirectView = userErrorRouteFactory.getRedirectView(new BankIdAuthenticationException(expectedId));
    String BASE_REGEX = "bankid#\\/error\\/unknown\\/";
    assertThat(redirect, matchesPattern(BASE_REGEX + expectedId));
    assertThat(redirectView, matchesPattern("redirect:/" + BASE_REGEX + expectedId));
  }

}