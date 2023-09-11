package se.swedenconnect.bankid.idp.argument;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class SamlUserSecurityContextFactory implements WithSecurityContextFactory<WithSamlUser> {
  @Override
  public SecurityContext createSecurityContext(WithSamlUser annotation) {
    AuthenticatedUserSecurityContext context = new AuthenticatedUserSecurityContext();
    SecurityContextHolder.setContext(context);
    return context;
  }
}
