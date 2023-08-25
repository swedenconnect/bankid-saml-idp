package se.swedenconnect.bankid.rpapi.service.impl;

import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.function.Function;

public class BankIdErrorBodyExtractors {
  public static Function<ClientResponse, Mono<? extends Throwable>> userErrorBodyExtractor() {
    return c -> {
      return c.body(BodyExtractors.toMono(HashMap.class)).map(m -> {
        return new BankIdUserException("Error to communicate with BankID API response:" + m.toString());
      });
    };
  }

  public static Function<ClientResponse, Mono<? extends Throwable>> serverErrorBodyExtractor() {
    return c -> {
      return c.body(BodyExtractors.toMono(HashMap.class)).map(m -> {
        return new BankIdServerException("Error to communicate with BankID API response:" + m.toString());
      });
    };
  }
}
