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
package se.swedenconnect.bankid.idp.authn.error;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import se.swedenconnect.bankid.rpapi.types.BankIDException;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;

/**
 * Exception class errors during BankID operations that we assign a trace ID to. This is useful for display purposes.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Slf4j
public class BankIdTraceableException extends BankIDException {

  private static final long serialVersionUID = -6220282820833829340L;

  private final String orderRef;

  private final String traceId;

  /**
   * Constructor.
   *
   * @param orderRef the order reference
   * @param msg the error message
   */
  public BankIdTraceableException(final String orderRef, final String msg) {
    this(orderRef, msg, null);
  }

  /**
   * Constructor.
   *
   * @param orderRef the order reference
   * @param msg the error message
   * @param cause the cause of the error
   */
  public BankIdTraceableException(final String orderRef, final String msg, final Throwable cause) {
    super(ErrorCode.UNKNOWN_ERROR, msg, cause);
    this.orderRef = orderRef;
    this.traceId = UUID.randomUUID().toString();

    log.error("{} created for orderReference: '{}' with identifier: '{}' - {}",
        this.getClass().getSimpleName(), this.orderRef, this.traceId, msg);
  }

  /**
   * Gets the BankID order reference.
   *
   * @return the BankID order reference
   */
  public String getOrderRef() {
    return this.orderRef;
  }

  /**
   * Gets the trace ID.
   *
   * @return the trace ID
   */
  public String getTraceId() {
    return this.traceId;
  }

}
