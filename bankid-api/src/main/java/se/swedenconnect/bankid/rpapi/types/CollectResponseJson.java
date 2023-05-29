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
package se.swedenconnect.bankid.rpapi.types;

import com.fasterxml.jackson.annotation.*;

/**
 * Representation of the response received from a collect call.
 *
 * @author Martin Lindström
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectResponseJson implements CollectResponse {

  /** The order reference string. */
  @JsonProperty(value = "orderRef", required = true)
  private String orderReference;

  /** The status. */
  @JsonProperty(value = "status", required = true)
  private Status status;

  /** The hintCode for status=pending or status=failed. */
  @JsonProperty(value = "hintCode", required = false)
  private String hintCode;

  /** The completion data. Present if status=complete. */
  @JsonProperty(value = "completionData", required = false)
  private CompletionData completionData;

  @JsonIgnore
  @Override
  public String toString() {
    return "CollectResponseJson{" + "orderReference='" + orderReference + '\'' +
        ", status=" + status +
        ", hintCode='" + hintCode + '\'' +
        ", completionData=" + completionData +
        '}';
  }

  /** {@inheritDoc} */
  @Override
  public String getOrderReference() {
    return this.orderReference;
  }

  /**
   * Assigns the order reference.
   *
   * @param orderReference the order reference
   */
  public void setOrderReference(final String orderReference) {
    this.orderReference = orderReference;
  }

  /**
   * Returns the overall status for the collect call.
   *
   * @return the status
   */
  public Status getStatus() {
    return this.status;
  }

  /**
   * Assigns the overall status for the collect call.
   *
   * @param status the status
   */
  public void setStatus(final Status status) {
    this.status = status;
  }

  /** {@inheritDoc} */
  @Override
  public ProgressStatus getProgressStatus() {
    if (Status.COMPLETE == this.status) {
      return ProgressStatus.COMPLETE;
    }
    else if (Status.PENDING == this.status) {
      return ProgressStatus.forValue(this.hintCode);
    }
    else {
      return null;
    }
  }

  /**
   * If the overall status is "failed", this method returns the error code found in hint code.
   *
   * @return the error code, or {@code null}
   */
  public ErrorCode getErrorCode() {
    if (Status.FAILED == this.status) {
      return ErrorCode.forValue(this.hintCode);
    }
    else {
      return null;
    }
  }

  /** {@inheritDoc} */
  @Override
  public CompletionData getCompletionData() {
    return this.completionData;
  }

  /**
   * Assigns the completion data.
   *
   * @param completionData the completion data
   */
  public void setCompletionData(final CompletionData completionData) {
    this.completionData = completionData;
  }

  /**
   * Assigns the hint code.
   *
   * @param hintCode the hint code
   */
  public void setHintCode(final String hintCode) {
    this.hintCode = hintCode;
  }

  /**
   * Represents the status field values.
   */
  public enum Status {

    /** Pending */
    PENDING("pending"),

    /** Failed */
    FAILED("failed"),

    /** Complete */
    COMPLETE("complete");

    /** The string representation of the enum. */
    private final String value;

    /**
     * Constructor.
     *
     * @param value string representation
     */
    Status(final String value) {
      this.value = value;
    }

    /**
     * Given a string representation its enum object is returned.
     *
     * @param value the string representation
     * @return a {@code Status}
     */
    @JsonCreator
    public static Status forValue(final String value) {
      for (final Status s : Status.values()) {
        if (s.getValue().equalsIgnoreCase(value)) {
          return s;
        }
      }
      return null;
    }

    /**
     * Returns the string representation of the enum.
     *
     * @return the string representation
     */
    @JsonValue
    public String getValue() {
      return this.value;
    }
  }

}
