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

package se.swedenconnect.bankid.idp.authn.api.overrides;

import lombok.Getter;
import lombok.Setter;

public class ContentEntry {
  /** Constructor
   *
   * @param text the message code for the text that should appear in the alert box
   * @param link optional link, if set the text should be handled as link text
   */
  public ContentEntry(String text, String link) {
    this.text = text;
    this.link = link;
  }

  /**
   * The message code for the text that should appear in the alert box.
   */
  @Getter
  @Setter
  private String text;

  /**
   * Optional link for the alert box
   */
  @Getter
  @Setter
  private String link;
}
