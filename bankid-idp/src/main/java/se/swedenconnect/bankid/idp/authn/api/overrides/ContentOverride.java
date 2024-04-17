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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Representation of a content override.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class ContentOverride {

  /**
   * The position where the context should be inserted.
   */
  public enum Position {
    /** Above the main content box on all screens. */
    ABOVE,

    /** Below the main content box on all screens. */
    BELOW,

    /** In the top of the main content box on the device select screen. */
    DEVICESELECT,

    /** In the top of the main content box on the "Other device" screen, showing the QR code. */
    QRCODE,

    /** In the top of the main content box on the "This device" screen, trying to autostart the BankID app. */
    AUTOSTART
  }

  /**
   * The type of alert box that should be inserted.
   */
  public enum Type {

    /** Info alert box. */
    INFO,

    /** Warning alert box. */
    WARNING
  }

  /**
   * The message code for the title of the alert box.
   */
  @Getter
  @Setter
  private String title;

  /**
   * The type of alert box.
   */
  @Getter
  @Setter
  private Type type;

  /**
   * The position of the alert box.
   */
  @Getter
  @Setter
  private Position position;

  @Getter
  @Setter
  private List<ContentEntry> content;
  /**
   * Default constructor.
   */
  public ContentOverride() {
  }

  /**
   * Constructor.
   *
   * @param title the message code for the title of the alert box
   * @param content list of messages that may include an optional link
   * @param type the type of alert box
   * @param position the position of the alert box
   */
  public ContentOverride(final String title, final List<ContentEntry> content, final Type type, final Position position) {
    this.title = Optional.ofNullable(title)
        .filter(StringUtils::hasText)
        .orElseThrow(() -> new IllegalArgumentException("Title must be set"));
    this.content = Optional.ofNullable(content)
            .orElseThrow(() -> new IllegalArgumentException("Content must be set"));
    boolean textSet = this.content.stream().allMatch(c -> Objects.nonNull(c.getText()));
    if (!textSet) {
      throw new IllegalArgumentException("Text must be set for every entry");
    }
    this.type = Objects.requireNonNull(type, "Type must be set");
    this.position = Objects.requireNonNull(position, "Position must be set");
  }

}
