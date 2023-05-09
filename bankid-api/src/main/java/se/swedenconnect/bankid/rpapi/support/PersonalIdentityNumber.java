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
package se.swedenconnect.bankid.rpapi.support;

import java.io.Serializable;
import java.time.Year;
import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import se.swedenconnect.bankid.rpapi.LibraryVersion;

/**
 * A representation of a Swedish personal identity number. Also handles "samordningsnummer".
 *
 * @author Martin Lindström
 */
public class PersonalIdentityNumber implements Serializable {

  /** For serializing. */
  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /** Enum for formatting a personal identity number. */
  public enum Format {
    /** Twelve digits with no delimiter. */
    TWELVE_DIGITS_NO_DELIMITER,

    /** Twelve digits with a delimiter. */
    TWELVE_DIGITS_DELIMITER,

    /** Ten digits with no delimiter. */
    TEN_DIGITS_NO_DELIMITER,

    /** Ten digits with a delimiter. */
    TEN_DIGITS_DELIMITER
  }

  /** The century. */
  private int century;

  /** The year within the century. */
  private int year;

  /** The delimiter - '-' or '+'. */
  private char delimiter;

  /** The month. */
  private int month;

  /** The date. */
  private int date;

  /** The birth number - The three digits following the birth date. */
  private int birthNumber;

  /** The control digit. */
  private int controlDigit;

  /** Pattern for a personal identity number. */
  private static final Pattern pattern =
      Pattern.compile("^(\\d{2}){0,1}(\\d{2})(\\d{2})(\\d{2})([-|+]{0,1})?(\\d{3})(\\d{1})$");

  /**
   * Constructor that parses the supplied personal identity number.
   * <p>
   * The following formats are supported:
   * </p>
   * <ul>
   * <li>12 digits with no delimiter, e.g. "196904146856".</li>
   * <li>12 digits with a delimiter, e.g. "19690414-6856".</li>
   * <li>10 digits with no delimiter, e.g. "6904146856". In these cases the person is assumed to be under 100 years
   * old.</li>
   * <li>10 digits with a delimiter, e.g. "690414-6856". The year the person is 100 years old, the delimiter changes to
   * '+'.</li>
   * </ul>
   *
   * @param number the string representation of the personal identity number
   * @throws PersonalIdentityNumberException if the supplied number is not valid
   */
  public PersonalIdentityNumber(final String number) throws PersonalIdentityNumberException {
    Assert.notNull(number, "number must not be null");

    final Matcher matcher = pattern.matcher(number.trim());
    if (!matcher.find()) {
      throw new PersonalIdentityNumberException("Invalid format of personal identity number");
    }

    try {
      // First handle the year
      //
      this.processYear(matcher.group(1), matcher.group(2), matcher.group(5));

      // The month
      this.month = Integer.parseInt(matcher.group(3));

      // The date (may be larger for a samordningsnummer).
      this.date = Integer.parseInt(matcher.group(4));
      if (this.date < 1 || this.date > 31 && this.date < 61 || this.date > 91) {
        throw new PersonalIdentityNumberException("Invalid date - " + matcher.group(4));
      }

      // OK, we have the complete birth date. Let's check if it is a valid date ...
      //
      final Calendar cal = Calendar.getInstance();
      cal.setLenient(false);
      cal.set(this.century * 100 + this.year, this.month - 1, this.date > 60 ? this.date - 60 : this.date);
      try {
        cal.getTime();
      }
      catch (final Exception e) {
        final String msg = this.date > 60 ? "Invalid samordningsnummer" : "Invalid birth date";
        throw new PersonalIdentityNumberException(String.format("%s - %02d%02d%02d%02d",
            msg, this.century, this.year, this.month, this.date));
      }

      // The birth number and control digit
      this.birthNumber = Integer.parseInt(matcher.group(6));
      this.controlDigit = Integer.parseInt(matcher.group(7));

      // Validate the control digit
      //
      final int luhn = calculateLuhn(this.getNumber(Format.TEN_DIGITS_NO_DELIMITER).substring(0, 9));
      if (luhn != this.controlDigit) {
        throw new PersonalIdentityNumberException("Invalid personal identity number - control digit is incorrect");
      }
    }
    catch (final NumberFormatException e) {
      throw new PersonalIdentityNumberException("Invalid personal number", e);
    }
  }

  /**
   * Parses a personal identity number string into a {@code PersonalIdentityNumber}. See
   * {@link PersonalIdentityNumber#PersonalIdentityNumber(String)}.
   *
   * @param number the string representation of the personal identity number
   * @return a {@code PersonalIdentityNumber} object
   * @throws PersonalIdentityNumberException if the supplied number is not valid
   */
  @JsonCreator
  public static PersonalIdentityNumber parse(final String number) throws PersonalIdentityNumberException {
    return new PersonalIdentityNumber(number);
  }

