package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class CancellationData implements Serializable {
  public enum Type {
    USER_CANCEL
  }

  private final Type type;
}
