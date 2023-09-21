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
package se.swedenconnect.bankid.idp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;

/**
 * Configuration properties for UI overrides.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class OverrideProperties {

  /**
   * Path to the directory where UI override files can be found.
   */
  @Getter
  @Setter
  private String directoryPath;

  /**
   * Path to the icon to be displayed in footer
   */
  @Getter
  @Setter
  private Resource svgLogo;
}
