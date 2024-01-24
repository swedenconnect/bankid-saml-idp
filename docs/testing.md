![Logo](images/sweden-connect.png)

# BankID IdP Acceptance Test Template

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

-----
         
## Introduction

The purpose of this document is to give an example of acceptance test cases for testing a deployment
of the BankID IdP application.

## Test Environment

Within Sweden Connect there is a test application, Test your eID, that has been used to test the
BankID IdP default setup in the Sweden Connect Sandbox-federation.

All tests cases in this example document have [Test my eID](https://eid.idsec.se/testmyeid/) in the
Sandbox-federation as precondition. 

For organizations that wish to test their own instance of the BankID IdP will have to use some other
SAML SP. An example is to use the "fine-grained" [Test SAML SP for the Swedish eID Framework and eIDAS](https://eid.litsec.se/svelegtest-sp/sp). Another example is to set up your own copy of "Test me eID", by cloning or forking the [https://github.com/swedenconnect/test-my-eid](https://github.com/swedenconnect/test-my-eid) repo and configure it for your purposes.


## Test Data and Preconditions

BankID on laptop/desktop and mobile devices will be needed. It has to be configured in test mode according to guidelines found at the [Demo BankID page](https://demo.bankid.com).

**Note:** It is not possible to have a BankID app both in test and live production mode on the same device.

## Requirements

For the implementation within Sweden Connect there is an overview of the combinations of browsers and OS that has been tested at the initial implementation of the BankID IdP. This table can be found [here](https://docs.swedenconnect.se/bankid-saml-idp/browsers.html).

## Test cases

The test cases below are examples of four main use cases that can be used for acceptance testing of 
the BankID IdP. The use cases 1-4 may need to be executed on different devices, browsers and operating
systems and in different combinations that reflects specific needs and demands of the implementing
organization.

To get a better test coverage, repeat test cases 1-4 on the OS, Browsers and device combinations that
the organization need to support. 

### 1. Desktop with MacOS, Safari Browser

#### 1.1. Successful authentication - BankID on this device

Perform a successful authentication by selecting 'BankID on this device', allow BankID to start and submit a valid password.

**Expected condition:** Following attributes should be received in the response: Personal identity number, Given name, Family name, Full name and a Transaction ID.

> Note: Other attributes are also delivered but not displayed by "Test my eID". See [Test SAML SP for the Swedish eID Framework and eIDAS](https://eid.litsec.se/svelegtest-sp/sp).

##### 1.1.1. Successful signing

Perform a successful signature by selecting 'Sign using BankID' at the authentication response page. Sign in the app with a valid password.

**Expected condition:** The selection page between 'BankID on this device' and 'Mobile BankID on other device' should not be visible in this flow. The same selection as in 1.1 should be default. 
Same attributes as above should be received and additional attribute "Signature message digest".

##### 1.1.2. Cancelling signing in by decline the BankID app from starting

Repeat 1.1, then initiate signature by selecting 'Sign using BankID'. In the Consent dialogue for opening the BankID app from the browser, press Cancel instead of Continue.

**Expected condition:** Consent dialogue should be closed and user can choose between Cancel or Click 'Click here if the BankID app did not start automatically within 5 seconds.' link to manually start the BankID app.

> Note: If user do not make any selection after Cancel, the Safari browser will show the Consent to open BankID again after a few seconds. Other browsers may handle this differently.

##### 1.1.3. Successful signing by manually open BankID

Continue after 1.1.2, click on link 'Click here if the BankID app did not start automatically within 5 seconds.' and Continue to open the BankID app. Submit a valid password.

**Expected condition:** The selection page between 'BankID on this device' and 'Mobile BankID on other device' should not be visible in this flow. The same selection as in 1.1 should be default. 
Same attributes as above should be received and additional attribute "Signature message digest".

##### 1.1.4. Cancelling signing in the BankID app

Repeat 1.1, then initiate signature by selecting 'Sign using BankID' at the authentication response page. Allow the app to start but in the BankID app, press Cancel instead of providing a password.

**Expected condition:** Signature should be aborted and user should be redirected back to initiating SP.

#### 1.2. Cancelling authentication at choose device page

Initiate authentication but press Cancel at first page where it is possible to select between 'BankID on this device' or 'Mobile BankID on other device'.

**Expected condition:** User should be redirect back to the SP and no successful authentication should be performed.

#### 1.3. Cancelling authentication in the BankID app

Initiate authentication by selecting 'BankID on this device', allow the app to start but press Cancel instead of submitting a valid password.

**Expected condition:** User should be redirect back to the SP and no successful authentication should be performed.

#### 1.4. Cancelling authentication by declining the BankID app from starting

Initiate authentication by selecting 'BankID on this device' but press Cancel in the consent dialog when browser asks for permission to start BankID app.

**Expected condition:** Consent dialogue should be closed and user can choose between Cancel or Click 'Click here if the BankID app did not start automatically within 5 seconds.' link to manually start the BankID app.

> Note: If user do not make any selection after Cancel, the Safari browser will show the Consent to open BankID again after a few seconds. Other browsers may handle this differently.

##### 1.4.1. Successful authentication by manually open BankID

Continue after 1.4, click on link 'Click here if the BankID app did not start automatically within 5 seconds.' and Continue to open the BankID app. Submit a valid password.

**Expected condition:** The selection page between 'BankID on this device' and 'Mobile BankID on other device' should not be visible in this flow. The same selection as in 1.3.1 should be default. 
Same attributes as in 1.1 should be received.

#### 1.5. Timeout at choose device page

Initiate authentication but instead of selecting 'BankID on this device' or 'Mobile BankID on other device' wait around 60 minutes and let the session time out.

**Expected condition:** An error page should be displayed telling that the session has expired 'Required session data could not be found' and that browser window should be closed.


#### 1.6. Timeout after app has started

Initiate authentication by selecting 'BankID on this device', allow app to open and then let the session time out instead of submitting password.

**Expected condition:** BankID should abort the authentication and user should be informed that Identification aborted due to too long processing time or authentication completed in other unit.

> Note: The timeout is configurable. See setting `bankid.start-retry-duration`.

The IdP should inform the user that the BankID app did not answer and offer the user to  Cancel or Try again.

##### 1.6.1. Cancel after timeout in app

Repeat 1.6. then press Cancel.

**Expected condition:** User should be redirected back to SP with error code "urn:oasis:names:tc:SAML:2.0:status:AuthnFailed" and error message "User authentication failed".

##### 1.6.2. Retrying after timeout in app

Repeat 1.6 then press Retry. Allow BankID app to start if not started automatically and submit a valid password.

**Expected condition:** Successful authentication with same attributes as in 1.1.

#### 1.7. Change language in UI

Initiate authentication, on the selection page between 'BankID on this device' and 'Mobile BankID on other device'. Change the language. Then press Cancel.
Initiate authentication again.

**Expected condition:** The language presented should correspond to the selected language.


### 2. Ipad with iOS, Safari Browser

#### 2.1. Successful authentication - BankID on this device

Perform a successful authentication by selecting 'BankID on this device' and submit a valid password/Touch ID.

**Expected condition:** Following attributes should be received in the response: Personal identity number, Given name, Family name, Full name and a Transaction ID.

> Note: Other attributes are also delivered but not displayed by "Test my eID". See [Test SAML SP for the Swedish eID Framework and eIDAS](https://eid.litsec.se/svelegtest-sp/sp).

##### 2.1.1. Successful signing

Perform a successful signature by selecting 'Sign using BankID' at the authentication response page. Sign in the app with a valid password.

**Expected condition:** The selection page between 'BankID on this device' and 'Mobile BankID on other device' should not be visible in this flow. The same selection as in 2.1 should be default. 
Same attributes as above should be received and additional attribute "Signature message digest".

#### 2.1.2. Cancelling signing in the BankID app

Repeat 2.1, then initiate signature by selecting 'Sign using BankID' at the authentication response page. In the bankID app, press Cancel instead of signing with a passcode.

**Expected condition:** Signature should be aborted and user should be redirected back to initiating SP.

#### 2.2. Cancelling authentication at choose device page

Initiate authentication but press Cancel at first page where where it is possible to select between 'BankID on this device' or 'Mobile BankID on other device'.

**Expected condition:** User should be redirect back to the SP and no successful authentication should be performed.


#### 2.3. Cancelling authentication in the BankID app

Initiate authentication by selecting 'BankID on this device' but press Cancel instead of submitting a valid password. Note! if Touch ID is activated, this has to be inactivated first in BankID app settings.

**Expected condition:** User should be redirect back to the SP and no successful authentication should be performed.


#### 2.4. Timeout at choose device page

Initiate authentication but instead of selecting 'BankID on this device' or 'Mobile BankID on other device' wait around 60 minutes and let the session time out.

**Expected condition:**  An error page should be displayed telling that the session has expired 'Required session data could not be found' and that browser window should be closed.


#### 2.5. Timeout after app has started 

Initiate authentication by selecting 'BankID on this device', press 'Identify with Touch ID/Password'. Cancel in the app after a few minutes when the session has timed out instead of submitting password.

**Expected condition:** BankID authentication is aborted and user should be informed that Identification aborted due to inactivity.

> Note: The timeout is configurable. See setting `bankid.start-retry-duration`.

The Idp should inform the user that 'The BankID operation has expired' and offer the user to Cancel or Try again.

##### 2.5.1. Cancel after timeout in app

Repeat 2.5 then press Cancel.

**Expected condition:** User should be redirected back to SP with error code 'urn:oasis:names:tc:SAML:2.0:status:AuthnFailed' and error message 'Use authentication failed'.

##### 2.5.2. Retrying after timeout in app

Repeat 2.5 then press Try again. Then start BankID if not started automatically and submit a valid password.

**Expected condition:** Successful authentication with same attributes as in 2.1.

##### 2.5.3. Timeout after app has started 

Initiate authentication by selecting 'BankID on this device', Do not press 'Identify with Touch ID/Password', wait a few minutes and let the session time out. 

**Expected condition:** BankID informs user that Identification aborted due to inactivity.

The Idp should inform the user that 'The BankID operation has expired' and offer the user to Cancel or Try again.

##### 2.5.4. Cancel after timeout in app

Repeat 2.5.3 then press Cancel.

**Expected condition:** User should be redirected back to SP with error code 'urn:oasis:names:tc:SAML:2.0:status:AuthnFailed' and error message 'Use authentication failed'.

##### 2.5.5. Retry after timeout in app

Repeat 2.5.3 then press Try again. Start BankID if not started automatically and submit a valid password.

**Expected condition:** Successful authentication with same attributes as in 2.1.

#### 2.6. Change language in UI

Initiate authentication, on the selection page between 'BankID on this device' and 'Mobile BankID on other device'. Change the language. Then press Cancel.
Initiate authentication again.

**Expected condition:** The language presented should correspond to the selected language.


### 3. Desktop with MacOS, Safari Browser and BankID on Ipad iOS, Safari browser

#### 3.1. Successful authentication - Mobile BankID on other device

Perform a successful authentication by selecting 'Mobile BankID on other device' on the laptop. Scan the QR-code using your tablet device and enter valid code/Touch ID.

**Expected condition:** Following attributes should be received in the response: Personal identity number, Given name, Family name, Full name and a Transaction ID.

> Note: Other attributes are also delivered but not displayed by "Test my eID". See [Test SAML SP for the Swedish eID Framework and eIDAS](https://eid.litsec.se/svelegtest-sp/sp).

##### 3.1.1. Successful signing

Perform a successful signing by selecting 'Sign using BankID' at the authentication response page. Scan the QR-code in the BankID app, sign in the app with a valid passcode.

**Expected condition:** The selection page between 'BankID on this device' and 'Mobile BankID on other device' should not be visible in this flow. The same selection as in 3.1 should be default. Same attributes as above should be received and additional attribute: Signature message digest.

##### 3.1.2. Cancelling signing in the BankID app

Repeat 3.1, then initiate signature by selecting 'Sign using BankID' at the authentication response page. Scan the QR-code in the BankID app, but press Cancel instead of providing a passcode.

**Expected condition:** Signature should be aborted and user should be redirected back to initiating SP.

#### 3.2. Close QR code

Initiate authentication by selecting 'Mobile BankID on other device' on the laptop. In the pop-up window where QR should be scanned, press Close.

**Expected condition:** QR-code window should be closed and user should view page where device selection between 'BankID on this device' or 'Mobile BankID on other device' is presented.

##### 3.2.1. Successful authentication after close and open QR-code

Initiate authentication by selecting 'Mobile BankID on other device' on the laptop. In the pop-up window where QR should be scanned, press Close. Then press 'Mobile BankID on other device' and scan the QR-code. Submit a valid passcode/Tocuh ID.

**Expected condition:** Successful authentication with same attributes as in 3.1.

#### 3.3. Cancel in BankID app

Initiate authentication by selecting 'Mobile BankID on other device' on the laptop. Scan QR code in the pop-up window with the tablet device but press Cancel in the BankID app instead of submit of password/Touch ID.

**Expected condition:** User should be redirect back to the SP and no successful authentication should be performed.

##### 3.3.1. Close dialogue in desktop browser after scanning of QR-code on tablet device

Initiate authentication by selecting 'Mobile BankID on other device' on the laptop. Scan QR code with tablet device in the pop-up window. In desktop browser, close the information dialog 'Enter your security code in the BankID app and select Identify.'. Continue with BankID on table device by submit of valid password/Touch ID.

**Expected condition:** Successful authentication with same attributes as in 3.1.

#### 3.4. Timeout at choose device page

Initiate authentication but instead of selecting 'BankID on this device' or 'Mobile BankID on other device' wait around 60 minutes and let the session time out.

**Expected condition:** An error page should be displayed telling that the session has expired 'Required session data could not be found' and that browser window should be closed.


#### 3.5. Timeout after app has started

Initiate authentication by selecting 'Mobile BankID on other device'. Scan the QR-code with the tablet device. Press 'Identify with password/Touch ID' in BankID app. Cancel in the app after a few minutes and identification process has timed out in your browser.

**Expected condition:** BankID authentication is aborted. User is informed that identification aborted due to inactivity.

The Idp should inform the user that 'The BankID app is not responding. Please check that the program is started and that you have internet access. If you do not have a valid BankID you can get one from your bank. Then try again.' and offer the user to Cancel or Try again.

##### 3.5.1. Cancel after timeout in app

Repeat 3.5 then press Cancel.

**Expected condition:** User should be redirected back to SP with error code 'urn:oasis:names:tc:SAML:2.0:status:AuthnFailed' and error message 'User authentication failed'.

##### 3.5.2. Retrying after timeout in app

Repeat 3.5 then press Try again. Scan the QR-code and submit a valid password/Touch ID.

**Expected condition:** Successful authentication with same attributes as in 3.1.

##### 3.5.3. Timeout after app has started

Initiate authentication by selecting 'Mobile BankID on other device'. Scan the QR-code with Tablet device. Do not press 'Identify with password/Touch ID' in BankID app. Wait a few minutes and until identification process times out in your browser.

**Expected condition:** BankID authentication is aborted. User is informed that identification aborted due to inactivity.

The Idp should inform the user that 'The BankID app is not responding. Please check that the program is started and that you have internet access. If you do not have a valid BankID you can get one from your bank. Then try again.' and offer the user to Cancel or Try again.

##### 3.5.4. Cancel after timeout in app

Repeat 3.5.3 then press Cancel.

Expected condition: User should be redirected back to SP with error code 'urn:oasis:names:tc:SAML:2.0:status:AuthnFailed' and error message 'User authentication failed'.

##### 3.5.5. Retrying after timeout in app
Repeat 3.5.3 then press Try again. Scan the QR-code and submit a valid passcode/Touch ID.

**Expected condition:** Successful authentication with same attributes as in 3.1.

#### 3.6. Change language in UI

Initiate authentication, on the selection page between 'BankID on this device' and 'Mobile BankID on other device'. Change the language. Then press Cancel.
Initiate authentication again.

**Expected condition:** The language presented should correspond to the selected language.

### 4. Ipad iOS with Chrome and BankID on Ipad iOS with Safari

#### 4.1. Successful authentication - Mobile BankID on other device

Perform a successful authentication by selecting 'Mobile BankID on other device' on tablet device with Chrome browser. Scan the QR-code using your other tablet device and enter valid code/Touch ID.

**Expected condition:** Following attributes should be received in the response: Personal identity number, Given name, Family name, Full name and a Transaction ID.

> Note: Other attributes are also delivered but not displayed by "Test my eID". See [Test SAML SP for the Swedish eID Framework and eIDAS](https://eid.litsec.se/svelegtest-sp/sp).

##### 4.1.1. Successful signing

Perform a successful signing by selecting 'Sign using BankID' at the authentication response page. Scan the QR-code in the BankID app, sign in the app with a valid passcode.

**Expected condition:** The selection page between 'BankID on this device' and 'Mobile BankID on other device' should not be visible in this flow. The same selection as in 4.1 should be default. Same attributes as above should be received and additional attribute: Signature message digest.

##### 4.1.2. Cancelling signing in the BankID app

Repeat 4.1, then initiate signature by selecting 'Sign using BankID' at the authentication response page. Scan the QR-code in the BankID app with the other tablet device, but press Cancel instead of providing a passcode.

**Expected condition:** Signature should be aborted and user should be redirected back to initiating SP.

#### 4.2. Close QR code

Initiate authentication by selecting 'Mobile BankID on other device' on tablet device with Chrome browser. In the pop-up window where QR should be scanned, press Close.

**Expected condition:** QR-code window should be closed and user should view page where device selection between 'BankID on this device' or 'Mobile BankID on other device' is presented.

##### 4.2.1. Successful authentication after close and open QR-code

Initiate authentication by selecting 'Mobile BankID on other device' on tablet device with Chrome browser. In the pop-up window where QR should be scanned, press Close. Then press 'Mobile BankID on other device' and scan the QR-code with the other table device. Submit a valid passcode/Touch ID.

**Expected condition:** Successful authentication with same attributes as in 4.1.

#### 4.3. Cancel in BankID app

Initiate authentication by selecting 'Mobile BankID on other device' on tablet device with Chrome browser. Scan QR code in the pop-up window with the other tablet device but press Cancel in the BankID app instead of submit of password/Touch ID.

**Expected condition:** User should be redirect back to the SP and no successful authentication should be performed.

##### 4.3.1 Close dialogue in Chrome browser after scanning of QR-code on tablet device

Initiate authentication by selecting 'Mobile BankID on other device' on tablet device with Chrome browser. Scan QR code in the pop-up window with the other tablet device. In chrome browser, close the information dialog 'Enter your security code in the BankID app and select Identify.'. Continue with BankID on table device by submit of valid password/Touch ID.

**Expected condition:** Successful authentication with same attributes as in 4.1.

#### 4.4. Timeout at choose device page

Initiate authentication but instead of selecting 'BankID on this device' or 'Mobile BankID on other device' wait around 60 minutes and let the session time out.

**Expected condition:** An error page should be displayed telling that the session has expired 'Required session data could not be found' and that browser window should be closed.

#### 4.5. Timeout after app has started

Initiate authentication by selecting 'Mobile BankID on other device' on tablet device with Chrome browser. Scan the QR-code with the other tablet device. Press 'Identify with password/Touch ID' in BankID app. Cancel in the app after a few minutes and identification process has timed out in your browser.

**Expected condition:** BankID authentication is aborted. User is informed that identification aborted due to inactivity.

The Idp should inform the user that 'The BankID app is not responding. Please check that the program is started and that you have internet access. If you do not have a valid BankID you can get one from your bank. Then try again.' and offer the user to Cancel or Try again.

##### 4.5.1. Cancel after timeout in app

Repeat 4.5 then press Cancel.

**Expected condition:** User should be redirected back to SP with error code 'urn:oasis:names:tc:SAML:2.0:status:AuthnFailed' and error message 'User authentication failed'.

##### 4.5.2. Retrying after timeout in app

Repeat 4.5 then press Try again. Scan the QR-code and submit a valid password/Touch ID.

**Expected condition:** Successful authentication with same attributes as in 4.1.

##### 4.5.3. Timeout after app has started

Initiate authentication by selecting 'Mobile BankID on other device' on tablet device with Chrome browser. Scan the QR-code with Tablet device. Do not press 'Identify with password/Touch ID' in BankID app. Wait a few minutes and until identification process times out in your browser.

**Expected condition:** BankID authentication is aborted. User is informed that identification aborted due to inactivity.

The Idp should inform the user that 'The BankID app is not responding. Please check that the program is started and that you have internet access. If you do not have a valid BankID you can get one from your bank. Then try again.' and offer the user to Cancel or Try again.

##### 4.5.4. Cancel after timeout in app

Repeat 4.5.3 then press Cancel.

Expected condition: User should be redirected back to SP with error code 'urn:oasis:names:tc:SAML:2.0:status:AuthnFailed' and error message 'User authentication failed'.

##### 4.5.5. Retrying after timeout in app
Repeat 4.5.3 then press Try again. Scan the QR-code and submit a valid passcode/Touch ID.

**Expected condition:** Successful authentication with same attributes as in 4.1.

#### 4.6. Change language in UI

Initiate authentication, on the selection page between 'BankID on this device' and 'Mobile BankID on other device' on tablet device with chrome browser. Change the language. Then press Cancel.
Initiate authentication again.

**Expected condition:** The language presented should correspond to the selected language.

-----

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).




