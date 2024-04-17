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
package se.swedenconnect.bankid.idp.authn.api.overrides;

import java.util.Optional;

import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Representation of a CSS override.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class CssOverride {

  /**
   * The CSS style to override.
   */
  @Getter
  @Setter
  private String style;

  /**
   * Default constructor.
   */
  public CssOverride() {
  }

  /**
   * Constructor.
   *
   * @param style the CSS style
   */
  public CssOverride(final String style) {
    this.style = Optional.ofNullable(style)
        .filter(StringUtils::hasText)
        .orElseThrow(() -> new IllegalArgumentException("style must be set"));
  }

}
