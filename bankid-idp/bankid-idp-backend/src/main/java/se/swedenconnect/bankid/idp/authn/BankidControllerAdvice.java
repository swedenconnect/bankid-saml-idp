package se.swedenconnect.bankid.idp.authn;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ThreadLocalRandom;

@RestControllerAdvice
@AllArgsConstructor
@Slf4j
public class BankidControllerAdvice {

  private final UserErrorRouteFactory userErrorRouteFactory;

  public static final String ERROR_TECHNICAL_DIFFICULTIES_BUSY = """
      {"Error": "This service is currently experiencing some issues" }
      """;

  @ExceptionHandler(value = {CallNotPermittedException.class})
  public ResponseEntity<String> handleException(CallNotPermittedException e) {
    int random = ThreadLocalRandom.current().nextInt(5) + 1;
    final int delay = 5 + random;
    return ResponseEntity
        .status(429)
        .header("retry-after", Integer.toString(delay))
        .header("content-type", MediaType.APPLICATION_JSON_VALUE)
        .body(ERROR_TECHNICAL_DIFFICULTIES_BUSY);
  }

  @ExceptionHandler(value = {NoHandlerFoundException.class})
  public ModelAndView handleException(NoHandlerFoundException e, HttpServletRequest request) {
    return new ModelAndView(userErrorRouteFactory.getRedirectView(e, request));
  }

  @ExceptionHandler(value = {Exception.class})
  public ModelAndView defaultHandler(Exception e, HttpServletRequest request) {
    log.error("Generic exception handler used for exception:{}", e.getClass().getCanonicalName(), e);
    return new ModelAndView(userErrorRouteFactory.getRedirectView(e, request));
  }
}
