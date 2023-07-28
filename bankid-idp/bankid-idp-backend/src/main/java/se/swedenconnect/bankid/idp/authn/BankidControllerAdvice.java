package se.swedenconnect.bankid.idp.authn;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.concurrent.ThreadLocalRandom;

@RestControllerAdvice
public class BankidControllerAdvice  {

    public static final String ERROR_TECHNICAL_DIFFICULTIES_BUSY = """
      {"Error": "This service is currently experiencing some issues" }
      """;
    @ExceptionHandler(value = {CallNotPermittedException.class})
    public ResponseEntity<String> handleException(Exception e) {
        int random = ThreadLocalRandom.current().nextInt(5) + 1;
        final int delay = 5 + random;
        return ResponseEntity
                .status(429)
                .header("retry-after", Integer.toString(delay))
                .header("content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(ERROR_TECHNICAL_DIFFICULTIES_BUSY);

    }
}
