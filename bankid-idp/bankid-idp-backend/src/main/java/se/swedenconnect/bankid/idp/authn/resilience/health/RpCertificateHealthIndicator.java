package se.swedenconnect.bankid.idp.authn.resilience.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties;
import se.swedenconnect.security.credential.PkiCredential;
import se.swedenconnect.security.credential.factory.PkiCredentialFactoryBean;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class RpCertificateHealthIndicator implements HealthIndicator {

    private final List<CertificateInformation> certificateInformation;

    public RpCertificateHealthIndicator(BankIdConfigurationProperties properties) {
        this.certificateInformation = properties.getRelyingParties().stream()
                .map(rp -> {
                    final PkiCredentialFactoryBean factory = new PkiCredentialFactoryBean(rp.getCredential());
                    try {
                        factory.afterPropertiesSet();
                        PkiCredential credential = factory.getObject();
                        Objects.requireNonNull(credential);
                        Date notAfter = credential.getCertificate().getNotAfter();
                        return new CertificateInformation(rp.getId(), notAfter);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Illegal arguments for reading certificate health of id:" + rp.getId());
                    }
                })
                .toList();
    }

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        List<CertificateHealth> list = certificateInformation
                .stream()
                .map(CertificateHealth::of)
                .peek(ch -> {
                    if (ch.expired()) {
                        log.error("Certificate for {} has expired since {}", ch.id, ch.expirationDate);
                    }
                    if (ch.expiresSoon() && !ch.expired) {
                        log.warn("Certificate for {} is about to expire at {}", ch.id, ch.expirationDate);
                    }
                    builder.withDetail(ch.id, Map.of("expired", ch.expired, "expiresSoon", ch.expiresSoon, "expirationDate", ch.expirationDate));
                })
                .toList();
        boolean anyExpired = list.stream().anyMatch(ch -> ch.expired);
        if (anyExpired) {
            return builder.down().build();
        }
        return builder.up().build();
    }

    private record CertificateHealth(String id, boolean expired, boolean expiresSoon, Date expirationDate) {
        private static CertificateHealth of(CertificateInformation certificateInformation) {
            Date notAfter = certificateInformation.notAfter();
            boolean expired = notAfter.before(Date.from(Instant.now()));
            boolean expiresSoon = notAfter.before(Date.from(Instant.now().minus(14, ChronoUnit.DAYS)));
            return new CertificateHealth(certificateInformation.id(), expired, expiresSoon, notAfter);
        }
    }

    private record CertificateInformation(String id, Date notAfter) {

    }
}
