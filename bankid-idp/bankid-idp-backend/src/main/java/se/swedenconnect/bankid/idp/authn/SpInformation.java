package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Data
public class SpInformation {

  // language -> name
  private Map<String, String> displayNames = new HashMap<>();

  private final String imageUrl;
}
