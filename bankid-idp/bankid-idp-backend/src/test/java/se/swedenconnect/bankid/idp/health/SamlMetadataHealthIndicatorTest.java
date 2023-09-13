/*
 * Copyright 2023 Sweden Connect
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties.RelyingPartyConfiguration;

/**
 * Test cases for SamlMetadataHealthIndicator.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class SamlMetadataHealthIndicatorTest {

  private static ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void testAllPresent() throws Exception {
    final BankIdConfigurationProperties props = new BankIdConfigurationProperties();

    final RelyingPartyConfiguration rp1 = new RelyingPartyConfiguration();
    rp1.setId("A");
    rp1.getEntityIds().add("https://www.example.com/a1");
    rp1.getEntityIds().add("https://www.example.com/a2");
    props.getRelyingParties().add(rp1);

    final RelyingPartyConfiguration rp2 = new RelyingPartyConfiguration();
    rp2.setId("B");
    rp2.getEntityIds().add("https://www.example.com/b1");
    rp2.getEntityIds().add("https://www.example.com/b2");
    props.getRelyingParties().add(rp2);

    final MetadataResolver metadata = Mockito.mock(MetadataResolver.class);
    Mockito.when(metadata.resolveSingle(Mockito.any())).thenReturn(Mockito.mock(EntityDescriptor.class));

    final SamlMetadataHealthIndicator indicator = new SamlMetadataHealthIndicator(metadata, props);

    final Health health = indicator.getHealth(true);
    Assertions.assertEquals(Status.UP, health.getStatus());

    final String json = objectMapper.writeValueAsString(health.getDetails());

    final String expected = """
        { "A" : [
          { "entityId" : "https://www.example.com/a1", "metadataPresent" : true},
          { "entityId" : "https://www.example.com/a2", "metadataPresent" : true} ],
        "B" : [
          { "entityId" : "https://www.example.com/b1", "metadataPresent" : true},
          { "entityId" : "https://www.example.com/b2", "metadataPresent" : true} ]
        }
        """;

    JSONAssert.assertEquals(expected, json, false);
  }

  @Test
  public void testMissingMetadata() throws Exception {
    final BankIdConfigurationProperties props = new BankIdConfigurationProperties();

    final RelyingPartyConfiguration rp1 = new RelyingPartyConfiguration();
    rp1.setId("A");
    rp1.getEntityIds().add("https://www.example.com/a1");
    rp1.getEntityIds().add("https://www.example.com/a2");
    props.getRelyingParties().add(rp1);

    final RelyingPartyConfiguration rp2 = new RelyingPartyConfiguration();
    rp2.setId("B");
    rp2.getEntityIds().add("https://www.example.com/b1");
    rp2.getEntityIds().add("https://www.example.com/b2");
    props.getRelyingParties().add(rp2);

    final MetadataResolver metadata = Mockito.mock(MetadataResolver.class);
    Mockito.when(metadata.resolveSingle(Mockito.any())).thenAnswer(a -> {
      final CriteriaSet criteria = a.getArgument(0, CriteriaSet.class);
      final EntityIdCriterion ec =
          criteria.stream().filter(EntityIdCriterion.class::isInstance).map(EntityIdCriterion.class::cast).findFirst().orElse(null);
      if (ec == null) {
        return null;
      }
      if ("https://www.example.com/a2".equals(ec.getEntityId()) || "https://www.example.com/b2".equals(ec.getEntityId())) {
        return null;
      }
      else {
        return Mockito.mock(EntityDescriptor.class);
      }
    });

    final SamlMetadataHealthIndicator indicator = new SamlMetadataHealthIndicator(metadata, props);

    final Health health = indicator.getHealth(true);
    Assertions.assertEquals(Status.DOWN, health.getStatus());

    final String json = objectMapper.writeValueAsString(health.getDetails());

    final String expected = """
        { "A" : [
          { "entityId" : "https://www.example.com/a1", "metadataPresent" : true},
          { "entityId" : "https://www.example.com/a2", "metadataPresent" : false} ],
        "B" : [
          { "entityId" : "https://www.example.com/b1", "metadataPresent" : true},
          { "entityId" : "https://www.example.com/b2", "metadataPresent" : false} ]
        }
        """;

    JSONAssert.assertEquals(expected, json, false);
  }

  @Test
  public void testTestMode() throws Exception {
    final BankIdConfigurationProperties props = new BankIdConfigurationProperties();

    final RelyingPartyConfiguration rp1 = new RelyingPartyConfiguration();
    rp1.setId("A");
    props.getRelyingParties().add(rp1);

    final MetadataResolver metadata = Mockito.mock(MetadataResolver.class);
    final SamlMetadataHealthIndicator indicator = new SamlMetadataHealthIndicator(metadata, props);

    final Health health = indicator.getHealth(true);
    Assertions.assertEquals(Status.UP, health.getStatus());

    final String json = objectMapper.writeValueAsString(health.getDetails());

    final String expected = "{ \"info\" : \"Test mode active - no metadata checks performed\" }";
    JSONAssert.assertEquals(expected, json, false);
  }

  @Test
  public void testResolverError() throws Exception {
    final BankIdConfigurationProperties props = new BankIdConfigurationProperties();

    final RelyingPartyConfiguration rp1 = new RelyingPartyConfiguration();
    rp1.setId("A");
    rp1.getEntityIds().add("https://www.example.com/a1");
    rp1.getEntityIds().add("https://www.example.com/a2");
    props.getRelyingParties().add(rp1);

    final MetadataResolver metadata = Mockito.mock(MetadataResolver.class);
    Mockito.when(metadata.resolveSingle(Mockito.any())).thenThrow(ResolverException.class);

    final SamlMetadataHealthIndicator indicator = new SamlMetadataHealthIndicator(metadata, props);

    final Health health = indicator.getHealth(true);
    Assertions.assertEquals(Status.OUT_OF_SERVICE, health.getStatus());

    final String json = objectMapper.writeValueAsString(health.getDetails());

    final String expected = "{ \"error\" : \"Failure to read SAML metadata\" }";

    JSONAssert.assertEquals(expected, json, false);
  }

}
