package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
/**
 * Creates redirect views and links to be used by error handlers
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@AllArgsConstructor
@Component
@Slf4j
public class UserErrorRouteFactory {

  private final UserErrorProperties properties;

  public enum ErrorMessage {
    TIMEOUT("timeout"),
    UNKNOWN("unknown");
    private final String message;

    ErrorMessage(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }

  public String getRedirectView(Exception e, HttpServletRequest request) {
    ErrorMessage errorMessage = getErrorMessage(e);
    String traceId = getTraceId(e);
    return "redirect:/bankid#/error/%s".formatted(build(errorMessage, traceId));
  }

  public String getRedirect(Exception e, HttpServletRequest request) {
    ErrorMessage errorMessage = getErrorMessage(e);
    String traceId = getTraceId(e);
    return "bankid#/error/%s".formatted(build(errorMessage, traceId));
  }

  public String getTraceId(Exception e) {
    if (e instanceof BankIdAuthenticationException bankIdAuthenticationException) {
      return bankIdAuthenticationException.getIdentifier();
    }
    return UUID.randomUUID().toString();
  }

  private String build(ErrorMessage message, String traceId) {
    StringBuilder builder = new StringBuilder();
    builder.append(message.getMessage());
    if (properties.getShowTraceId()) {
      builder.append("/%s".formatted(traceId));
    }
    return builder.toString();
  }

  private static ErrorMessage getErrorMessage(Exception e) {
    return ErrorMessage.UNKNOWN;
  }
}
