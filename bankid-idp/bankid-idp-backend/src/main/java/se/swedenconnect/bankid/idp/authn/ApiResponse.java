package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiResponse {
    public enum Status {
        IN_PROGRESS,
        ERROR,
        COMPLETE,

        CANCEL
    }

    private Status status;

    private String qrCode;

    private String autoStartToken;

    private String messageCode;
}
