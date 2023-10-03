/*
 * Copyright 2023 Sweden Connect
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
package se.swedenconnect.bankid.idp.integration.response;

import lombok.NoArgsConstructor;
import se.swedenconnect.bankid.idp.ApplicationVersion;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CompletionData;

@NoArgsConstructor
public class CollectResponseBuilder extends CollectResponse {

  private static final long serialVersionUID = ApplicationVersion.SERIAL_VERSION_UID;

  private String orderReference;
  private Status status = Status.PENDING;
  private String hintCode;
  private CompletionData completionData;

  public CollectResponseBuilder orderReference(String orderReference) {
    this.orderReference = orderReference;
    return this;
  }

  public CollectResponseBuilder status(Status status) {
    this.status = status;
    return this;
  }

  public CollectResponseBuilder hintCode(String hintCode) {
    this.hintCode = hintCode;
    return this;
  }

  public CollectResponseBuilder completionData(CompletionData completionData) {
    this.completionData = completionData;
    return this;
  }

  public CollectResponse build() {
    CollectResponse collectResponse = new CollectResponse();
    collectResponse.setOrderReference(orderReference);
    collectResponse.setStatus(status);
    collectResponse.setHintCode(hintCode);
    collectResponse.setCompletionData(completionData);
    return collectResponse;
  }
}
