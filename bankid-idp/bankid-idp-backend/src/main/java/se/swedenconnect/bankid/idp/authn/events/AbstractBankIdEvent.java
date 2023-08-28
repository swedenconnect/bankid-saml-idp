package se.swedenconnect.bankid.idp.authn.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;

import javax.servlet.http.HttpServletRequest;

/**
 * Default dataset for events to fulfill event-consumers
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@AllArgsConstructor
@Data
public abstract class AbstractBankIdEvent {
  /** The servlet request. */
  private final HttpServletRequest request;

  /** The relying party. */
  private final RelyingPartyData data;
}
