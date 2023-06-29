package se.swedenconnect.bankid.idp.authn.session;

import java.util.List;

public class SessionAttributeKeys {

  public static final String BANKID_CONTEXT = "BANKID-CONTEXT";


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
  /**
   * The session attribute where we store display message
   */
  public static final String BANKID_USER_VISIBLE_DATA_ATTRIBUTE = "BANKID-DISPLAY-MESSAGE";

  /**
   * Attributes which should not be persisted upon cancelation or completion of an order
   */
  public static final List<String> BANKID_VOLATILE_ATTRIBUTES = List.of(BANKID_USER_VISIBLE_DATA_ATTRIBUTE, BANKID_STATE_ATTRIBUTE, BANKID_COMPLETION_DATA_ATTRIBUTE);
}
