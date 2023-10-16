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

import jakarta.servlet.http.HttpServletRequest;

/**
 * The Session Data Access Object interface.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public interface SessionDao {

  /**
   * Writes object {@code value} under the key {@code key}.
   *
   * @param key the object key
   * @param value the object
   * @param request the HTTP servlet request
   */
  void write(final String key, final Object value, final HttpServletRequest request);

  /**
   * Reads the object identified by {@code key} of type {@code tClass}.
   *
   * @param <T> the type
   * @param key the object key
   * @param tClass the type of the object to read
   * @param request the HTTP servlet request
   * @return the object, or {@code null} if none is available
   */
  <T> T read(final String key, final Class<T> tClass, final HttpServletRequest request);

  /**
   * Removes the object identified by {@code key}.
   *
   * @param key the object key
   * @param request the HTTP servlet request
   */
  void remove(final String key, final HttpServletRequest request);
}
