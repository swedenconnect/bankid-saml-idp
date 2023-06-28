package se.swedenconnect.bankid.idp.authn.session;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;

import javax.servlet.http.HttpServletRequest;

import static se.swedenconnect.bankid.idp.authn.session.SessionAttributeKeys.*;

/**
 * BankIdSessionData
 */

@AllArgsConstructor
@Service
public class BankIdSessions implements BankIdSessionWriter, BankIdSessionReader {

  private final SessionDao dao;

  @Override
  public void save(HttpServletRequest request, BankIdSessionData data) {
    BankIdSessionState state = dao.read(BANKID_STATE_ATTRIBUTE, BankIdSessionState.class, request);
    if (state == null) {
      state = new BankIdSessionState();
    } else if (state.getBankIdSessionData().getOrderReference().equals(data.getOrderReference())) {
      state.pop();
    }
    state.push(data);
    dao.write(BANKID_STATE_ATTRIBUTE, state, request);
  }

  @Override
  public void save(HttpServletRequest request, CollectResponse data) {
    dao.write(BANKID_COMPLETION_DATA_ATTRIBUTE, data, request);
  }

  @Override
  public void delete(HttpServletRequest request) {
    BANKID_VOLATILE_ATTRIBUTES.forEach(key -> dao.remove(key, request));
  }

  @Override
  public void save(HttpServletRequest request, PreviousDeviceSelection previousDeviceSelection) {
    dao.write(PREVIOUS_DEVICE_SESSION_ATTRIBUTE, previousDeviceSelection.getValue(), request);
  }

  @Override
  public BankIdSessionState loadSessionData(HttpServletRequest request) {
    return dao.read(BANKID_STATE_ATTRIBUTE, BankIdSessionState.class, request);
  }

  @Override
  public CollectResponse laodCompletionData(HttpServletRequest request) {
    return dao.read(BANKID_COMPLETION_DATA_ATTRIBUTE, CollectResponse.class, request);
  }

  @Override
  public PreviousDeviceSelection loadPreviousSelectedDevice(HttpServletRequest request) {
    String attribute = dao.read(PREVIOUS_DEVICE_SESSION_ATTRIBUTE, String.class, request);
    if (attribute == null) {
      return null;
    }
    return PreviousDeviceSelection.forValue(attribute);
  }

  @Override
  public UserVisibleData loadUserVisibleData(HttpServletRequest request) {
    return dao.read(BANKID_USER_VISIBLE_DATA_ATTRIBUTE, UserVisibleData.class, request);
  }

  @Override
  public void save(HttpServletRequest request, UserVisibleData userVisibleData) {
    dao.write(BANKID_USER_VISIBLE_DATA_ATTRIBUTE, userVisibleData, request);
  }

  @Override
  public BankIdContext loadContext(HttpServletRequest request) {
    return dao.read(BANKID_CONTEXT, BankIdContext.class, request);
  }
}
