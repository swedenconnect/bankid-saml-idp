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
package se.swedenconnect.bankid.rpapi.types;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the structure in which a relying party can define how an authentication or signature process should be
 * performed.
 *
 * <p>
 * <b>Note:</b> It is recommended to use the {@link RequirementBuilder} to construct a {@code Requirement}.
 * </p>
 *
 * @author Martin Lindström
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Requirement {

  /** Certificate policy Object Identifier - BankID on file (for production) should be used. */
  public static final String CP_BANKID_ON_FILE = "1.2.752.78.1.1";

  /** Certificate policy Object Identifier - BankID on smartcard (for production) should be used. */
  public static final String CP_BANKID_ON_SMARTCARD = "1.2.752.78.1.2";

  /** Certificate policy Object Identifier - Mobile BankID (for production) should be used. */
  public static final String CP_MOBILE_BANKID = "1.2.752.78.1.5";

  /** Certificate policy Object Identifier - Nordea eID on file and smartcard (for production) should be used. */
  public static final String CP_NORDEA_EID = "1.2.752.71.1.3";

  /** Certificate policy Object Identifier - BankID on file (for test) should be used. */
  public static final String CP_BANKID_ON_FILE_TEST = "1.2.3.4.5";

  /** Certificate policy Object Identifier - BankID on smartcard (for test) should be used. */
  public static final String CP_BANKID_ON_SMARTCARD_TEST = "1.2.3.4.10";

  /** Certificate policy Object Identifier - Mobile BankID (for test) should be used. */
  public static final String CP_MOBILE_BANKID_TEST = "1.2.3.4.25";

  /** Certificate policy Object Identifier - Nordea eID on file and smartcard (for test) should be used. */
  public static final String CP_NORDEA_EID_TEST = "1.2.752.71.1.3";

  /** Certificate policy Object Identifier - Test BankID for some BankID Banks. */
  public static final String CP_TEST_BANKID = "1.2.752.60.1.6";

  /**
   * Tells whether users are required to sign the transaction with their PIN code, even if they have biometrics
   * activated.
   */
  private Boolean pinCode;

  /**
   * If present, and set to "true", the client needs to provide MRTD (Machine readable travel document) information to
   * complete the order.Only Swedish passports and national ID cards are supported.
   */
  private Boolean mrtd;

  /** Requirement for which type of smart card reader that is required. */
  private CardReaderRequirement cardReader;

  /** Object identifiers for which policies that should be used. */
  private List<String> certificatePolicies;

  /**
   * A personal eidentification number to be used to complete the transaction. If a BankID with another personal number
   * attempts to sign the transaction, it fails.
   */
  private String personalNumber;

  /**
   * Creates a {@code RequirementBuilder}.
   *
   * @return a builder
   */
  public static RequirementBuilder builder() {
    return new RequirementBuilder();
  }

  /**
   * Creates a {@code RequirementBuilder} given an already existing requirement.
   *
   * @param req the template requirement
   * @return a builder
   */
  public static RequirementBuilder builder(final Requirement req) {
    return new RequirementBuilder(req);
  }

  /**
   * Predicate that tells whether this object is "empty", meaning that no properties have been assigned.
   *
   * @return true if not properties have been assigned, and false otherwise
   */
  @JsonIgnore
  public boolean isEmpty() {
    return this.pinCode == null && this.mrtd == null && this.cardReader == null
        && (this.certificatePolicies == null || this.certificatePolicies.isEmpty())
        && this.personalNumber == null;
  }

  /**
   * Gets the pin code requirement that tells whether users are required to sign the transaction with their PIN code,
   * even if they have biometrics activated.
   *
   * @return whether pin code is required
   */
  public Boolean getPinCode() {
    return this.pinCode;
  }

  /**
   * Sets whether users are required to sign the transaction with their PIN code, even if they have biometrics
   * activated.
   *
   * @param pinCode whether pin code is required
   */
  public void setPinCode(final Boolean pinCode) {
    this.pinCode = pinCode;
  }

  /**
   * Gets the MRTD flag. If set to "true", the client needs to provide MRTD (Machine readable travel document)
   * information to complete the order.Only Swedish passports and national ID cards are supported.
   *
   * @return the MRTD flag
   */
  public Boolean getMrtd() {
    return this.mrtd;
  }

  /**
   * Assigns the MRTD flag. If set to "true", the client needs to provide MRTD (Machine readable travel document)
   * information to complete the order.Only Swedish passports and national ID cards are supported.
   *
   * @param mrtd the MRTD flag
   */
  public void setMrtd(final Boolean mrtd) {
    this.mrtd = mrtd;
  }

  /**
   * Returns the requirement for which type of smart card reader that is required.
   *
   * @return the card reader requirement, or {@code null}
   */
  public CardReaderRequirement getCardReader() {
    return this.cardReader;
  }

  /**
   * Assigns the requirement for which type of smart card reader that is required.
   *
   * <p>
   * This condition should be combined with a {@code certificatePolicies} for a smart card to avoid undefined behavior.
   * </p>
   *
   * @param cardReader the card reader requirement
   */
  public void setCardReader(final CardReaderRequirement cardReader) {
    this.cardReader = cardReader;
  }

  /**
   * Returns the certificate policies telling which types of BankID:s that should be supported.
   *
   * @return a list of certificate policy object identifiers
   */
  public List<String> getCertificatePolicies() {
    return this.certificatePolicies;
  }

  /**
   * Assigns the certificate policies telling which types of BankID:s that should be supported.
   * <p>
   * It is recommended to use the {@link RequirementBuilder} to set up certificate policies.
   * </p>
   *
   * @param certificatePolicies a list of certificate policy object identifiers
   */
  public void setCertificatePolicies(final List<String> certificatePolicies) {
    this.certificatePolicies = certificatePolicies;
  }

  /**
   * Gets the personal eidentification number to be used to complete the transaction. If a BankID with another personal
   * number attempts to sign the transaction, it fails.
   *
   * @return the personal number or {@code null}
   */
  public String getPersonalNumber() {
    return this.personalNumber;
  }

  /**
   * Assigns the personal eidentification number to be used to complete the transaction. If a BankID with another
   * personal number attempts to sign the transaction, it fails.
   *
   * @param personalNumber the personal number
   */
  public void setPersonalNumber(final String personalNumber) {
    this.personalNumber = personalNumber;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format(
        "pinCode=%s, mtrd=%s, cardReader=%s, certificatePolicies=%s",
        this.pinCode != null ? this.pinCode : "<not set>",
        this.mrtd != null ? this.mrtd : "<not-set>",
        this.personalNumber != null ? this.personalNumber : "<not-set>",
        this.cardReader != null ? this.cardReader : "<not set>",
        this.certificatePolicies != null ? this.certificatePolicies : "<not set - defaults apply>");
  }

  /**
   * A class implementing a builder pattern for constructing {@link Requirement} objects.
   */
  public static class RequirementBuilder {

    /** The requirement being built. */
    private final Requirement requirement;

    /** Flag telling whether we are setting up a requirement for a production system. */
    private boolean productionSetup = true;

    /** Flag for enabling mobile BankID. */
    private boolean enableMobile = true;

    /** Flag for enabling BankID on file. */
    private boolean enableOnFile = true;

    /** Flag for enabling BankID on smart card. */
    private boolean enableOnSmartCard = true;

    /** Flag for enabling Nordea BankID. */
    private boolean enableNordea = true;

    /**
     * Default constructor.
     */
    public RequirementBuilder() {
      this.requirement = new Requirement();
    }

    /**
     * Constructor setting up a builder from the supplied requirement object.
     *
     * @param requirement the object to initialize the builder from
     */
    public RequirementBuilder(final Requirement requirement) {
      this();
      if (requirement == null) {
        return;
      }
      this.requirement.setPinCode(requirement.getPinCode());
      this.requirement.setMrtd(requirement.getMrtd());
      this.requirement.setPersonalNumber(requirement.getPersonalNumber());

      if (requirement.getCertificatePolicies() != null && !requirement.getCertificatePolicies().isEmpty()) {
        this.enableMobile = requirement.getCertificatePolicies().contains(Requirement.CP_MOBILE_BANKID)
            || requirement.getCertificatePolicies().contains(Requirement.CP_MOBILE_BANKID_TEST);
        this.enableOnFile = requirement.getCertificatePolicies().contains(Requirement.CP_BANKID_ON_FILE)
            || requirement.getCertificatePolicies().contains(Requirement.CP_BANKID_ON_FILE_TEST);
        this.enableOnSmartCard = requirement.getCertificatePolicies().contains(Requirement.CP_BANKID_ON_SMARTCARD)
            || requirement.getCertificatePolicies().contains(Requirement.CP_BANKID_ON_SMARTCARD_TEST);
        this.enableNordea = requirement.getCertificatePolicies().contains(Requirement.CP_NORDEA_EID)
            || requirement.getCertificatePolicies().contains(Requirement.CP_NORDEA_EID_TEST);
      }
      this.requirement.setCardReader(requirement.getCardReader());
    }

    /**
     * Returns the built {@code Requirement} object
     *
     * @return a Requirement object
     */
    public Requirement build() {

      // If no configuration for certificate policies has been performed, we don't assign anything for
      // certificate policies and let the default kick in.
      //
      if (this.enableMobile && this.enableOnFile && this.enableOnSmartCard && this.enableNordea) {
        return this.requirement;
      }

      final List<String> oids =
          Arrays.asList(BANKID_ON_FILE(this.productionSetup), BANKID_ON_SMARTCARD(this.productionSetup),
              MOBILE_BANKID(this.productionSetup), NORDEA_EID(this.productionSetup));

      if (!this.enableMobile) {
        oids.removeIf(item -> item.equals(MOBILE_BANKID(this.productionSetup)));
      }
      if (!this.enableOnFile) {
        oids.removeIf(item -> item.equals(BANKID_ON_FILE(this.productionSetup)));
      }
      if (!this.enableOnSmartCard) {
        oids.removeIf(item -> item.equals(BANKID_ON_SMARTCARD(this.productionSetup)));

        // Also unset the card reader requirements since we are not using smart cards.
        this.requirement.setCardReader(null);

        // If both BankID on file and BankID on smart card were disabled, we also turn off Nordea.
        if (!this.enableOnFile) {
          oids.removeIf(item -> item.equals(NORDEA_EID(this.productionSetup)));
        }
      }
      if (!this.enableNordea) {
        oids.removeIf(item -> item.equals(NORDEA_EID(this.productionSetup)));
      }

      this.requirement.setCertificatePolicies(oids);

      return this.requirement;
    }

    /**
     * Tells whether we are setting up the requirement for a production system.
     *
     * @param production true for production and false for test
     * @return the builder
     */
    public RequirementBuilder productionSetup(final boolean production) {
      this.productionSetup = production;
      return this;
    }

    /**
     * Sets whether users are required to sign the transaction with their PIN code, even if they have biometrics
     * activated.
     *
     * @param pinCode whether pin code is required
     * @return the builder
     */
    public RequirementBuilder pinCode(final Boolean pinCode) {
      this.requirement.setPinCode(pinCode);
      return this;
    }

    /**
     * Assigns the MRTD flag. If set to "true", the client needs to provide MRTD (Machine readable travel document)
     * information to complete the order.Only Swedish passports and national ID cards are supported.
     *
     * @param mrtd the MRTD flag
     * @return the builder
     */
    public RequirementBuilder mrtd(final Boolean mrtd) {
      this.requirement.setMrtd(mrtd);
      return this;
    }

    /**
     * Assigns the personal eidentification number to be used to complete the transaction. If a BankID with another
     * personal number attempts to sign the transaction, it fails.
     *
     * @param personalNumber the personal number
     * @return the builder
     */
    public RequirementBuilder personalNumber(final String personalNumber) {
      this.requirement.setPersonalNumber(personalNumber);
      return this;
    }

    /**
     * Assigns the requirement for which type of smart card reader that is required.
     *
     * <p>
     * See {@link Requirement#setCardReader(CardReaderRequirement)}.
     * </p>
     *
     * @param cardReaderRequirement the card reader requirement
     * @return the builder
     */
    public RequirementBuilder cardReader(final CardReaderRequirement cardReaderRequirement) {
      this.requirement.setCardReader(cardReaderRequirement);
      return this;
    }

    /**
     * Enables/disables use of Mobile BankID.
     *
     * <p>
     * By default, Mobile BankID is enabled.
     * </p>
     *
     * @param enable should Mobile BankID be enabled or disabled?
     * @return the builder
     */
    public RequirementBuilder mobile(final boolean enable) {
      this.enableMobile = enable;
      return this;
    }

    /**
     * Enables/disables use of BankID on file.
     *
     * <p>
     * By default, BankID on file is enabled.
     * </p>
     *
     * @param enable should BankID on file be enabled or disabled?
     * @return the builder
     */
    public RequirementBuilder onFile(final boolean enable) {
      this.enableOnFile = enable;
      return this;
    }

    /**
     * Enables/disables use of BankID on smart card.
     *
     * <p>
     * By default, BankID on smart card is enabled.
     * </p>
     *
     * @param enable should BankID on smart card be enabled or disabled?
     * @return the builder
     */
    public RequirementBuilder onSmartCard(final boolean enable) {
      this.enableOnSmartCard = enable;
      return this;
    }

    /**
     * Enables/disables use of Nordea BankID:s.
     *
     * <p>
     * By default, Nordea BankID:s are enabled.
     * </p>
     * <p>
     * Note: Nordea BankID:s will automatically be disabled if BanID on file and BankID on smart card are disabled.
     * </p>
     *
     * @param enable should Nordea BankID:s be enabled or disabled?
     * @return the builder
     */
    public RequirementBuilder nordea(final boolean enable) {
      this.enableNordea = enable;
      return this;
    }

    //
    // Helpers
    //
    public static String BANKID_ON_FILE(final boolean production) {
      return production ? CP_BANKID_ON_FILE : CP_BANKID_ON_FILE_TEST;
    }

    public static String BANKID_ON_SMARTCARD(final boolean production) {
      return production ? CP_BANKID_ON_SMARTCARD : CP_BANKID_ON_SMARTCARD_TEST;
    }

    public static String MOBILE_BANKID(final boolean production) {
      return production ? CP_MOBILE_BANKID : CP_MOBILE_BANKID_TEST;
    }

    public static String NORDEA_EID(final boolean production) {
      return production ? CP_NORDEA_EID : CP_NORDEA_EID_TEST;
    }

  }

  /**
   * Represents a requirement for which type of smart card reader that is required.
   */
  public enum CardReaderRequirement {

    /**
     * The transaction must be performed using a smart card reader where the PIN code is entered on the computer's
     * keyboard, or a card reader of a higher security class.
     */
    CLASS1("class1"),

    /**
     * The transaction must be performed using a smart card reader where the PIN code is entered on the reader itself,
     * or a card reader of a higher security class.
     */
    CLASS2("class2");

    /** The string value of the enum. */
    private final String value;

    /**
     * Constructor.
     *
     * @param value the string value of the enum
     */
    CardReaderRequirement(final String value) {
      this.value = value;
    }

    /**
     * Given a string representation its enum object is returned.
     *
     * @param value the string representation
     * @return a CardReaderRequirement or null if not match is found
     */
    @JsonCreator
    public static CardReaderRequirement forValue(final String value) {
      for (final CardReaderRequirement cr : CardReaderRequirement.values()) {
        if (cr.getValue().equals(value)) {
          return cr;
        }
      }
      throw new IllegalArgumentException("Unsupported card reader requirement - " + value);
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
