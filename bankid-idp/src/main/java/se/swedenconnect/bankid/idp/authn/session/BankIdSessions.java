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

import java.util.Objects;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;

/**
 * Implements the writing and reading of BankID session data.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Service
public class BankIdSessions implements BankIdSessionWriter, BankIdSessionReader {

  /** The underlying data access object for storing BankID sessions. */
  private final SessionDao sessionDao;

  /**
   * Constructor.
   *
   * @param sessionDao the underlying data access object for storing BankID sessions
   */
  public BankIdSessions(final SessionDao sessionDao) {
    this.sessionDao = Objects.requireNonNull(sessionDao, "sessionDao must not be null");
  }

  /** {@inheritDoc} */
  @Override
  public void save(final HttpServletRequest request, final BankIdSessionData data) {
    BankIdSessionState state =
        this.sessionDao.read(BankIdSessionAttributeKeys.BANKID_STATE_ATTRIBUTE, BankIdSessionState.class, request);
    if (state == null) {
      state = new BankIdSessionState();
    }
    else if (state.getBankIdSessionData().getOrderReference().equals(data.getOrderReference())) {
      state.pop();
    }
    state.push(data);
    this.sessionDao.write(BankIdSessionAttributeKeys.BANKID_STATE_ATTRIBUTE, state, request);
  }

  /** {@inheritDoc} */
  @Override
  public void save(final HttpServletRequest request, final CollectResponse data) {
    this.sessionDao.write(BankIdSessionAttributeKeys.BANKID_COMPLETION_DATA_ATTRIBUTE, data, request);
  }

  /** {@inheritDoc} */
  @Override
  public void delete(final HttpServletRequest request) {
    BankIdSessionAttributeKeys.BANKID_VOLATILE_ATTRIBUTES.forEach(key -> this.sessionDao.remove(key, request));
  }

  /** {@inheritDoc} */
  @Override
  public void save(final HttpServletRequest request, final PreviousDeviceSelection previousDeviceSelection) {
    this.sessionDao.write(BankIdSessionAttributeKeys.PREVIOUS_DEVICE_SESSION_ATTRIBUTE,
        previousDeviceSelection.getValue(), request);
  }

  /** {@inheritDoc} */
  @Override
  public BankIdSessionState loadSessionData(final HttpServletRequest request) {
    return this.sessionDao.read(BankIdSessionAttributeKeys.BANKID_STATE_ATTRIBUTE, BankIdSessionState.class, request);
  }

  /** {@inheritDoc} */
  @Override
  public CollectResponse loadCompletionData(final HttpServletRequest request) {
    return this.sessionDao.read(BankIdSessionAttributeKeys.BANKID_COMPLETION_DATA_ATTRIBUTE, CollectResponse.class,
        request);
  }

  /** {@inheritDoc} */
  @Override
  public PreviousDeviceSelection loadPreviousSelectedDevice(final HttpServletRequest request) {
    final String attribute =
        this.sessionDao.read(BankIdSessionAttributeKeys.PREVIOUS_DEVICE_SESSION_ATTRIBUTE, String.class, request);
    if (attribute == null) {
      return null;
    }
    else {
      return PreviousDeviceSelection.forValue(attribute);
    }
  }

  /** {@inheritDoc} */
  @Override
  public UserVisibleData loadUserVisibleData(final HttpServletRequest request) {
    return this.sessionDao.read(BankIdSessionAttributeKeys.BANKID_USER_VISIBLE_DATA_ATTRIBUTE, UserVisibleData.class,
        request);
  }

  /** {@inheritDoc} */
  @Override
  public void save(final HttpServletRequest request, final UserVisibleData userVisibleData) {
    this.sessionDao.write(BankIdSessionAttributeKeys.BANKID_USER_VISIBLE_DATA_ATTRIBUTE, userVisibleData, request);
  }

  /** {@inheritDoc} */
  @Override
  public BankIdContext loadContext(final HttpServletRequest request) {
    return this.sessionDao.read(BankIdSessionAttributeKeys.BANKID_CONTEXT, BankIdContext.class, request);
  }
}
