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

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.swedenconnect.bankid.idp.authn.annotations.ApiController;

/**
 * Controller advice for api error handling.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@RestControllerAdvice(annotations = ApiController.class)
@AllArgsConstructor
@Slf4j
public class ApiControllerAdvice {

  private final UserErrorFactory userErrorFactory;

  public static final String ERROR_TECHNICAL_DIFFICULTIES_BUSY = """
      {"Error": "This service is currently experiencing some issues" }
      """;

  /**
   * Handles exception for CallNotPermitted (resilience4j)
   * @return an error response with a randomized backoff timer for the frontend to handle
   */
  @ExceptionHandler(value = {CallNotPermittedException.class})
  public ResponseEntity<String> handleException() {
    final int random = ThreadLocalRandom.current().nextInt(5) + 1;
    final int delay = 5 + random;
    return ResponseEntity
        .status(429)
        .header("retry-after", Integer.toString(delay))
        .header("content-type", MediaType.APPLICATION_JSON_VALUE)
        .body(ERROR_TECHNICAL_DIFFICULTIES_BUSY);
  }

  /**
   * Handles uncaught exceptions for /api routes
   * @param e Uncaught exception to handle
   * @return redirect to error view (as json)
   */
  @ExceptionHandler(value = {Exception.class})
  public ResponseEntity<UserErrorResponse> defaultHandler(final Exception e) {
    log.error("Generic API exception handler used for exception:{}", e.getClass().getCanonicalName(), e);
    UserErrorResponse userError = this.userErrorFactory.getUserError(e);
    return ResponseEntity
        .status(400)
        .header("content-type", MediaType.APPLICATION_JSON_VALUE)
        .body(userError);
  }
}
