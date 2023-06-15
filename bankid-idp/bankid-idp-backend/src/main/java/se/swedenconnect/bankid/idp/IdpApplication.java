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
package se.swedenconnect.bankid.idp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import se.swedenconnect.opensaml.OpenSAMLInitializer;
import se.swedenconnect.opensaml.OpenSAMLSecurityDefaultsConfig;
import se.swedenconnect.opensaml.OpenSAMLSecurityExtensionConfig;
import se.swedenconnect.opensaml.sweid.xmlsec.config.SwedishEidSecurityConfiguration;

/**
 * Application main.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
@SpringBootApplication
@EnableConfigurationProperties
public class IdpApplication {

  /**
   * Program main.
   * 
   * @param args program arguments
   */
  public static void main(String[] args) {

    try {
      OpenSAMLInitializer.getInstance()
          .initialize(
              new OpenSAMLSecurityDefaultsConfig(new SwedishEidSecurityConfiguration()),
              new OpenSAMLSecurityExtensionConfig());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    SpringApplication.run(IdpApplication.class, args);
  }

}
