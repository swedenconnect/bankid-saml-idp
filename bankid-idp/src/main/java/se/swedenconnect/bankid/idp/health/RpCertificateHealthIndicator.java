/*
 * Copyright 2023-2024 Sweden Connect
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

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties.HealthConfiguration;
import se.swedenconnect.security.credential.PkiCredential;

/**
 * Health check indicator for the installed BankID Relying Party certificates.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Component
@Slf4j
public class RpCertificateHealthIndicator implements HealthIndicator {

  /** Information about all RP certificates installed. */
  private final List<CertificateInformation> certificateInformation;

  /** Tells when the health endpoint should warn about Relying Party certificates that are about to expire. */
  private final Duration warnThreshold;

  /**
   * Constructor.
   *
   * @param properties the BankID IdP configuration properties
   */
  public RpCertificateHealthIndicator(final BankIdConfigurationProperties properties) {
    this.warnThreshold = Optional.ofNullable(properties.getHealth())
        .map(HealthConfiguration::getRpCertificateWarnThreshold)
        .orElseGet(() -> HealthConfiguration.RP_CERTIFICATE_WARN_THRESHOLD_DEFAULT);

    this.certificateInformation = properties.getRelyingParties().stream()
        .map(rp -> {
          try {
            final PkiCredential credential = rp.createCredential();
            Objects.requireNonNull(credential);
            return new CertificateInformation(rp.getId(), credential.getCertificate().getNotAfter());
          }
          catch (final Exception e) {
            throw new IllegalArgumentException("Illegal arguments for reading certificate health of id:" + rp.getId());
          }
        })
        .toList();
  }

  /**
   * Checks if the RP certificate has expired, or is about to.
   */
  @Override
  public Health health() {
    final Health.Builder builder = new Health.Builder();
    final List<CertificateHealth> list = this.certificateInformation
        .stream()
        .map(ci -> CertificateHealth.of(ci, this.warnThreshold))
        .peek(ch -> {
          if (ch.expired()) {
            log.error("Certificate for {} has expired since {}", ch.id(), ch.expirationDate());
          }
          else if (ch.expiresSoon()) {
            log.warn("Certificate for {} is about to expire at {}", ch.id(), ch.expirationDate());
          }
          builder.withDetail(ch.id(),
              Map.of("expired", ch.expired(), "expiresSoon", ch.expiresSoon(), "expirationDate", ch.expirationDate()));
        })
        .toList();

    if (list.stream().anyMatch(ch -> ch.expired())) {
      return builder.down().build();
    }
    else if (list.stream().anyMatch(ch -> ch.expiresSoon())) {
      return builder.status(CustomStatus.WARNING).build();
    }
    else {
      return builder.up().build();
    }
  }

  private record CertificateHealth(String id, boolean expired, boolean expiresSoon, Date expirationDate) {
    private static CertificateHealth of(final CertificateInformation certificateInformation, final Duration warnThreshold) {
      final Date notAfter = certificateInformation.notAfter();
      final boolean expired = notAfter.before(new Date());
      final boolean expiresSoon = !expired
          ? notAfter.before(Date.from(Instant.now().plus(warnThreshold)))
          : false;
      return new CertificateHealth(certificateInformation.id(), expired, expiresSoon, notAfter);
    }
  }

  private record CertificateInformation(String id, Date notAfter) {

  }

}
