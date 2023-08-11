package se.swedenconnect.bankid.idp.authn;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class UserErrorPropertiesFixture {

  public static final UserErrorProperties EMPTY_PROPERTIES = new UserErrorProperties("", false, false);
  public static final UserErrorProperties SHOW_EMAIL_NO_TRACE = new UserErrorProperties("contact@email.com", false, true);
  public static final UserErrorProperties SHOW_EMAIL_SHOW_TRACE = new UserErrorProperties("contact@email.com", true, true);
}
