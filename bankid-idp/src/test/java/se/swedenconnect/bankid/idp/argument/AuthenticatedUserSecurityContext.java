package se.swedenconnect.bankid.idp.argument;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

@AllArgsConstructor
@Data
public class AuthenticatedUserSecurityContext implements SecurityContext {

  private final Boolean sign;

  @Override
  public Authentication getAuthentication() {
    return null;
  }

  @Override
  public void setAuthentication(Authentication authentication) {

  }
}
