package se.swedenconnect.bankid.idp.concurrency;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handles per user locking for the polling critical section
 */
@Component
@AllArgsConstructor
@Slf4j
public class LockingFilter extends OncePerRequestFilter {

  public static final String ERROR_RESOURCE_BUSY = """
      {"Error": "The resource is busy for current user, try again soon"}
      """;
  private final TryLockRepository locks;

  /**
   * Checks if the request is "/api/poll", and locks the same user from accessing the resource
   * If the resources is already being accessed, the api will return a HTTP-Status 429
   *
   * @param request     Request to be handled
   * @param response    Response to be sent to user
   * @param filterChain filterChain of remaining filters
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

    if (request.getServletPath().contains("/api/poll")) {
      HttpSession session = request.getSession();
      //Keyformat is lock:/path/:sessionID
      String key = "lock:/api/poll:%s".formatted(session.getId());
      TryLock lock = locks.get(key);
      if (lock.tryLock()) {
        try {
          filterChain.doFilter(request, response);
        } finally {
          lock.unlock();
        }
      } else {
        // The resource is busy, send an error to the user
        // Do not continue the filter chain
        handleError(response);
      }
    } else {
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
    log.info("Failed to acquire lock, resource busy");
    response.setStatus(429);
    response.addHeader("Retry-After", "1");
    PrintWriter writer = response.getWriter();
    writer.write(ERROR_RESOURCE_BUSY);
  }
}
