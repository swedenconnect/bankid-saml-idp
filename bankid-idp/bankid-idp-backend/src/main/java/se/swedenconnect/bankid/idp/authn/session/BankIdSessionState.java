package se.swedenconnect.bankid.idp.authn.session;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

public class BankIdSessionState {
    private final Deque<BankIdSessionData> bankIdSessionDataStack;

    public BankIdSessionState() {
        this.bankIdSessionDataStack = new ArrayDeque<>();
    }

    public void push(BankIdSessionData data) {
        bankIdSessionDataStack.push(data);
    }

    public BankIdSessionData pop() {
        return bankIdSessionDataStack.removeFirst();
    }

    public BankIdSessionData getBankIdSessionData() {
        return bankIdSessionDataStack.getFirst();
    }

    public Instant getInitialOrderTime() {
        return bankIdSessionDataStack.getLast().getStartTime();
    }
}
