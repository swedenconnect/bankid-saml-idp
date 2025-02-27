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
package se.swedenconnect.bankid.idp.rp;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * The UI info for a Relying Party is normally extracted from the SAML metadata, but there are cases where you may want
 * to manually configure these data elements (for example if the metadata does not contain this information, or you
 * simply want to override it). This class holds this information.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class RelyingPartyUiInfo {

  /**
   * A mapping between language codes and display names.
   */
  @Getter
  @Setter
  private Map<String, String> displayName;

  /**
   * The URL for the SP's logotype.
   */
  @Getter
  @Setter
  private String logotypeUrl;

  /**
   * Whether the data in this object should be used as a fallback to UI information gathered from the SAML metadata or
   * not. If {@code true}, the data will only be used if data is not present in SAML metadata, and if {@code false}, the
   * data from this object will have precedence over data found in SAML metadata.
   */
  @Getter
  @Setter
  private boolean useAsFallback = true;

}
