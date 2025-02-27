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

import java.util.Map;
import java.util.Optional;

import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Representation of a message override.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class MessageOverride {

  /**
   * The message code.
   */
  @Getter
  @Setter
  private String code;

  /**
   * Mappings of text where the key is the language code (e.g. "sv") and the value the text (in that language).
   */
  @Getter
  @Setter
  private Map<String, String> text;

  /**
   * Default constructor.
   */
  public MessageOverride() {
  }

  /**
   * Constructor.
   *
   * @param code the message code
   * @param text the text
   */
  public MessageOverride(final String code, final Map<String, String> text) {
    this.code = Optional.ofNullable(code)
        .filter(StringUtils::hasText)
        .orElseThrow(() -> new IllegalArgumentException("code must be set"));
    this.text = Optional.ofNullable(text)
        .filter(t -> !t.isEmpty())
        .orElseThrow(() -> new IllegalArgumentException("text must be set and non-empty"));
  }

}
