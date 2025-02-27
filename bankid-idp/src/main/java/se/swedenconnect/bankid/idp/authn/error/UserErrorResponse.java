/*
 * Copyright 2023-2025 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
