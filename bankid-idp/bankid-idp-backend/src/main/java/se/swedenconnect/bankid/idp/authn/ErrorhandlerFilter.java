package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ErrorhandlerFilter handles error that do not originate from a controller e.g. another filter
 * Handles exceptions thrown by BankIdAuthenticationProvider @see {@link BankIdAuthenticationProvider}
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */

@AllArgsConstructor
@Component(ErrorhandlerFilter.NAME)
public class ErrorhandlerFilter extends OncePerRequestFilter {

  private final UserErrorRouteFactory userErrorRouteFactory;
  public static final String NAME = "ERROR_HANDLER_FILTER";

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
    try {
      filterChain.doFilter(request, response);
    } catch (RuntimeException e) {
      response.sendRedirect(userErrorRouteFactory.getRedirect(e, request));
    }
  }
}
