package se.swedenconnect.bankid.idp.authn.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.rpapi.service.AuthenticateRequest;
import se.swedenconnect.bankid.rpapi.service.DataToSign;
import se.swedenconnect.bankid.rpapi.service.SignatureRequest;

@Component
@AllArgsConstructor
public class BankIdRequestFactory {

    public AuthenticateRequest createAuthenticateRequest(final PollRequest request) {
        return new AuthenticateRequest(request.getContext().getPersonalNumber(), request.getRequest().getRemoteAddr(), request.getData(), BankIdRequirementFactory.create(request));
    }
    public SignatureRequest createSignRequest(final PollRequest request) {
        if (request.getData() instanceof final DataToSign dataToSign) {
            return new SignatureRequest(request.getContext().getPersonalNumber(), request.getRequest().getRemoteAddr(), dataToSign, BankIdRequirementFactory.create(request));
        } else {
            throw new IllegalArgumentException("Message was not of type DataToSign but was " + request.getData().getClass().getCanonicalName());
        }
    }
}
