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

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import se.swedenconnect.bankid.idp.authn.BankIdAuthenticationProvider;

/**
 * {@code ErrorhandlerFilter} handles errors that do not originate from a controller e.g. another filter handles
 * exceptions thrown by {@link BankIdAuthenticationProvider}.
 *
 * @see BankIdAuthenticationProvider
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Component(ErrorhandlerFilter.NAME)
public class ErrorhandlerFilter extends OncePerRequestFilter {

  public static final String NAME = "ERROR_HANDLER_FILTER";

  /** Creates redirect views and links to be used by error handlers. */
  private final UserErrorFactory userErrorFactory;

  /**
   * Constructor.
   *
   * @param userErrorFactory creates redirect views and links to be used by error handlers
   */
  public ErrorhandlerFilter(final UserErrorFactory userErrorFactory) {
    this.userErrorFactory = userErrorFactory;
  }

  /** {@inheritDoc} */
  @Override
  protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
      final FilterChain filterChain) throws ServletException, IOException {

    try {
      filterChain.doFilter(request, response);
    }
    catch (final RuntimeException e) {
      response.sendRedirect(this.userErrorFactory.getRedirect(request, e));
    }
  }
}
