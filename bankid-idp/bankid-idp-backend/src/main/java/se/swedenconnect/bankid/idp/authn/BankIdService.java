package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.idp.ApiResponseFactory;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;
import se.swedenconnect.bankid.rpapi.types.Requirement;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BankIdService {

  private final BankIdEventPublisher eventPublisher;

  public Mono<ApiResponse> poll(HttpServletRequest request, Boolean qr, BankIdSessionState state, Saml2UserAuthenticationInputToken authnInputToken, BankIdContext bankIdContext, BankIDClient client) {
    return Optional.ofNullable(state)
        .map(BankIdSessionState::getBankIdSessionData)
        .map(sessionData -> collect(request, client, sessionData)
            .map(c -> BankIdSessionData.of(sessionData, c))
            .flatMap(b -> reAuthIfExpired(request, state, bankIdContext, client, b))
            .map(b -> ApiResponseFactory.create(b, client.getQRGenerator(), qr))
            .onErrorResume(this::handleError))
        .orElseGet(() -> onNoSession(request, qr, bankIdContext, client));
  }

  public Mono<Void> cancel(HttpServletRequest request, BankIdSessionState state, BankIDClient client) {
    eventPublisher.orderCancellation(request).publish();
    return client.cancel(state.getBankIdSessionData().getOrderReference());
  }

  private Mono<OrderResponse> auth(final HttpServletRequest request, String personalNumber, BankIDClient client) {
    Requirement requirement = new Requirement();
    // TODO: 2023-05-17 Requirement factory per entityId
    UserVisibleData userVisibleData = new UserVisibleData();
    userVisibleData.setUserVisibleData(new String(Base64.getEncoder().encode("Text".getBytes()), StandardCharsets.UTF_8));
    return client.authenticate(personalNumber, request.getRemoteAddr(), userVisibleData, requirement)
        .map(o -> {
          eventPublisher.orderResponse(request, o).publish();
          return o;
        });
  }

  private Mono<ApiResponse> onNoSession(HttpServletRequest request, Boolean qr, BankIdContext bankIdContext, BankIDClient client) {
    return auth(request, bankIdContext.getPersonalNumber(), client)
        .map(BankIdSessionData::of)
        .flatMap(b -> collect(request, client, b)
            .map(c -> ApiResponseFactory.create(BankIdSessionData.of(b, c), client.getQRGenerator(), qr)));
  }

  private Mono<ApiResponse> handleError(Throwable e) {
    if (e instanceof BankIdSessionExpiredException bankIdSessionExpiredException) {
      return this.sessionExpired(bankIdSessionExpiredException.getExpiredSessionHolder());
    }
    return Mono.error(e);
  }

  private Mono<BankIdSessionData> reAuthIfExpired(HttpServletRequest request, BankIdSessionState state, BankIdContext bankIdContext, BankIDClient client, BankIdSessionData b) {
    if (b.getExpired()) {
      if (Duration.between(state.getInitialOrderTime(), Instant.now()).toMinutes() >= 3) {
        return Mono.error(new BankIdSessionExpiredException(request));
      }
      return auth(request, bankIdContext.getPersonalNumber(), client)
          .map(BankIdSessionData::of);
    }
    return Mono.just(b);
  }

  private Mono<CollectResponse> collect(HttpServletRequest request, BankIDClient client, BankIdSessionData data) {
    return client.collect(data.getOrderReference())
        .map(c -> {
          eventPublisher.collectResponse(request, c).publish();
          return c;
        });
  }

  private Mono<ApiResponse> sessionExpired(HttpServletRequest request) {
    eventPublisher.orderCancellation(request).publish();
    return Mono.just(ApiResponseFactory.createErrorResponseTimeExpired());
  }
}
