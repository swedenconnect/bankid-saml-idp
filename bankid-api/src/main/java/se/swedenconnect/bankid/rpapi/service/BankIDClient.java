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
package se.swedenconnect.bankid.rpapi.service;

import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.rpapi.types.BankIDException;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;
import se.swedenconnect.bankid.rpapi.types.UserCancelException;

/**
 * An interface that declares the methods for a BankID Relying Party (client).
 *
 * @author Martin Lindström
 */
public interface BankIDClient {

  /**
   * The unique identifier for this client. This is mainly important when we use more than one client, for example if we
   * implement an IdP that serves several relying parties, and each RP has a client of its own.
   *
   * @return the unique identifier
   */
  String getIdentifier();

  /**
   * Initiates an authentication operation.
   *
   * @param request the authentication request parameters
   * @return an {@link OrderResponse}
   */
  Mono<OrderResponse> authenticate(final AuthenticateRequest request);

  /**
   * Initiates a signature operation.
   *
   * @param request the signature request parameters
   * @return an {@link OrderResponse}
   */
  Mono<OrderResponse> sign(final SignatureRequest request);

  /**
   * Cancels an ongoing order.
   *
   * @param orderReference the order reference
   * @throws BankIDException for errors
   */
  Mono<Void> cancel(final String orderReference);

  /**
   * Collects the result from {@link #authenticate(AuthenticateRequest)} or {@link #sign(SignatureRequest)}.
   *
   * @param orderReference the unique order reference
   * @return a collect response object
   * @throws UserCancelException if the user cancels the operation
   * @throws BankIDException for errors
   */
  Mono<? extends CollectResponse> collect(final String orderReference);

  /**
   * Returns the QR generator that should be used to generate QR codes.
   *
   * @return a {@link QRGenerator} or {@code null} if no QR code generator has been configured
   */
  QRGenerator getQRGenerator();

}
