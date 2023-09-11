package se.swedenconnect.bankid.idp.argument;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import java.security.Principal;

@AllArgsConstructor
@Data
public class AuthenticatedUserSecurityContext implements SecurityContext {

  @Override
  public Authentication getAuthentication() {
    return null;
  }

  @Override
  public void setAuthentication(Authentication authentication) {

  }
}
