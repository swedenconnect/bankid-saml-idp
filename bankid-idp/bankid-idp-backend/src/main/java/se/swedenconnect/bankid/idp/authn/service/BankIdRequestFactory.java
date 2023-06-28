package se.swedenconnect.bankid.idp.authn.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.rpapi.service.AuthenticateRequest;
import se.swedenconnect.bankid.rpapi.service.DataToSign;
import se.swedenconnect.bankid.rpapi.service.SignatureRequest;

@Component
@AllArgsConstructor
public class BankIdRequestFactory {

    private final BankIdRequirementFactory requirementFactory;

    public AuthenticateRequest createAuthenticateRequest(PollRequest request) {
        return new AuthenticateRequest(request.getContext().getPersonalNumber(), request.getRequest().getRemoteAddr(), request.getData(), requirementFactory.create(request));
    }
    public SignatureRequest createSignRequest(PollRequest request) {
        if (request.getData() instanceof DataToSign dataToSign) {
            return new SignatureRequest(request.getContext().getPersonalNumber(), request.getRequest().getRemoteAddr(), dataToSign, requirementFactory.create(request));
        } else {
            throw new IllegalArgumentException("Message was not of type DataToSign but was " + request.getData().getClass().getCanonicalName());
        }
    }
}
