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
package se.swedenconnect.bankid.rpapi.service.impl;

import java.util.function.IntPredicate;
/**
 * Predicates for matching status codes
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class StatusCodePredicates {
  public static IntPredicate compareBetween(final int from, final int to) {
    return (toCompare) -> {
      return toCompare >= from && toCompare < to;
    };
  }

  /**
   *
   * @return Predicate to determine user error
   */
  public static IntPredicate userError() {
    return compareBetween(400, 500);
  }

  /**
   *
   * @return Predicate to determine server error
   */
  public static IntPredicate serverError() {
    return compareBetween(500, 600);
  }
}
