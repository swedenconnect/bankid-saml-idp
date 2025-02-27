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
package se.swedenconnect.bankid.idp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/**
 * Test cases for ApplicationVersion.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class ApplicationVersionTest {

  private String version;

  public ApplicationVersionTest() throws Exception {
    final Properties properties = new Properties();
    properties.load(this.getClass().getClassLoader().getResourceAsStream("version.properties"));
    this.version = properties.getProperty("bankid.idp.version");
    if (this.version.endsWith("-SNAPSHOT")) {
      this.version = this.version.substring(0, version.length() - 9);
    }
  }

  @Test
  public void testUid() {
    Assertions.assertEquals(this.version.hashCode(), ApplicationVersion.SERIAL_VERSION_UID);
  }

  @Test
  public void testVersion() throws Exception {
    Assertions.assertEquals(this.version, ApplicationVersion.getVersion(),
        "Expected ApplicationVersion.getVersion() to return " + version);
  }

}
