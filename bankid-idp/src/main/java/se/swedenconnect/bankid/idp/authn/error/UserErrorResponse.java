package se.swedenconnect.bankid.idp.authn.error;

public class UserErrorResponse {

  public UserErrorResponse(final String errorMessage, final String traceId) {
    this.errorMessage = errorMessage;
    this.traceId = traceId;
  }

  private String errorMessage;
  private String traceId;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(final String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getTraceId() {
    return traceId;
  }

  public void setTraceId(final String traceId) {
    this.traceId = traceId;
  }
}
