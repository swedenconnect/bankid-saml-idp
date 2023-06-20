package se.swedenconnect.bankid.idp.concurrency;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

/**
 * Handles per user locking for the polling critical section
 */
@Component
@AllArgsConstructor
public class LockingFilter extends OncePerRequestFilter {

  private final LockRepository locks;

  /**
   * Checks if the request is "/api/poll", and locks the same user from accessing the resource
   * If the resources is already being accessed, the api will return a HTTP-Status 429
   *
   * @param request Request to be handled
   * @param response Response to be sent to user
   * @param filterChain filterChain of remaining filters
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

    if (request.getServletPath().contains("/api/poll")) {
      HttpSession session = request.getSession();
      String key = session.getId();
      Optional<Lock> lock = getAcquiredLock(key);
      if (lock.isPresent()) {
        Lock preAcquiredLock = lock.get();
        try {
          filterChain.doFilter(request, response);
        } finally {
          preAcquiredLock.unlock();
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
   * Sets header flags and writes error message to be sent to user
   *
   * @param response the response to be returned
   * @throws IOException
   */
  private static void handleError(HttpServletResponse response) throws IOException {
    response.setStatus(429);
    response.addHeader("Retry-After", "1");
    PrintWriter writer = response.getWriter();
    writer.write("""
        {"Error": "The resource is busy for current user, try again soon"}
        """);
  }

  /**
   * @param key Unique identifier for this session
   * @return A pre acquired lock if the resource is not busy, empty if the resource is busy
   */
  private Optional<Lock> getAcquiredLock(String key) {
    try {
      return Optional.of(locks.get(key));
    } catch (LockAlreadyAcquiredException e) {
      return Optional.empty();
    }
  }
}
