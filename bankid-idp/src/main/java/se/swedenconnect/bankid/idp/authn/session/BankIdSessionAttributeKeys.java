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
package se.swedenconnect.bankid.idp.authn.session;

import java.util.List;

/**
 * Keys for storing BankID objects in the session.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class BankIdSessionAttributeKeys {

  /**
   * The BankID context.
   */
  public static final String BANKID_CONTEXT = "BANKID-CONTEXT";

  /**
   * The session attribute where we store whether we selected "this device" or "other device".
   */
  public static final String PREVIOUS_DEVICE_SESSION_ATTRIBUTE = "DEVICE-SELECTION";

  /**
   * The session attribute where we store completion data for a bankid session
   */
  public static final String BANKID_COMPLETION_DATA_ATTRIBUTE = "BANKID-COMPLETION-DATA";

  /**
   * The session attribute where we store the current state of a bankid session
   */
  public static final String BANKID_STATE_ATTRIBUTE = "BANKID-STATE";

  /**
   * The session attribute where we store display message
   */
  public static final String BANKID_USER_VISIBLE_DATA_ATTRIBUTE = "BANKID-DISPLAY-MESSAGE";

  /**
   * Attributes which should not be persisted upon cancelation or completion of an order
   */
  public static final List<String> BANKID_VOLATILE_ATTRIBUTES =
      List.of(BANKID_USER_VISIBLE_DATA_ATTRIBUTE, BANKID_STATE_ATTRIBUTE, BANKID_COMPLETION_DATA_ATTRIBUTE);

  // Hidden constructor
  private BankIdSessionAttributeKeys() {
  }
}
