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
package se.swedenconnect.bankid.idp.authn;

import se.swedenconnect.bankid.idp.config.UiProperties.UserErrorProperties;

public class UserErrorPropertiesFixture {

  public static final UserErrorProperties EMPTY_PROPERTIES = new UserErrorProperties();
  public static UserErrorProperties SHOW_EMAIL_NO_TRACE;
  public static UserErrorProperties SHOW_EMAIL_SHOW_TRACE;

  static {
    SHOW_EMAIL_NO_TRACE = new UserErrorProperties();
    SHOW_EMAIL_NO_TRACE.setContactEmail("contact@email.com");
    SHOW_EMAIL_NO_TRACE.setShowContactInformation(true);

    SHOW_EMAIL_SHOW_TRACE = new UserErrorProperties();
    SHOW_EMAIL_SHOW_TRACE.setContactEmail("contact@email.com");
    SHOW_EMAIL_SHOW_TRACE.setShowContactInformation(true);
    SHOW_EMAIL_SHOW_TRACE.setShowTraceId(true);
  }
}
