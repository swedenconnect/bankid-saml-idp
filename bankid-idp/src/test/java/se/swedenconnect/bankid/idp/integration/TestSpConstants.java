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
package se.swedenconnect.bankid.idp.integration;

import java.util.Arrays;

import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import se.swedenconnect.opensaml.saml2.metadata.provider.StaticMetadataProvider;

/**
 * Constants used in integration tests.
 *
 * @author Martin Lindström
 */
public class TestSpConstants {

  public static MetadataResolver createMetadataResolver(final EntityDescriptor... metadata) throws Exception {

    final EntitiesDescriptor ed =
        (EntitiesDescriptor) XMLObjectSupport.buildXMLObject(EntitiesDescriptor.DEFAULT_ELEMENT_NAME);
    ed.setID("_simulatedmetadata");

    Arrays.stream(metadata).forEach(m -> ed.getEntityDescriptors().add(m));

    StaticMetadataProvider provider = new StaticMetadataProvider(ed);
    provider.initialize();

    return provider.getMetadataResolver();
  }

}