package se.swedenconnect.bankid.idp.authn.resilience;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties;
import se.swedenconnect.security.credential.PkiCredential;
import se.swedenconnect.security.credential.factory.PkiCredentialConfigurationProperties;
import se.swedenconnect.security.credential.factory.PkiCredentialFactoryBean;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@AllArgsConstructor
@Component
@Slf4j
public class RpCertificateHealthIndicator implements HealthIndicator {

    private final BankIdConfigurationProperties bankIdConfigurationProperties;

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        boolean anyExpired = bankIdConfigurationProperties.getRelyingParties()
                .stream()
                .map(this::checkExpiration)
                .peek(ch -> {
                    builder.withDetail("id", ch.id);
                    builder.withDetail("expired", ch.expired);
                    builder.withDetail("expiresSoon", ch.expiresSoon);
                    builder.withDetail("expirationDate", ch.expirationDate);
                })
                .anyMatch(ch -> ch.expired);
        if (anyExpired) {
            return builder.down().build();
        }
        return builder.up().build();
    }

    private CertificateHealth checkExpiration(BankIdConfigurationProperties.RelyingParty relyingParty) {
        try {

            PkiCredential pkiCredential = this.createPkiCredential(relyingParty.getCredential());
            Date notAfter = pkiCredential.getCertificate().getNotAfter();
            boolean expired = notAfter.before(Date.from(Instant.now()));
            boolean expiresSoon = notAfter.before(Date.from(Instant.now().minus(14, ChronoUnit.DAYS)));
            return new CertificateHealth(relyingParty.getId(), expired, expiresSoon, notAfter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PkiCredential createPkiCredential(final PkiCredentialConfigurationProperties cred) throws Exception {
        final PkiCredentialFactoryBean factory = new PkiCredentialFactoryBean(cred);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    private record CertificateHealth(String id, boolean expired, boolean expiresSoon, Date expirationDate) {

    }
}
