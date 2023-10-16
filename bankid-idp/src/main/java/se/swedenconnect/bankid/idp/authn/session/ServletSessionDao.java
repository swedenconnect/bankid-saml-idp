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
 * Implements the {@link SessionDao} interface using the session extracted from the {@link HttpServletRequest}.
 * If using Spring session the session objects will be distributed.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class ServletSessionDao implements SessionDao {

  /** {@inheritDoc} */
  @Override
  public void write(final String key, final Object value, final HttpServletRequest request) {
    request.getSession().setAttribute(key, value);
  }

  /** {@inheritDoc} */
  @Override
  public <T> T read(final String key, final Class<T> tClass, final HttpServletRequest request) {
    return tClass.cast(request.getSession().getAttribute(key));
  }

  /** {@inheritDoc} */
  @Override
  public void remove(final String key, final HttpServletRequest request) {
    request.getSession().setAttribute(key, null);
  }
}
