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
package se.swedenconnect.bankid.idp.authn;

import lombok.Getter;
import lombok.Setter;

/**
 * Class that represents text to be displayed for the user (during authenticate or sign).
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class DisplayText {

  /**
   * Format of the display text.
   */
  public static enum TextFormat {

    /**
     * Plain text.
     */
    PLAIN_TEXT,

    /**
     * Simple Markdown v1 according to https://www.bankid.com/utvecklare/guider/formatera-text.
     */
    SIMPLE_MARKDOWN_V1;
  }

  /**
   * The text.
   */
  @Getter
  @Setter
  private String text;

  /**
   * The format (defaults to {@link TextFormat#PLAIN_TEXT}.
   */
  @Getter
  @Setter
  private TextFormat format = TextFormat.PLAIN_TEXT;

}
