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
package se.swedenconnect.bankid.rpapi.service.impl;

import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;
/**
 * Body Extractors for BankIdErrors
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class BankIdErrorBodyExtractors {

  /**
   *
   * @return Function for extracting a User Error Body
   */
  public static Function<ClientResponse, Mono<? extends Throwable>> userErrorBodyExtractor() {
    return c -> {
      return c.body(BodyExtractors.toMono(HashMap.class)).map(m -> {
        final String errorCode = (String) m.get("errorCode");
        if (Objects.nonNull(errorCode)) {
          return new BankIdUserException(ErrorCode.forValue(errorCode), "Error to communicate with BankID API response:" + m.toString());
        }
        return new BankIdUserException("Error to communicate with BankID API response:" + m.toString());
      });
    };
  }

  /**
   *
   * @return Function for extracting a Server Error Body
   */
  public static Function<ClientResponse, Mono<? extends Throwable>> serverErrorBodyExtractor() {
    return c -> {
      return c.body(BodyExtractors.toMono(HashMap.class)).map(m -> {
        final String errorCode = (String) m.get("errorCode");
        if (Objects.nonNull(errorCode)) {
          return new BankIdUserException(ErrorCode.forValue(errorCode), "Error to communicate with BankID API response:" + m.toString());
        }
        return new BankIdServerException("Error to communicate with BankID API response:" + m.toString());
      });
    };
  }
}
