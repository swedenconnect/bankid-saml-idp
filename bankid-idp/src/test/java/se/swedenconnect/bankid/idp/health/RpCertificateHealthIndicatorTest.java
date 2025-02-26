/*
 * Copyright 2023-2025 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.swedenconnect.bankid.idp.health;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties.RelyingPartyConfiguration;
import se.swedenconnect.security.credential.PkiCredential;

import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Test cases for RpCertificateHealthIndicator.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class RpCertificateHealthIndicatorTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void testUp() throws Exception {
    final Instant NOW = Instant.now();

    final Rp rp1 = new Rp("ID1", NOW.plus(365, ChronoUnit.DAYS));
    final Rp rp2 = new Rp("ID2", NOW.plus(15, ChronoUnit.DAYS));

    final BankIdConfigurationProperties props = buildProps(rp1, rp2);
    final Function<RelyingPartyConfiguration, PkiCredential> credProvider = buildCredentialFunction(rp1, rp2);

    final RpCertificateHealthIndicator indicator = new RpCertificateHealthIndicator(props, credProvider);

    final Health health = indicator.getHealth(true);
    Assertions.assertEquals(Status.UP, health.getStatus());

    final String json = objectMapper.writeValueAsString(health.getDetails());

    final String expected =
        "{ \"ID1\" : { \"expiresSoon\" : false, \"expired\" : false, \"expirationDate\" : " + rp1.formatExpires()
            + " },"
            + "\"ID2\" : { \"expiresSoon\" : false, \"expired\" : false, \"expirationDate\" : " + rp2.formatExpires()
            + " }}";

    JSONAssert.assertEquals(expected, json, false);
  }

  @Test
  public void testExpired() throws Exception {
    final Instant NOW = Instant.now();

    final Rp rp1 = new Rp("ID1", NOW.plus(365, ChronoUnit.DAYS));
    final Rp rp2 = new Rp("ID2", NOW.minus(2, ChronoUnit.DAYS));

    final BankIdConfigurationProperties props = buildProps(rp1, rp2);
    final Function<RelyingPartyConfiguration, PkiCredential> credProvider = buildCredentialFunction(rp1, rp2);

    final RpCertificateHealthIndicator indicator = new RpCertificateHealthIndicator(props, credProvider);

    final Health health = indicator.getHealth(true);
    Assertions.assertEquals(Status.DOWN, health.getStatus());

    final String json = objectMapper.writeValueAsString(health.getDetails());

    final String expected =
        "{ \"ID1\" : { \"expiresSoon\" : false, \"expired\" : false, \"expirationDate\" : " + rp1.formatExpires()
            + " },"
            + "\"ID2\" : { \"expiresSoon\" : false, \"expired\" : true, \"expirationDate\" : " + rp2.formatExpires()
            + " }}";

    JSONAssert.assertEquals(expected, json, false);
  }

  @Test
  public void testWarning() throws Exception {
    final Instant NOW = Instant.now();

    final Rp rp1 = new Rp("ID1", NOW.plus(365, ChronoUnit.DAYS));
    final Rp rp2 = new Rp("ID2", NOW.plus(2, ChronoUnit.DAYS));

    final BankIdConfigurationProperties props = buildProps(rp1, rp2);
    props.getHealth().setRpCertificateWarnThreshold(Duration.ofDays(7));
    final Function<RelyingPartyConfiguration, PkiCredential> credProvider = buildCredentialFunction(rp1, rp2);

    final RpCertificateHealthIndicator indicator = new RpCertificateHealthIndicator(props, credProvider);

    final Health health = indicator.getHealth(true);
    Assertions.assertEquals(CustomStatus.WARNING, health.getStatus());

    final String json = objectMapper.writeValueAsString(health.getDetails());

    final String expected =
        "{ \"ID1\" : { \"expiresSoon\" : false, \"expired\" : false, \"expirationDate\" : " + rp1.formatExpires()
            + " },"
            + "\"ID2\" : { \"expiresSoon\" : true, \"expired\" : false, \"expirationDate\" : " + rp2.formatExpires()
            + " }}";

    JSONAssert.assertEquals(expected, json, false);
  }

  private static BankIdConfigurationProperties buildProps(final Rp... rps) throws Exception {
    final BankIdConfigurationProperties props = new BankIdConfigurationProperties();
    for (final Rp rp : rps) {
      final RelyingPartyConfiguration relyingParty = Mockito.mock(RelyingPartyConfiguration.class);
      Mockito.when(relyingParty.getId()).thenReturn(rp.id());
      props.getRelyingParties().add(relyingParty);
    }

    return props;
  }

  private static Function<RelyingPartyConfiguration, PkiCredential> buildCredentialFunction(final Rp... rps) {
    final Map<String, PkiCredential> credentials = new HashMap<>();

    for (final Rp rp : rps) {
      final RelyingPartyConfiguration relyingParty = Mockito.mock(RelyingPartyConfiguration.class);
      Mockito.when(relyingParty.getId()).thenReturn(rp.id());

      final X509Certificate cert = Mockito.mock(X509Certificate.class);
      Mockito.when(cert.getNotAfter()).thenReturn(Date.from(rp.expires()));

      final PkiCredential cred = Mockito.mock(PkiCredential.class);
      Mockito.when(cred.getCertificate()).thenReturn(cert);

      credentials.put(rp.id(), cred);
    }

    return conf -> credentials.get(conf.getId());
  }

  private record Rp(String id, Instant expires) {

    public String formatExpires() {
      try {
        return objectMapper.writeValueAsString(Date.from(this.expires));
      }
      catch (final JsonProcessingException e) {
        return "";
      }
    }

  }

}
