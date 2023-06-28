package se.swedenconnect.bankid.idp.authn.service;

import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.rpapi.types.Requirement;

@Component
public class BankIdRequirementFactory {

    // TODO: 2023-06-27 Wire per RP configurations here and use the configuration to create requriments
    public Requirement create(PollRequest request) {
        return new Requirement();
    }
}
