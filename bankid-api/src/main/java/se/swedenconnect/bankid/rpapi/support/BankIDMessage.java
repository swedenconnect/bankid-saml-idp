/*
 * Copyright 2023 Litsec AB
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

import java.util.List;

/**
 * An abstraction for messages that are displayed for the user during a BankID operation.
 * 
 * @author Martin Lindström
 */
public interface BankIDMessage {

  /**
   * Returns the "short name" for the message to displayed.
   * <p>
   * See section 6 of the BankID Relying Party Guidelines.
   * </p>
   * 
   * @return the message short name
   */
  ShortName getShortName();

  /**
   * Returns an ordered list containing the message code(s) for this message.
   * <p>
   * If more than one message code is returned, the idea is to display the message divided into parts, for example in
   * separate p-tags.
   * </p>
   * 
   * @return a (non-empty) list of message codes
   */
  List<String> getMessageCodes();

  /**
   * Enumeration of the short names for the "Recommended User Messages" as defined in section 6 of the BankID Relying
   * Party Guidelines.
   */
  public enum ShortName {
    /** Start your BankID app. */
    RFA1,

    /** The BankID app is not installed. Please contact your internet bank. */
    RFA2,

    /** Action cancelled. Please try again. */
    RFA3,

    /** An identification or signing for this personal number is already started. Please try again. */
    RFA4,

    /** Internal error. Please try again. */
    RFA5,

    /** Action cancelled. */
    RFA6,

    /**
     * The BankID app is not responding. Please check that the program is started and that you have internet access. If
     * you don’t have a valid BankID you can get one from your bank. Try again.
     */
    RFA8,

    /** Enter your security code in the BankID app and select Identify or Sign. */
    RFA9,

    /** Enter your security code in the BankID app and select Identify. */
    RFA9_AUTH,

    /** Enter your security code in the BankID app and select Sign. */
    RFA9_SIGN,

    /** Trying to start your BankID app. */
    RFA13,

    /**
     * Searching for BankID:s, it may take a little while... If a few seconds have passed and still no BankID has been
     * found, you probably don’t have a BankID which can be used for this identification/signing on this computer. If
     * you have a BankID card, please insert it into your card reader. If you don’t have a BankID you can order one from
     * your internet bank. If you have a BankID on another device you can start the BankID app on that device.
     */
    RFA14_DESKTOP,

    /**
     * Searching for BankID:s, it may take a little while... If a few seconds have passed and still no BankID has been
     * found, you probably don’t have a BankID which can be used for this identification/signing on this device. If you
     * don’t have a BankID you can order one from your internet bank. If you have a BankID on another device you can
     * start the BankID app on that device.
     */
    RFA14_MOBILE,

    /**
     * Searching for BankID:s, it may take a little while... If a few seconds have passed and still no BankID has been
     * found, you probably don’t have a BankID which can be used for this identification/signing on this computer. If
     * you have a BankID card, please insert it into your card reader. If you don’t have a BankID you can order one from
     * your internet bank.
     */
    RFA15_DESKTOP,

    /**
     * Searching for BankID:s, it may take a little while... If a few seconds have passed and still no BankID has been
     * found, you probably don’t have a BankID which can be used for this identification/signing on this device. If you
     * don’t have a BankID you can order one from your internet bank.
     */
    RFA15_MOBILE,

    /**
     * The BankID you are trying to use is revoked or too old. Please use another BankID or order a new one from your
     * internet bank.
     */
    RFA16,

    /**
     * The BankID app couldn’t be found on your computer or mobile device. Please install it and order a BankID from
     * your internet bank. Install the app from your app store or https://install.bankid.com.
     */
    RFA17_PNR,

    /**
     * Failed to scan the QR code. Start the BankID app and scan the QR code. If you don't have the BankID app, you need
     * to install it and order a BankID from your internet bank. Install the app from your app store or
     * https://install.bankid.com.
     */
    RFA17_QR,

    /**
     * Start the BankID app.
     * <p>
     * The name of link or button used to start the BankID App
     * </p>
     */
    RFA18,

    /** Would you like to identify yourself or sign with a BankID on this computer or with a Mobile BankID? */
    RFA19,

    /** Would you like to identify yourself with a BankID on this computer or with a Mobile BankID? */
    RFA19_AUTH,

    /** Would you like to sign with a BankID on this computer or with a Mobile BankID? */
    RFA19_SIGN,

    /** Would you like to identify yourself or sign with a BankID on this device or with a BankID on another device? */
    RFA20,

    /** Would you like to identify yourself with a BankID on this device or with a BankID on another device? */
    RFA20_AUTH,

    /** Would you like to sign with a BankID on this device or with a BankID on another device? */
    RFA20_SIGN,

    /** Identification or signing in progress. */
    RFA21,

    /** Identification in progress. */
    RFA21_AUTH,

    /** Signing in progress. */
    RFA21_SIGN,

    /** Unknown error. Please try again. */
    RFA22,

    /**
     * Extension: Should QR code or personal identity number be used when starting initiating the operation on other
     * device?
     * <p>
     * Note: This should not be a question that you ask the end user. It should be a Relying Party configuration. But we
     * include this message for the cases where demo and test applications is built using this library.
     * </p>
     */
    EXT1,

    /**
     * Extension: Start the BankID app and read the QR code.
     */
    EXT2,

    /**
     * Extension: Since you are not using Safari the BankID app can not be automatically started. Start the app
     * manually, and then go back to the application.
     */
    EXT3,

    /**
     * Extension: The BankID app can not be started automatically. Therefore, you need to provide your personal identity
     * number.
     */
    EXT4;

  }

}
