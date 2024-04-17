/*
 * Copyright 2023-2024 Sweden Connect
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
package se.swedenconnect.bankid.idp.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

public class BankIdApiMock {
  private static final WireMockServer server;

  static {
    WireMockConfiguration wireMockConfiguration = WireMockConfiguration.options()
        .containerThreads(20)
        .port(9000);

    server = new WireMockServer(wireMockConfiguration);
    server.start();
  }

  public static void mockAuth(OrderResponse orderResponse) {
    String json = BankIdResponseFactory.serialize(orderResponse);
    CollectResponse first = BankIdResponseFactory.collect(orderResponse);
    String collectJson = BankIdResponseFactory.serialize(first);
    server.stubFor(post("/auth").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
    server.stubFor(
        post("/collect").withRequestBody(matchingJsonPath("$.orderRef", equalTo(orderResponse.getOrderReference())))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(collectJson)));
  }

  public static void nextCollect(CollectResponse collectResponse) {
    String json = BankIdResponseFactory.serialize(collectResponse);
    server.stubFor(
        post("/collect").withRequestBody(matchingJsonPath("$.orderRef", equalTo(collectResponse.getOrderReference())))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  public static void failStart(OrderResponse toExpire, OrderResponse next) {
    CollectResponse expired = BankIdResponseFactory.failStart(toExpire);
    String expiredJson = BankIdResponseFactory.serialize(expired);
    server
        .stubFor(post("/collect").withRequestBody(matchingJsonPath("$.orderRef", equalTo(toExpire.getOrderReference())))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(expiredJson)));
    server.stubFor(post("/auth").willReturn(
        aResponse().withHeader("Content-Type", "application/json").withBody(BankIdResponseFactory.serialize(next))));
    server.stubFor(post("/collect").withRequestBody(matchingJsonPath("$.orderRef", equalTo(next.getOrderReference())))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withBody(BankIdResponseFactory.serialize(BankIdResponseFactory.collect(next)))));
  }

  public static CollectResponse completeCollect(final OrderResponse orderResponse) throws JsonProcessingException {
    CollectResponse complete = BankIdResponseFactory.complete(orderResponse);
    String json = BankIdResponseFactory.serialize(complete);
    server.stubFor(
        post("/collect").withRequestBody(matchingJsonPath("$.orderRef", equalTo(orderResponse.getOrderReference())))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
    return complete;
  }

  public static void setDelay(int millis) {
    server.setGlobalFixedDelay(millis);
  }

  public static void resetDelay() {
    server.setGlobalFixedDelay(0);
  }

  public static void mockSign(OrderResponse orderResponse) {
    String json = BankIdResponseFactory.serialize(orderResponse);
    CollectResponse first = BankIdResponseFactory.collect(orderResponse);
    String collectJson = BankIdResponseFactory.serialize(first);
    server.stubFor(post("/sign").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
    server.stubFor(
        post("/collect").withRequestBody(matchingJsonPath("$.orderRef", equalTo(orderResponse.getOrderReference())))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(collectJson)));
  }

  public static void mockCancel(OrderResponse orderResponse) {
    server.stubFor(
        post("/cancel").withRequestBody(matchingJsonPath("$.orderRef", equalTo(orderResponse.getOrderReference()))));
  }
}
