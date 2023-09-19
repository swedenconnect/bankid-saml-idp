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
package se.swedenconnect.bankid.idp.concurrency;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * A {@link Filter} that handles per user locking for the polling critical section.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Component
@Slf4j
public class LockingFilter extends OncePerRequestFilter {

  /** The path to protect. */
  public static final String POLLING_PATH = "/api/poll";

  public static final String ERROR_RESOURCE_BUSY = """
      {"Error": "The resource is busy for current user, try again soon"}
      """;

  private final TryLockRepository locks;

  /**
   * Constructor.
   *
   * @param locks the lock repository
   */
  public LockingFilter(final TryLockRepository locks) {
    this.locks = Objects.requireNonNull(locks, "locks must not be null");
  }

  /**
   * Checks if the request is {@value #POLLING_PATH}, and locks the same user from accessing the resource. If the
   * resources is already being accessed, the api will return a HTTP-Status 429.
   */
  @Override
  protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
      final FilterChain filterChain) throws ServletException, IOException {

    if (request.getServletPath().contains(POLLING_PATH)) {
      final HttpSession session = request.getSession();
      // Keyformat is lock:/path/:sessionID
      final String key = "lock:%s:%s".formatted(POLLING_PATH, session.getId());
      final TryLock lock = this.locks.get(key);
      if (lock.tryLock()) {
        try {
          filterChain.doFilter(request, response);
        }
        finally {
          lock.unlock();
        }
      }
      else {
        // The resource is busy, send an error to the user
        // Do not continue the filter chain
        handleError(response);
      }
    }
    else {
      filterChain.doFilter(request, response);
    }
  }

  /**
   * Sets header flags and writes error message to be sent to user.
   *
   * @param response the response to be returned
   * @throws IOException for IO errors
   */
  private static void handleError(final HttpServletResponse response) throws IOException {
    log.info("Failed to acquire lock, resource busy");
    response.setStatus(429);
    response.addHeader("Retry-After", "1");
    final PrintWriter writer = response.getWriter();
    writer.write(ERROR_RESOURCE_BUSY);
  }

}
