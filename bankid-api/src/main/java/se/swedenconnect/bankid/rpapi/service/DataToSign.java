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
package se.swedenconnect.bankid.rpapi.service;

import java.util.Base64;
import java.util.Optional;

import se.swedenconnect.bankid.rpapi.LibraryVersion;

/**
 * Class the represents "to-be-signed" input for a signature operation.
 *
 * @author Martin Lindström
 */
public class DataToSign extends UserVisibleData {

  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /** Data not displayed to the user (optional). */
  private String userNonVisibleData;

  /**
   * Assigns the data that is part of the signature process but should not be displayed to the user. This supplied data
   * is the raw bytes and the method will Base64 encode it.
   *
   * <p>
   * See also {@link DataToSign#setUserNonVisibleData(String)}.
   * </p>
   *
   * @param bytes the data that is part of the signature process but should not be displayed to the user (raw data)
   */
  public void setUserNonVisibleDataRaw(final byte[] bytes) {
    this.userNonVisibleData = Base64.getEncoder().encodeToString(bytes);
  }

  /**
   * Assigns the data that is part of the signature process but should not be displayed to the user.
   * <p>
   * The value must be base 64-encoded. 0 - 200 000 characters (after base 64-encoding).
   * </p>
   * <p>
   * See also {@link DataToSign#setUserNonVisibleDataRaw(byte[])}.
   * </p>
   *
   * @param userNonVisibleData the data that is part of the signature process but should not be displayed to the user
   *          (base64-encoded)
   */
  public void setUserNonVisibleData(final String userNonVisibleData) {
    this.userNonVisibleData = userNonVisibleData;
  }

  /**
   * Returns the data that is part of the signature process but should not be displayed to the user.
   *
   * @return data to be signed, but not displayed to the user
   */
  public String getUserNonVisibleData() {
    return this.userNonVisibleData;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("%s, userNonVisibleData='%s'", super.toString(),
        Optional.ofNullable(this.userNonVisibleData).orElseGet(() -> "<not set>"));
  }

}
