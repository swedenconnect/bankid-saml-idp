package se.swedenconnect.bankid.idp.authn.api.overrides;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FrontendOverrideResponse {

  private List<MessageOverride> messages;
  private List<CssOverride> css;
  private List<ContentOverride> content;
}
