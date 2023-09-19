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
package se.swedenconnect.bankid.idp.authn.session;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Container for multiple BankID session states.
 * <p>
 * This is construct serves the purpose of holding all sessions that gets started when a user starts authentication for
 * the strict purpose of being able to refer to the first initial session as well as the most current one.
 * </p>
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class BankIdSessionState {

  private final Deque<BankIdSessionData> bankIdSessionDataStack;

  /**
   * Constructor.
   */
  public BankIdSessionState() {
    this.bankIdSessionDataStack = new ArrayDeque<>();
  }

  /**
   * Operation to insert a new session data.
   *
   * @param data new session data
   */
  public void push(final BankIdSessionData data) {
    this.bankIdSessionDataStack.push(data);
  }

  /**
   * Operation to remove the most current session data.
   *
   * @return old session data
   */
  public BankIdSessionData pop() {
    return this.bankIdSessionDataStack.removeFirst();
  }

  /**
   * Gets the most current session data.
   * 
   * @return most current bankid session data
   */
  public BankIdSessionData getBankIdSessionData() {
    return this.bankIdSessionDataStack.getFirst();
  }

  /**
   * Gets the instant for the first response.
   * 
   * @return point in time of the first response
   */
  public Instant getInitialOrderTime() {
    return this.bankIdSessionDataStack.getLast().getStartTime();
  }
}
