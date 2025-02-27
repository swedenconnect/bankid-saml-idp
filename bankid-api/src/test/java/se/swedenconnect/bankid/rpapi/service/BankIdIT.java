/*
 * Copyright 2023-2025 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.swedenconnect.bankid.rpapi.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import se.swedenconnect.bankid.rpapi.service.impl.BankIDClientImpl;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;
import se.swedenconnect.bankid.rpapi.types.Requirement;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

public class BankIdIT {

  @Test
  void clientWillCallEndpoints() {
    mockBankId();
    WebClient webClient = WebClient.builder().baseUrl("http://localhost:9000").build();
    BankIDClient client = new BankIDClientImpl("id", webClient, null);

    Mono<OrderResponse> authenticateStep = client.authenticate(new AuthenticateRequest("1.1.1.1", new UserVisibleData(), new Requirement()));
    StepVerifier.create(authenticateStep)
        .expectNextMatches(o -> o.getOrderReference() != null)
        .verifyComplete();

    Mono<? extends CollectResponse> collectStep = authenticateStep.flatMap(o -> client.collect(o.getOrderReference()));
    StepVerifier.create(collectStep)
        .expectNextMatches(collect -> collect.getOrderReference() != null)
        .verifyComplete();

    DataToSign dataToSign = new DataToSign();
    dataToSign.setUserVisibleData("Data visible to user!");
    Mono<OrderResponse> signStep = client.sign(new SignatureRequest("1.1.1.1", dataToSign, new Requirement()));
    StepVerifier.create(signStep)
        .expectNextMatches(sign -> sign.getOrderReference() != null)
        .verifyComplete();

    Mono<Void> cancelStep = authenticateStep.flatMap(a -> client.cancel(a.getOrderReference()));
    StepVerifier.create(cancelStep)
        .verifyComplete();
  }

  private static void mockBankId() {
    WireMockServer wireMockServer = new WireMockServer(9000);
    wireMockServer.start();
    String response = """
        {
          "orderRef": "%s",
          "autoStartToken": "%s",
          "qrStartToken": "%s",
          "qrStartSecret": "%s",
          "status": "%s"
        }
        """.formatted(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        "PENDING"
    );
    wireMockServer.stubFor(post("/auth").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(response)));
    wireMockServer.stubFor(post("/collect").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(response)));
    wireMockServer.stubFor(post("/sign").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(response)));
    wireMockServer.stubFor(post("/cancel").willReturn(aResponse()));
  }
}
