package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SpInformation {

  // language -> name
  private Map<String, String> displayNames = new HashMap<>();

  private String imageUrl;
}
