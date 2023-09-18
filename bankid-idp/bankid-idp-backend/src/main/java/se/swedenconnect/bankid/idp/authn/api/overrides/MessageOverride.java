package se.swedenconnect.bankid.idp.authn.api.overrides;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageOverride {

  private String sv;
  private String en;
  private String code;
}
