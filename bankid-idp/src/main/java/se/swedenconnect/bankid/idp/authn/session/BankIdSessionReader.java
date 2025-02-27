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
package se.swedenconnect.bankid.idp.authn.session;

import jakarta.servlet.http.HttpServletRequest;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;

/**
 * Interface for reading from the BankID session.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public interface BankIdSessionReader {

  /**
   * Loads session data.
   *
   * @param request the current request for the user to determine session key
   * @return a {@link BankIdSessionState}
   */
  BankIdSessionState loadSessionData(final HttpServletRequest request);

  /**
   * Loads final {@link CollectResponse}.
   *
   * @param request the current request for the user to determine session key
   * @return the final {@link CollectResponse} from the BankID API containing CompletionData
   */
  CollectResponse loadCompletionData(final HttpServletRequest request);

  /**
   * Loads device selection.
   *
   * @param request the current request for the user to determine session key
   * @return device selection from last successful authentication for the current user
   */
  PreviousDeviceSelection loadPreviousSelectedDevice(final HttpServletRequest request);

  /**
   * Loads uservisible data.
   *
   * @param request the current request for the user to determine session key
   * @return user visible data to be displayed in app
   */
  UserVisibleData loadUserVisibleData(final HttpServletRequest request);

  /**
   * Loads the BankID context.
   *
   * @param request the servlet request
   * @return a {@link BankIdContext}
   */
  BankIdContext loadContext(final HttpServletRequest request);
}
