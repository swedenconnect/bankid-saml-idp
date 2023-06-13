package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SelectedDeviceInformation {

  public boolean isSign;

  public String device;
}
