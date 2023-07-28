package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServiceInformation {
    public enum Status {
        OK,
        ISSUES
    }

    private Status status;
}
