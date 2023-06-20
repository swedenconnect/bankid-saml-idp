package se.swedenconnect.bankid.idp.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RedisSecurityProperties {
  private String p12KeyStorePath;
  private String p12KeyStorePassword;
  private String p12TrustStorePath;
  private String p12TrustStorePassword;
  private Boolean enableHostnameVerification;
}
