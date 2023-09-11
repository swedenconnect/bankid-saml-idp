package se.swedenconnect.bankid.idp.argument;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = SamlUserSecurityContextFactory.class)
public @interface WithSamlUser {

}
