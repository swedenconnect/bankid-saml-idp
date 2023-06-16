package se.swedenconnect.bankid.idp.authn.session;

public class SessionAttributeKeys {
  /**
   * The session attribute where we store whether we selected "this device" or "other device".
   */
  public static final String PREVIOUS_DEVICE_SESSION_ATTRIBUTE = "DEVICE-SELECTION";
  /**
   * The session attribute where we store completion data for a bankid session
   */
  public static final String BANKID_COMPLETION_DATA_ATTRIBUTE = "BANKID-COMPLETION-DATA";
  /**
   * The session attribute where we store the current state of a bankid session
   */
  public static final String BANKID_STATE_ATTRIBUTE = "BANKID-STATE";
}
