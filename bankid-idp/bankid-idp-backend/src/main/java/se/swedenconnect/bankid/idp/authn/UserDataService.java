package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyRepository;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Function;

@Slf4j
@AllArgsConstructor
@Component
public class UserDataService {

  private final RelyingPartyRepository repository;

  public BankIdUserData resolve(HttpServletRequest request, Saml2UserAuthenticationInputToken token) {
    final RelyingPartyData relyingParty = this.getRelyingParty(token.getAuthnRequestToken().getEntityId());
    BankIdSessionData sessionData = (BankIdSessionData) request.getSession().getAttribute("BANKID-STATE");
    return new BankIdUserData(sessionData, getRelyingParty(relyingParty.getEntityId()));
  }

  private RelyingPartyData getRelyingParty(final String entityId) {
    final RelyingPartyData rp = this.repository.getRelyingParty(entityId);
    if (rp == null) {
      log.info("SAML SP '{}' is not a registered BankID Relying Party", entityId);
      throw new IllegalArgumentException("The service provider with entityId:%s is not registered".formatted(entityId));
    }
    return rp;
  }
}
