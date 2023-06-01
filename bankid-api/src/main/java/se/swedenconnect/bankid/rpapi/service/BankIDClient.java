/*
 * Copyright 2023 Sweden Connect
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
import se.swedenconnect.bankid.rpapi.types.*;

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
   * Request an authentication order. The {@link #collect(String)} method is used to query the status of the order.
   * 
   * @param personalIdentityNumber the ID number of the user trying to be authenticated (optional). If the ID number is
   *          omitted the user must use the same device and the client must be started with the autoStartToken returned
   *          in orderResponse
   * @param endUserIp the user IP address as seen by the relying party
   * @param userVisibleData data to display to the user during authentication (optional)
   * @param requirement used by the relying party to set requirements how the authentication or sign operation must be
   *          performed. Default rules are applied if omitted
   * @return an order response
   * @throws BankIDException for errors
   */
  Mono<OrderResponse> authenticate(final String personalIdentityNumber, final String endUserIp,
                                   final UserVisibleData userVisibleData, final Requirement requirement);

  /**
   * Request a signing order. The {@link #collect(String)} method is used to query the status of the order.
   * 
   * @param personalIdentityNumber the ID number of the user trying to be authenticated (optional). If the ID number is
   *          omitted the user must use the same device and the client must be started with the autoStartToken returned
   *          in orderResponse
   * @param endUserIp the user IP address as seen by the relying party
   * @param dataToSign the data to sign
   * @param requirement used by the relying party to set requirements how the authentication or sign operation must be
   *          performed. Default rules are applied if omitted
   * @return an order response
   * @throws BankIDException for errors
   */
  Mono<OrderResponse> sign(final String personalIdentityNumber, final String endUserIp,
      final DataToSign dataToSign, final Requirement requirement);

  /**
   * Cancels an ongoing order.
   * 
   * @param orderReference the order reference
   * @throws BankIDException for errors
   */
  Mono<Void> cancel(final String orderReference);

  /**
   * Collects the result from {@link #authenticate(String, String, Requirement)} or
   * {@link #sign(String, String, DataToSign, Requirement)}.
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
