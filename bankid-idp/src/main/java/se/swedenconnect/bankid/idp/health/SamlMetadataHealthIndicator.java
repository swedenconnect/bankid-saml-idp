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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.ResolverException;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties;

/**
 * A {@link HealthIndicator} that monitors the SAML metadata feed.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Component
@Slf4j
@Profile("!integrationtest")
public class SamlMetadataHealthIndicator implements HealthIndicator {

  /** The metadata feed. */
  private final MetadataResolver metadataResolver;

  /** All configured relying parties. */
  private final List<RpInfo> rpInformation;

  /**
   * Constructor.
   *
   * @param metadataResolver the SAML metadata feed
   * @param properties the BankID IdP configuration properties
   */
  public SamlMetadataHealthIndicator(final MetadataResolver metadataResolver,
      final BankIdConfigurationProperties properties) {
    this.metadataResolver = Objects.requireNonNull(metadataResolver, "metadataResolver must not be null");

    final boolean allSpsServed = properties.getRelyingParties().stream().anyMatch(rp -> rp.getEntityIds().isEmpty());
    if (!allSpsServed) {
      this.rpInformation =
          properties.getRelyingParties().stream().map(rp -> new RpInfo(rp.getId(), rp.getEntityIds())).toList();
    }
    else {
      // We are in test-mode and serving all SP:s ...
      log.info("BankID IdP is in test-mode and is serving all SP:s - health check will not check for SP metadata");
      this.rpInformation = Collections.emptyList();
    }
  }

  /**
   * Ensures that the metadata records for each of the configured SP:s can be found.
   */
  @Override
  public Health health() {
    final Health.Builder builder = new Health.Builder();

    if (this.rpInformation.isEmpty()) {
      // We are running in test mode and accepts requests from any SP ...
      return builder
          .withDetail("info", "Test mode active - no metadata checks performed")
          .up().build();
    }

    try {
      Status status = Status.UP;
      for (final RpInfo rp : this.rpInformation) {
        final List<SpStatus> rpStatus = new ArrayList<>();
        for (final String entityId : rp.entityIds) {
          final boolean metadataPresent = this.isMetadataPresent(entityId);
          rpStatus.add(new SpStatus(entityId, metadataPresent));
          if (!metadataPresent) {
            status = Status.DOWN;
            log.warn("SAML metadata is not present for RP: {} - entityID: {}", rp.id, entityId);
          }
          else {
            log.debug("SAML metadata is present for RP: {} - entityID: {}", rp.id, entityId);
          }
        }
        builder.withDetail(rp.id, rpStatus);
      }

      return builder.status(status).build();
    }
    catch (final ResolverException e) {
      log.error("Fatal metadata error", e);
      return new Health.Builder()
          .withDetail("error", "Failure to read SAML metadata")
          .outOfService().build();
    }
  }

  /**
   * Predicate that checks if the metadata record for the given entity is present in the IdP metadata feed.
   *
   * @param entityId the entityID of the SP to check
   * @return {@code true} if the metadata is available and {@code false} otherwise
   * @throws ResolverException for underlying resolver errors
   */
  private boolean isMetadataPresent(final String entityId) throws ResolverException {
    final CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion(entityId),
        new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME),
        new ProtocolCriterion(SAMLConstants.SAML20P_NS));

    return this.metadataResolver.resolveSingle(criteria) != null;
  }

  // Holds information about a Relying Party
  private record RpInfo(String id, List<String> entityIds) {
  }

  // The metadata status for an SP
  private record SpStatus(String entityId, boolean metadataPresent) {
  }

}
