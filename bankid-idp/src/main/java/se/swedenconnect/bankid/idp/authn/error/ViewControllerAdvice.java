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
package se.swedenconnect.bankid.idp.authn.error;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import se.swedenconnect.bankid.idp.authn.annotations.ViewController;

/**
 * Controller advice for view error handling.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@RestControllerAdvice(annotations = ViewController.class)
@AllArgsConstructor
@Slf4j
public class ViewControllerAdvice {

  private final UserErrorFactory userErrorFactory;

  /**
   * Handles uncaught exceptions for /view routes
   * @param e Uncaught exception to handle
   * @return redirect to error view
   */
  @ExceptionHandler(value = {Exception.class})
  public ModelAndView defaultHandler(final Exception e) {
    log.error("Generic view exception handler used for exception:{}", e.getClass().getCanonicalName(), e);
    String redirectView = this.userErrorFactory.getRedirectView(e);
    return new ModelAndView(redirectView);
  }
}
