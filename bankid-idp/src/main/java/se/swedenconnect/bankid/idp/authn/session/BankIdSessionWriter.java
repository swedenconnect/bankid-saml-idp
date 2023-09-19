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
package se.swedenconnect.bankid.idp.authn.session;

import javax.servlet.http.HttpServletRequest;

import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;

/**
 * Interface for writing to the BankID session.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
public interface BankIdSessionWriter {

  /**
   * Saves {@link BankidSessionData} to repository. Overwrites data if order reference is the same.
   *
   * @param request to determine session key
   * @param data to be saved
   */
  void save(final HttpServletRequest request, final BankIdSessionData data);

  /**
   * Saves the final {@link CollectResponse} that contains CompletionData.
   *
   * @param request to determine session key
   * @param data to be saved
   */
  void save(final HttpServletRequest request, final CollectResponse data);

  /**
   * Deletes everything except device selection for finalized authentication.
   *
   * @param request to determine session key
   */
  void delete(final HttpServletRequest request);

  /**
   * Loads previous device selection from successful authentication.
   *
   * @param request to determine session key
   * @param previousDeviceSelection device used for authentication
   */
  void save(final HttpServletRequest request, final PreviousDeviceSelection previousDeviceSelection);

  /**
   * Loads user visible data to be displayed in BankID application.
   *
   * @param request to determine session key
   * @param userVisibleData message that should be displayed in app
   */
  void save(final HttpServletRequest request, final UserVisibleData userVisibleData);
}
