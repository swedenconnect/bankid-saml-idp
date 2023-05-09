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
package se.swedenconnect.bankid.rpapi.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

/**
 * The implementation of {@code BankIDMessage}.
 *
 * @author Martin Lindström
 */
public class BankIDMessageImpl implements BankIDMessage {

  /** The message short name. */
  private final ShortName shortName;

  /** The message code(s) for rendering text. */
  private final List<String> messageCodes;

  /**
   * Constructor.
   *
   * @param shortName the message short name
   * @param messageCodes the message code(s)
   */
  public BankIDMessageImpl(final ShortName shortName, final String... messageCodes) {
    Assert.notNull(shortName, "shortName must not be null");
    Assert.notEmpty(messageCodes, "messageCodes must not be empty");
    this.shortName = shortName;
    this.messageCodes = Arrays.asList(messageCodes);
  }

  /**
   * Constructor.
   *
   * @param shortName the message short name
   * @param messageCodes the message code(s)
   */
  public BankIDMessageImpl(final ShortName shortName, final List<String> messageCodes) {
    Assert.notNull(shortName, "shortName must not be null");
    Assert.notEmpty(messageCodes, "messageCodes must not be empty");
    this.shortName = shortName;
    this.messageCodes = messageCodes;
  }

  /** {@inheritDoc} */
  @Override
  public ShortName getShortName() {
    return this.shortName;
  }

  /** {@inheritDoc} */
  @Override
  public List<String> getMessageCodes() {
    return Collections.unmodifiableList(this.messageCodes);
  }

}
