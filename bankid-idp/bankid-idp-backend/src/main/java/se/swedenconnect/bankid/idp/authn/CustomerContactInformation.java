package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerContactInformation {
  private String email;
  private Boolean displayInformation;
}
