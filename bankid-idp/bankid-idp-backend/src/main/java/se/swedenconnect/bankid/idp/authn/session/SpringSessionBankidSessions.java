package se.swedenconnect.bankid.idp.authn.session;

import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static se.swedenconnect.bankid.idp.authn.session.SessionAttributeKeys.*;

/**
 * Spring Session Implementation of Session Storage
 * This implementation is not meant for production use
 */
public class SpringSessionBankidSessions implements BankIdSessionWriter, BankIdSessionReader {

  @Override
  public void save(HttpServletRequest request, BankIdSessionData data) {
    HttpSession session = request.getSession();
    BankIdSessionState state = (BankIdSessionState) session.getAttribute(BANKID_STATE_ATTRIBUTE);
    if (state == null) {
      state = new BankIdSessionState();
    } else if (state.getBankIdSessionData().getOrderReference().equals(data.getOrderReference())) {
      state.pop();
    }
    state.push(data);
    session.setAttribute(BANKID_STATE_ATTRIBUTE, state);
  }

  @Override
  public void save(HttpServletRequest request, CollectResponse data) {
    HttpSession session = request.getSession();
    session.setAttribute(BANKID_COMPLETION_DATA_ATTRIBUTE, data);
  }

  @Override
  public void delete(HttpServletRequest request) {
    HttpSession session = request.getSession();
    session.setAttribute(BANKID_COMPLETION_DATA_ATTRIBUTE, null);
    session.setAttribute(BANKID_STATE_ATTRIBUTE, null);
  }

  @Override
  public void save(HttpServletRequest request, PreviousDeviceSelection previousDeviceSelection) {
    request.getSession().setAttribute(PREVIOUS_DEVICE_SESSION_ATTRIBUTE, previousDeviceSelection.getValue());
  }

  @Override
  public BankIdSessionState loadSessionData(HttpServletRequest request) {
    return (BankIdSessionState) request.getSession().getAttribute(BANKID_STATE_ATTRIBUTE);
  }

  @Override
  public CollectResponse laodCompletionData(HttpServletRequest request) {
    return (CollectResponse) request.getSession().getAttribute(BANKID_COMPLETION_DATA_ATTRIBUTE);
  }

  @Override
  public PreviousDeviceSelection loadPreviousSelectedDevice(HttpServletRequest request) {
    String attribute = (String) request.getSession().getAttribute(PREVIOUS_DEVICE_SESSION_ATTRIBUTE);
    if (attribute == null) {
      return null;
    }
    return PreviousDeviceSelection.forValue(attribute);
  }
}
