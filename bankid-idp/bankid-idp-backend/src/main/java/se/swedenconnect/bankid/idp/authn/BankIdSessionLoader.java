package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyRepository;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@AllArgsConstructor
@Component
public class BankIdSessionLoader {

  private final RelyingPartyRepository repository;

  public BankIdUserData load(HttpServletRequest request, Saml2UserAuthenticationInputToken token) {
    final RelyingPartyData relyingParty = this.getRelyingParty(token.getAuthnRequestToken().getEntityId());
    BankIdSessionData sessionData = (BankIdSessionData) request.getSession().getAttribute("BANKID-STATE");
    return new BankIdUserData(sessionData, getRelyingParty(relyingParty.getEntityId()));
  }

  public void save(HttpServletRequest request, BankIdSessionData data) {
    request.getSession().setAttribute("BANKID-STATE", data);
  }

  private RelyingPartyData getRelyingParty(final String entityId) {
    final RelyingPartyData rp = this.repository.getRelyingParty(entityId);
    if (rp == null) {
      log.info("SAML SP '{}' is not a registered BankID Relying Party", entityId);
      throw new IllegalArgumentException("The service provider with entityId:%s is not registered".formatted(entityId));
    }
    return rp;
  }

  public void delete(HttpServletRequest request) {
    request.getSession().setAttribute("BANKID-STATE", null);
    request.getSession().setAttribute("BANKID-COMPLETION-DATA", null);
  }
}
