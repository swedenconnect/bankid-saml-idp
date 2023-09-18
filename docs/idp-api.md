![Logo](images/sweden-connect.png)

# BankID IdP Backend API

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

For those who wish to implement their own BankID IdP Frontends this page contains a description of
the BankID IdP Backend API.

<a name="api-endpoints"></a>
## API Endpoints

<a name="polling"></a>
### Polling

The polling endpoint is the primary endpoint that the frontend will call in order to communicate
with the BankID server (via the IdP backend).

> TODO: More. Periodically invoke this ....

**Path:** `/api/poll`

**Method:** POST

**Request Parameters:** The query parameter `qr` is optionally included in the request (possible values
are `true` and `false`, where `false` is the default). This parameter tells whether the frontend wants
to display a BankID QR code, and by setting the value to `true` the backend will generate a QR-code and
include in the resulting response (see below).

**Response Status Codes:**

| HTTP Status Code | Description |                                                                                                                                                                                                                                                                                                                                                                                                      
| :--- | :--- |
| `200`  | OK |                                                                                                                                                                                                                                                                                                                                                                                                                
| `429`  | Too many attempts - resource busy. Parallel requests have been made for the same user, a header "Retry-After" will be present as a time for when you can retry the request again. <br/>This can also happen when a request has been blocked by the circuit breaker due to API errors towards the BankID server API.<br />This status code means that the request has been rejected before being made towards BankID's API. |


**Response Object:** [ApiResponse](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/authn/api/ApiResponse.java)

| Field | Type | Description |
| :--- | :--- | :--- |
| `status` | String | The status code for the response. Can be any of the following: `NOT_STARTED` (the operation against the BankID Server has not been started), `IN_PROGRESS` (the BankID operation is in progress), `ERROR` (an error response, check `messageCode` to know which error message to display), `COMPLETE` (the BankID operation has been completed) and `CANCEL` (the BankID operation has been cancelled by the user). |
| `qrCode` | String | Optionally holds the generated QR code that is to be displayed for the user. Base64-encoded string. |
| `autoStartToken` | String | Optionally holds the BankID auto start token. See [BankID Integration Guide](https://www.bankid.com/utvecklare/guider/teknisk-integrationsguide/programstart). |
| `messageCode` | The code for the message that should be displayed for the user. |

<a name="cancelled-operation"></a>
### Cancelled Operation

API-call that should be invoked if the user cancels the operation.

**Path:** `/api/cancel`

**Method:** POST

**Request Parameters:** None

**Response Status Codes:** `200` for a successfully received call and `500` for internal errors.

**Response Object:** None

<a name="selected-device"></a>
### Selected Device

In the case where a BankID signature is carried out we want to ensure that the user is not prompted
whether to use "BankID on this device" or "Another device". The reason is that in almost all cases
a signing operation follows a login (authentication) operation, and it would not be a good user
experience to have to answer the same question twice within the same session.

**Path:** `/api/device`

**Method:** GET

**Request Parameters:** None

**Response Status Codes:** `200` for a successfully executed call and `500` for internal errors.

**Response Object:** [SelectedDeviceInformation](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/authn/api/SelectedDeviceInformation.java)

| Field | Type | Description |
| :--- | :--- | :--- |
| `isSign` | Boolean | Is the current BankID operation a sign operation? |
| `device` | String | The selected device. Can be any of `this` (BankID on **this** device was selected), `other` (BankID on **another** device was selected) or `unknown` (no previous device selection is available). |

<a name="service-information"></a>
### Service/Status Information

An endpoint that delivers service, or status, information.

**Path:** `/api/status`

**Method:** GET

**Request Parameters:** None

**Response Status Codes:** `200` for a successfully executed call and `500` for internal errors.

**Response Object:** [ServiceInformation](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/authn/api/ServiceInformation.java)

| Field | Type | Description |
| :--- | :--- | :--- |
| `status` | String | `OK` for no problems and `ISSUES` if the BankID IdP has encountered issues. |

<a name="relying-party-information"></a>
### Relying Party Information

An endpoint that delivers information about the Relying Party, or SAML Service Provider, that 
has made the current authentication request to the BankID IdP.

**Path:** `/api/sp`

**Method:** GET

**Request Parameters:** None

**Response Status Codes:** `200` for a successfully executed call and `500` for internal errors.

**Response Object:** [SpInformation](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/authn/api/SpInformation.java)

| Field | Type | Description |
| :--- | :--- | :--- |
| `displayNames` | Map holding pairs of language codes and display names. | Contains display names for the SP in the languages declared by the SP. |
| `imageUrl` | String | URL to the SP logotype. |


<a name="contact-information"></a>
### Contact Information

An endpoint that delivers service contact information. Typically this information is displayed when
an error has occurred.

**Path:** `/api/contact`

**Method:** GET

**Request Parameters:** None

**Response Status Codes:** `200` for a successfully executed call and `500` for internal errors.

**Response Object:** [CustomerContactInformation](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/authn/api/CustomerContactInformation.java)

| Field | Type | Description |
| :--- | :--- | :--- |
| `email` | String | The email address that should be displayed in the UI for customer support/contact. |
| `displayInformation` | Boolean | Whether the UI should display contact information. |

<a name="views"></a>
## Views

When writing a custom frontend it is also useful to now about about the backend views described in
this section. You can either replace the index page with your own frontend or host it separately.

If the frontend should be hosted separately the `/bankid` path (see below) should be disabled by using the configuration flag `bankid.built-in-frontend = false`. 

### /bankid

This is the primary view that displays the frontend that has been built into resources (defaults to Vue frontend).

### /view/complete

Redirect here to consume a completed BankID operation and get a SAML `Response` message to be posted
back to the SAML Service Provider.

### /view/cancel

Redirect here if the user wants to cancel the current login. The user will be posted back to the SAML
Service Provider.

-----

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
