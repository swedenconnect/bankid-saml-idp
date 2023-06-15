package se.swedenconnect.bankid.idp.concurrency;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.locks.Lock;

@Component
@AllArgsConstructor
public class LockingFilter extends OncePerRequestFilter {

  private final LockRepository locks;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    if (request.getServletPath().contains("/api/poll")) {
      HttpSession session = request.getSession();
      String key = session.getId();
      Lock lock = locks.get(key);
      boolean lockAcquired = false;// lock.tryLock();
      if (lockAcquired) {
        try {
          filterChain.doFilter(request, response);
        } finally {
          lock.unlock();
        }
      }
      else {
        response.setStatus(429);
        response.addHeader("Retry-After", "1");
        PrintWriter writer = response.getWriter();
        writer.write("""
            {"Error": "Something went wrong"}
            """);
      }
    }
    else {
      filterChain.doFilter(request, response);
    }
  }
}
