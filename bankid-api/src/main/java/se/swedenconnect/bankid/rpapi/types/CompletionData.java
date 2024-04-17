/*
 * Copyright 2023-2024 Sweden Connect
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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import se.swedenconnect.bankid.rpapi.LibraryVersion;

/**
 * Represents the completion data for completed orders.
 *
 * @author Martin Lindström
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompletionData implements Serializable {

  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /** Information related to the user. */
  private User user;

  /** Information related to the device used during the BankID operation. */
  private Device device;

  /**
   * The date the BankID was issued to the user. The issue date of the ID expressed using ISO 8601 date format
   * YYYY-MM-DD with a UTC time zone offset.
   */
  private String bankIdIssueDate;

  /** Information about extra verifications that were part of the transaction. */
  private StepUp stepUp;

  /** The Base64-encoded BankID signature. */
  private String signature;

  /** The Base64-encoded OCSP-response. */
  private String ocspResponse;

  /**
   * Returns the user information (such as personal identity number).
   *
   * @return user information
   */
  public User getUser() {
    return this.user;
  }

  /**
   * Assigns the user information.
   *
   * @param user user information
   */
  public void setUser(final User user) {
    this.user = user;
  }

  /**
   * Returns the information related to the device used during the BankID operation.
   *
   * @return the device information
   */
  public Device getDevice() {
    return this.device;
  }

  /**
   * Assigns the information related to the device used during the BankID operation.
   *
   * @param device the device information
   */
  public void setDevice(final Device device) {
    this.device = device;
  }

  /**
   * Gets the date the BankID was issued to the user. The issue date of the ID expressed using ISO 8601 date format
   * YYYY-MM-DD with a UTC time zone offset.
   * 
   * @return issue date
   */
  public String getBankIdIssueDate() {
    return this.bankIdIssueDate;
  }

  /**
   * Assigns the date the BankID was issued to the user. The issue date of the ID expressed using ISO 8601 date format
   * YYYY-MM-DD with a UTC time zone offset.
   * 
   * @param bankIdIssueDate issue date
   */
  public void setBankIdIssueDate(final String bankIdIssueDate) {
    this.bankIdIssueDate = bankIdIssueDate;
  }

  /**
   * Gets the information about extra verifications that were part of the transaction.
   * 
   * @return information or {@code null}
   */
  public StepUp getStepUp() {
    return this.stepUp;
  }

  /**
   * Assigns the information about extra verifications that were part of the transaction.
   * 
   * @param stepUp information
   */
  public void setStepUp(final StepUp stepUp) {
    this.stepUp = stepUp;
  }

  /**
   * Returns the Base64-encoded BankID signature.
   *
   * @return the Base64-encoded BankID signature
   */
  public String getSignature() {
    return this.signature;
  }

  /**
   * Assigns the Base64-encoded BankID signature.
   *
   * @param signature the Base64-encoded BankID signature
   */
  public void setSignature(final String signature) {
    this.signature = signature;
  }

  /**
   * Returns the Base64-encoded OCSP-response.
   *
   * @return the Base64-encoded OCSP-response
   */
  public String getOcspResponse() {
    return this.ocspResponse;
  }

  /**
   * Assigns the Base64-encoded OCSP-response.
   *
   * @param ocspResponse the Base64-encoded OCSP-response
   */
  public void setOcspResponse(final String ocspResponse) {
    this.ocspResponse = ocspResponse;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("user=[%s], device=[%s], bankIdIssueDate='%s', stepUp=[%s], signature='%s', ocspResponse='%s'",
        this.user, this.device, this.bankIdIssueDate, this.stepUp, this.signature, this.ocspResponse);
  }

  /**
   * Represents the user field of the completion data.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class User implements Serializable {

    private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

    /** The personal identity number. */
    private String personalNumber;

    /** The user full name (given name and surname). */
    private String name;

    /** The user given name. */
    private String givenName;

    /** The user surname. */
    private String surname;

    /**
     * Returns the personal identity number.
     *
     * @return the personal identity number
     */
    public String getPersonalNumber() {
      return this.personalNumber;
    }

    /**
     * Assigns the personal identity number.
     *
     * @param personalNumber the personal identity number
     */
    public void setPersonalNumber(final String personalNumber) {
      this.personalNumber = personalNumber;
    }

    /**
     * Returns the user full name.
     *
     * @return the user full name
     */
    public String getName() {
      return this.name;
    }

    /**
     * Assigns the user full name.
     *
     * @param name the user full name
     */
    public void setName(final String name) {
      this.name = name;
    }

    /**
     * Returns the user given name.
     *
     * @return the user given name
     */
    public String getGivenName() {
      return this.givenName;
    }

    /**
     * Assigns the user given name.
     *
     * @param givenName the user given name
     */
    public void setGivenName(final String givenName) {
      this.givenName = givenName;
    }

    /**
     * Returns the user surname.
     *
     * @return the user surname
     */
    public String getSurname() {
      return this.surname;
    }

    /**
     * Returns the user surname.
     *
     * @param surname the user surname
     */
    public void setSurname(final String surname) {
      this.surname = surname;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return String.format("personalNumber='%s', name='%s', givenName='%s', surname='%s'",
          this.personalNumber, this.name, this.givenName, this.surname);
    }
  }

  /**
   * Represents the device field of the completion data.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Device implements Serializable {

    private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

    /** The device IP address. */
    private String ipAddress;

    /** Unique hardware identifier for the user’s device. */
    private String uhi;

    /**
     * Returns the device IP address.
     *
     * @return the device IP address
     */
    public String getIpAddress() {
      return this.ipAddress;
    }

    /**
     * Assigns the device IP address.
     *
     * @param ipAddress the device IP address
     */
    public void setIpAddress(final String ipAddress) {
      this.ipAddress = ipAddress;
    }

    /**
     * Returns the unique hardware identifier for the user’s device.
     * 
     * @return unique hardware identifier
     */
    public String getUhi() {
      return this.uhi;
    }

    /**
     * Assigns the unique hardware identifier for the user’s device.
     * 
     * @param uhi unique hardware identifier
     */
    public void setUhi(final String uhi) {
      this.uhi = uhi;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return String.format("ipAddress='%s', uhi='%s'", this.ipAddress, this.uhi);
    }

  }

  /**
   * Information about extra verifications that were part of the transaction.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class StepUp implements Serializable {

    private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

    /** Indicate if there was a check of the mrtd (machine readable travel document). */
    private Boolean mrtd;

    /**
     * Gets the MRTD status that indicates if there was a check of the MRTD (machine readable travel document).
     * {@code true} means that the MRTD check was performed and passed, and {@code false} means that the check was
     * performed but failed.
     * 
     * @return the MRTD status
     */
    public Boolean getMrtd() {
      return this.mrtd;
    }

    /**
     * Assigns the MRTD status that indicates if there was a check of the MRTD (machine readable travel document).
     * {@code true} means that the MRTD check was performed and passed, and {@code false} means that the check was
     * performed but failed.
     * 
     * @param mrtd the MRTD status
     */
    public void setMrtd(final Boolean mrtd) {
      this.mrtd = mrtd;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return String.format("mrtd=%d", this.mrtd);
    }

  }

}
