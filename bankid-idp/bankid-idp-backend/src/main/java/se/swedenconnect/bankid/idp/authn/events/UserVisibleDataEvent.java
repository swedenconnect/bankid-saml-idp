package se.swedenconnect.bankid.idp.authn.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;

import javax.servlet.http.HttpServletRequest;

/**
 * Fires when a new UserVisibleData has been created for a user
 */
@AllArgsConstructor
@Data
public class UserVisibleDataEvent {
  private final UserVisibleData userVisibleData;
  private final HttpServletRequest request;
}
