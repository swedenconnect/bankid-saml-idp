package se.swedenconnect.bankid.idp.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.swedenconnect.bankid.rpapi.types.Requirement;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EntityRequirement {
  private Boolean useFingerPrint;
  private List<String> issuerCn;
  private List<String> certificatePolicies;
  private Requirement.CardReaderRequirement cardReader;
  private Boolean tokenStartRequired;
}
