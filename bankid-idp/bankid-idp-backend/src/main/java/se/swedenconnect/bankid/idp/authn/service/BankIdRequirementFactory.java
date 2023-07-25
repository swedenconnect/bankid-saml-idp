package se.swedenconnect.bankid.idp.authn.service;

import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.idp.config.EntityRequirement;
import se.swedenconnect.bankid.rpapi.types.Requirement;

import java.util.Optional;

@Component
public class BankIdRequirementFactory {
  public static Requirement create(final PollRequest request) {
    Optional<EntityRequirement> requirement = Optional.ofNullable(request.getRelyingPartyData().getRequirement());
    return requirement.map(BankIdRequirementFactory::fromEntityRequirement)
        .orElseGet(BankIdRequirementFactory::defaultRequirement);
  }

  public static Requirement fromEntityRequirement(EntityRequirement entityRequirement) {
    Requirement requirement = new Requirement();
    Optional.ofNullable(entityRequirement.getUseFingerPrint()).ifPresent(requirement::setAllowFingerprint);
    Optional.ofNullable(entityRequirement.getIssuerCn()).ifPresent(requirement::setIssuerCn);
    Optional.ofNullable(entityRequirement.getTokenStartRequired()).ifPresent(requirement::setTokenStartRequired);
    Optional.ofNullable(entityRequirement.getCardReader()).ifPresent(requirement::setCardReader);
    Optional.ofNullable(entityRequirement.getCertificatePolicies()).ifPresent(requirement::setCertificatePolicies);
    return requirement;
  }

  public static Requirement defaultRequirement() {
    return new Requirement();
  }
}
