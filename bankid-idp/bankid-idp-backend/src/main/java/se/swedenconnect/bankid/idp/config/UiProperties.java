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
package se.swedenconnect.bankid.idp.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import lombok.Getter;
import lombok.Setter;
import se.swedenconnect.bankid.idp.authn.DisplayText;

/**
 * Configuration properties concerning the BankID IdP UI (including texts displayed in the BankID app).
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class UiProperties implements InitializingBean {

  /**
   * Default text(s) to display during authentication/signing.
   */
  @Getter
  @Setter
  private UserMessageProperties userMessageDefaults;

  /**
   * UI properties for how to display errors for the user.
   */
  @Getter
  @Setter
  private UserErrorProperties userError;

  /** {@inheritDoc} */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (this.userMessageDefaults == null) {
      this.userMessageDefaults = new UserMessageProperties();
    }
    if (this.userError == null) {
      this.userError = new UserErrorProperties();
    }
  }

  /**
   * Texts to display during authentication and signature.
   */
  public static class UserMessageProperties implements InitializingBean {

    /**
     * Text to display when authenticating.
     */
    @Getter
    @Setter
    private DisplayText loginText;

    /**
     * If no SignMessage extension was received in the AuthnRequest message, this text will be displayed.
     */
    @Getter
    @Setter
    private DisplayText fallbackSignText;

    /** {@inheritDoc} */
    @Override
    public void afterPropertiesSet() throws Exception {
      if (this.loginText != null) {
        Assert.hasText(this.loginText.getText(), "Missing text field for login-text");
      }
      if (this.fallbackSignText != null) {
        Assert.hasText(this.fallbackSignText.getText(), "Missing text field for fallback-sign-text");
      }
    }

  }

  /**
   * Configuration properties for information to display to the user when errors have occurred.
   */
  public static class UserErrorProperties {

    /**
     * E-mail address to use in the UI. For example to report errors.
     */
    @Getter
    @Setter
    private String contactEmail;

    /**
     * Predicate that tells whether contact information should be displayed in the UI.
     */
    @Getter
    @Setter
    private boolean showContactInformation = false;

    /**
     * Whether an ID should be displayed for the user when an error has occurred. Using this ID, the user can contact
     * user support.
     */
    @Getter
    @Setter
    private boolean showTraceId = false;

  }

}
