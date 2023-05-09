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
package se.swedenconnect.bankid.idp.config.tomcat;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import lombok.Setter;

/**
 * Configuration settings for Tomcat AJP.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Component
@ConditionalOnProperty(name = "tomcat.ajp.enabled", havingValue = "true")
@EnableConfigurationProperties(TomcatAjpConfigurationProperties.class)
public class TomcatAjpCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

  @Autowired
  @Setter
  private TomcatAjpConfigurationProperties ajp;

  /** {@inheritDoc} */
  @Override
  public void customize(final TomcatServletWebServerFactory factory) {

    if (this.ajp.isEnabled()) {
      Connector ajpConnector = new Connector("AJP/1.3");
      ajpConnector.setPort(this.ajp.getPort());
      ajpConnector.setAllowTrace(false);
      ajpConnector.setScheme("http");
      ajpConnector.setProperty("address", "0.0.0.0");
      ajpConnector.setProperty("allowedRequestAttributesPattern", ".*");

      final AbstractAjpProtocol<?> protocol = (AbstractAjpProtocol<?>) ajpConnector.getProtocolHandler();
      if (this.ajp.isSecretRequired()) {
        ajpConnector.setSecure(true);
        protocol.setSecretRequired(true);
        protocol.setSecret(this.ajp.getSecret());
      }
      else {
        ajpConnector.setSecure(false);
        protocol.setSecretRequired(false);
      }

      factory.addAdditionalTomcatConnectors(ajpConnector);
    }

  }

}
