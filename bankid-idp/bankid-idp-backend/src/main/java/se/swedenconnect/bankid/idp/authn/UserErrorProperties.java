package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserErrorProperties {
  private String contactEmail;
  private Boolean showTraceId;
  private Boolean showContactInformation;
}
