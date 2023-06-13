package se.swedenconnect.bankid.idp.authn.session;

import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CompletionData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static se.swedenconnect.bankid.idp.authn.BankIdAuthenticationController.PREVIOUS_DEVICE_SESSION_ATTRIBUTE;

public class SpringSessionBankidSessions implements BankIdSessionWriter, BankIdSessionReader {
    @Override
    public void save(HttpServletRequest request, BankIdSessionData data) {
        HttpSession session = request.getSession();
        BankIdSessionState state = (BankIdSessionState) session.getAttribute("BANKID-STATE");
        if (state == null) {
            state = new BankIdSessionState();
        }
        else if (state.getBankIdSessionData().getOrderReference().equals(data.getOrderReference())) {
            state.pop();
        }
        state.push(data);
        session.setAttribute("BANKID-STATE", state);
    }

    @Override
    public void save(HttpServletRequest request, CollectResponse data) {
        HttpSession session = request.getSession();
        session.setAttribute("BANKID-COMPLETION-DATA", data);
    }

    @Override
    public void delete(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("BANKID-COMPLETION-DATA", null);
        session.setAttribute("BANKID-STATE", null);
    }

    @Override
    public void save(HttpServletRequest request, PreviousDeviceSelection previousDeviceSelection) {
        request.getSession().setAttribute(PREVIOUS_DEVICE_SESSION_ATTRIBUTE, previousDeviceSelection.getValue());
    }

    @Override
    public BankIdSessionState loadSessionData(HttpServletRequest request) {
        return (BankIdSessionState) request.getSession().getAttribute("BANKID-STATE");
    }

    @Override
    public CompletionData laodCompletionData(HttpServletRequest request) {
        return (CompletionData) request.getSession().getAttribute("BANKID-COMPLETION-DATA");
    }
}