  /**
   * Returns the personal identity number according to the {@link Format#TWELVE_DIGITS_NO_DELIMITER} format.
   *
   * @return the personal identity number as twelve digits with no delimiter
   */
  @JsonValue
  public String getNumber() {
    return this.getNumber(Format.TWELVE_DIGITS_NO_DELIMITER);
  }

  /**
   * Returns the personal identity number according to the required format.
   *
   * @param format the required format for the personal identity number string
   * @return personal identity number string
   */
  public String getNumber(final Format format) {
    if (format == Format.TWELVE_DIGITS_NO_DELIMITER) {
      return String.format("%02d%02d%02d%02d%03d%d", this.century, this.year, this.month, this.date, this.birthNumber,
          this.controlDigit);
    }
    else if (format == Format.TWELVE_DIGITS_DELIMITER) {
      return String.format("%02d%02d%02d%02d%c%03d%d", this.century, this.year, this.month, this.date, this.delimiter,
          this.birthNumber,
          this.controlDigit);
    }
    else if (format == Format.TEN_DIGITS_NO_DELIMITER) {
      return String.format("%02d%02d%02d%03d%d", this.year, this.month, this.date, this.birthNumber, this.controlDigit);
    }
    else { // TEN_DIGITS_DELIMITER
      return String.format("%02d%02d%02d%c%03d%d", this.year, this.month, this.date, this.delimiter, this.birthNumber,
          this.controlDigit);
    }
  }

  /**
   * Predicate that returns {@code true} if this is a "samordningsnummer".
   * <p>
   * See
   * https://www.skatteverket.se/foretagochorganisationer/myndigheter/informationsutbytemellanmyndigheter/folkbokforingsamordningsnummer.4.46ae6b26141980f1e2d3643.html.
   * </p>
   *
   * @return {@code true} if this is a "samordningsnummer" and {@code false} otherwise
   */
  public boolean isSamordningsnummer() {
    return this.date > 60;
  }

  /**
   * Parses the year digits.
   * <p>
   * If century digits are missing we look at the delimiter. This should be '+' from the year a person is 100 and '-'
   * otherwise. If the delimiter is missing we assume the person is under 100.
   * </p>
   *
   * @param centuryDigits century digits (may be null)
   * @param yearDigits the year digits (2 digits)
   * @param delimeter the delimiter (may be the empty string)
   * @throws NumberFormatException for illegal numbers (should not happen since the regex matched)
   */
  private void processYear(final String centuryDigits, final String yearDigits, final String delimeter)
      throws NumberFormatException {
    this.year = Integer.parseInt(yearDigits);
    if (centuryDigits == null) {
      // If delimiter is "+" the person is 100 or over, if it is "-" he or she is under 100,
      // and if there is no delimiter, we'll assume under 100.
      //
      if ("-".equals(delimeter) || delimeter.isEmpty()) {
        this.century = Year.now().minusYears(this.year).getValue() / 100;
        this.delimiter = '-';
      }
      else { // +
        this.century = Year.now().minusYears(100 + this.year).getValue() / 100;
        this.delimiter = '+';
      }
    }
    else {
      this.century = Integer.parseInt(centuryDigits);
      // No matter what the delimiter is set to, we make sure to use "-" if the person is
      // under 100 and "+" if he or she is 100 or over.
      //
      this.delimiter = Year.now().getValue() - (this.century * 100 + this.year) >= 100 ? '+' : '-';
    }
  }

  /**
   * Calculates a checksum of the passed value according to the luhn algorithm.
   *
   * @param number the value to calculate the checksum for
   * @return the luhn digit
   */
  private static int calculateLuhn(final String number) {
    int luhn = 0;
    for (int i = number.length() - 1; i >= 0; i--) {
      int c = number.charAt(i) - 48;
      luhn += i % 2 == 0 ? (c *= 2) > 9 ? c - 9 : c : c;
    }
    return luhn != 0 ? 10 - luhn % 10 : luhn;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(this.birthNumber, this.century, this.controlDigit, this.date, this.delimiter, this.month,
        this.year);
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || !(obj instanceof PersonalIdentityNumber)) {
      return false;
    }
    final PersonalIdentityNumber other = (PersonalIdentityNumber) obj;
    return this.birthNumber == other.birthNumber && this.century == other.century
        && this.controlDigit == other.controlDigit
        && this.date == other.date && this.delimiter == other.delimiter && this.month == other.month
        && this.year == other.year;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return this.getNumber(Format.TWELVE_DIGITS_DELIMITER);
  }

}
