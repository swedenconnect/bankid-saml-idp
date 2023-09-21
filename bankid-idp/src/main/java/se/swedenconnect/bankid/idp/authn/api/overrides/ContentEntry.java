package se.swedenconnect.bankid.idp.authn.api.overrides;

import lombok.Getter;
import lombok.Setter;

public class ContentEntry {
  /** Constructor
   *
   * @param text the message code for the text that should appear in the alert box
   * @param link optional link, if set the text should be handled as link text
   */
  public ContentEntry(String text, String link) {
    this.text = text;
    this.link = link;
  }

  /**
   * The message code for the text that should appear in the alert box.
   */
  @Getter
  @Setter
  private String text;

  /**
   * Optional link for the alert box
   */
  @Getter
  @Setter
  private String link;
}
