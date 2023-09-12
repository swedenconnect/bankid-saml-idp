package se.swedenconnect.bankid.idp.authn.api.overrides;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContentOverride {
  public enum Position {
    ABOVE,
    BELOW,
    DEVICESELECT,
    QRCODE
  }

  public enum Type {
    INFO,
    WARNING
  }

  private String text;

  private Type type;

  private Position position;
}
