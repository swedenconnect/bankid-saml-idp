package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.idp.authn.session.SessionDao;

import javax.servlet.http.HttpServletRequest;

@Component
@AllArgsConstructor
public class CustomerContactInformationFactory {

  private final UserErrorProperties properties;
  public CustomerContactInformation getContactInformation() {
    if (properties.getShowContactInformation()) {
      return new CustomerContactInformation(properties.getContactEmail(), true);
    }
    return new CustomerContactInformation("", false);
  }
}
