package se.swedenconnect.bankid.idp.authn.session;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Container for multiple BankId session states
 * This is construct serves the purpose of holding all sessions that gets started when a user starts authentication
 * for the strict purpose of being able to refer to the first initial session as well as the most current one.
 *
 */
public class BankIdSessionState {

  private final Deque<BankIdSessionData> bankIdSessionDataStack;

  public BankIdSessionState() {
    this.bankIdSessionDataStack = new ArrayDeque<>();
  }

  /**
   * Operation to insert a new session data
   * @param data New session data
   */
  public void push(final BankIdSessionData data) {
    bankIdSessionDataStack.push(data);
  }

  /**
   * Operation to remove the most current session data
   * @return Old session data
   */
  public BankIdSessionData pop() {
    return bankIdSessionDataStack.removeFirst();
  }

  /**
   * @return Most current bankid session data
   */
  public BankIdSessionData getBankIdSessionData() {
    return bankIdSessionDataStack.getFirst();
  }

  /**
   * @return Point in time of the first response
   */
  public Instant getInitialOrderTime() {
    return bankIdSessionDataStack.getLast().getStartTime();
  }
}
