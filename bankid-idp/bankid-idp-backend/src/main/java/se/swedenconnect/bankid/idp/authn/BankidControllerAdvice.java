package se.swedenconnect.bankid.idp.authn;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.concurrent.ThreadLocalRandom;

@RestControllerAdvice
public class BankidControllerAdvice  {
    @ExceptionHandler(value = {CallNotPermittedException.class})
    public ResponseEntity<Mono<Void>> handleException(Exception e) {
        int random = ThreadLocalRandom.current().nextInt(5) + 1;
        final int delay = 5 + random;
        return ResponseEntity.status(429).header("retry-after", Integer.toString(delay)).body(Mono.empty());
    } // TODO: 2023-07-28 Write error message
}
