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

import static se.swedenconnect.bankid.idp.authn.BankIdAuthenticationController.AUTHN_PATH;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * If we are running in "standalone" mode, i.e., if we are using the built in Vue frontend app, this controller
 * redirects calls made from the underlying SAML IdP library to our frontend start page.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Controller
@ConditionalOnProperty(value = "bankid.builtinfrontend.disable", havingValue = "false", matchIfMissing = true)
public class FrontendController {

  /**
   * The entry point for the BankID authentication/signature process.
   *
   * @return a {@link ModelAndView}
   */
  @GetMapping(AUTHN_PATH)
  public ModelAndView view() {
    return new ModelAndView("index");
  }
}
