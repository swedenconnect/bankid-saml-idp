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

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller advice for error handling.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@RestControllerAdvice
@AllArgsConstructor
@Slf4j
public class BankidControllerAdvice {

  private final UserErrorRouteFactory userErrorRouteFactory;

  public static final String ERROR_TECHNICAL_DIFFICULTIES_BUSY = """
      {"Error": "This service is currently experiencing some issues" }
      """;

  @ExceptionHandler(value = { CallNotPermittedException.class })
  public ResponseEntity<String> handleException(final CallNotPermittedException e) {
    final int random = ThreadLocalRandom.current().nextInt(5) + 1;
    final int delay = 5 + random;
    return ResponseEntity
        .status(429)
        .header("retry-after", Integer.toString(delay))
        .header("content-type", MediaType.APPLICATION_JSON_VALUE)
        .body(ERROR_TECHNICAL_DIFFICULTIES_BUSY);
  }

  @ExceptionHandler(value = { NoHandlerFoundException.class })
  public ModelAndView handleException(final NoHandlerFoundException e) {
    return new ModelAndView(this.userErrorRouteFactory.getRedirectView(e));
  }

  @ExceptionHandler(value = { Exception.class })
  public ModelAndView defaultHandler(final Exception e) {
    log.error("Generic exception handler used for exception:{}", e.getClass().getCanonicalName(), e);
    return new ModelAndView(this.userErrorRouteFactory.getRedirectView(e));
  }
}
