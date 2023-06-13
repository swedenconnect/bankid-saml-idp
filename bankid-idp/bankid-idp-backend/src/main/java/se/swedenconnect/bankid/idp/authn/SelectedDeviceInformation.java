package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SelectedDeviceInformation {
  public enum SignDevice {
    SAME,
    OTHER
  }
  public boolean isSign;

  public SignDevice device;
}
