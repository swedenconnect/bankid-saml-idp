![Logo](images/sweden-connect.png)

# Event Logging

The BankID SAML IdP application produces event logs using the Spring Boot Actuator.

> TODO: more here

## Events

This section lists all audit events that are produced.

### Received Request

A request for authentication or signing was received. Note that this event is published before
any communication with the BankID server is initiated.

**Parameters:**

| Parameter | Description |
| :--- | :--- |
| `type` | `BANKID_RECEIVED_REQUEST` |
| `operation` | The type of operation. Possible values are `auth` and `sign`. |
| `rp` | The name of the Relying Party. |
| `entityId` | The SAML entityID of the SAML SP that sent the request. |
| `samlId` | The ID of the SAML authentication request message. |
| `timestamp` | The timestamp. |

### Initiated Operation

The BankID operation has been initiated, i.e., the underlying BankID server has been invoked.

**Parameters:**

| Parameter | Description |
| :--- | :--- |
| `type` | `BANKID_INIT` |
| `operation` | The type of operation. Possible values are `auth` and `sign`. |
| `rp` | The name of the Relying Party. |
| `entityId` | The SAML entityID of the SAML SP that sent the request. |
| `samlId` | The ID of the SAML authentication request message. |
| `orderRef` | The BankID order reference. |
| `timestamp` | The timestamp. |

### Authentication Completed

An BankID authentication operation has been successfully completed.

**Parameters:**

| Parameter | Description |
| :--- | :--- |
| `type` | `BANKID_AUTH_COMPLETE` |
| `operation` | The type of operation. Will always be `auth`. |
| `rp` | The name of the Relying Party. |
| `entityId` | The SAML entityID of the SAML SP that sent the request. |
| `samlId` | The ID of the SAML authentication request message. |
| `orderRef` | The BankID order reference. |
| `userId` | The Swedish personal identity number of the user that was authenticated. |
| `timestamp` | The timestamp. |

### Signing Completed

An BankID signature operation has been successfully completed.

**Parameters:**

| Parameter | Description |
| :--- | :--- |
| `type` | `BANKID_SIGN_COMPLETE` |
| `operation` | The type of operation. Will always be `sign`. |
| `rp` | The name of the Relying Party. |
| `entityId` | The SAML entityID of the SAML SP that sent the request. |
| `samlId` | The ID of the SAML authentication request message. |
| `orderRef` | The BankID order reference. |
| `userId` | The Swedish personal identity number of the user that performed the signing. |
| `timestamp` | The timestamp. |

### Operation cancelled

An operation that was started was cancelled by the user.

**Parameters:**

| Parameter | Description |
| :--- | :--- |
| `type` | `BANKID_CANCEL` |
| `operation` | The type of operation. Possible values are `auth` and `sign`. |
| `rp` | The name of the Relying Party. |
| `entityId` | The SAML entityID of the SAML SP that sent the request. |
| `samlId` | The ID of the SAML authentication request message. |
| `orderRef` | The BankID order reference. |
| `timestamp` | The timestamp. |

### Erroneous Operation

An error occurred when processing a BankID request.

**Parameters:**

| Parameter | Description |
| :--- | :--- |
| `type` | `BANKID_ERROR` |
| `operation` | The type of operation. Possible values are `auth` and `sign`. |
| `rp` | The name of the Relying Party. If not known, a value of `unknown` is used. |
| `entityId` | The SAML entityID of the SAML SP that sent the request. If not known, `unknown` is used. |
| `samlId` | The ID of the SAML authentication request message. |
| `orderRef` | The BankID order reference. If operation has not been fully initiated, `not-set` is used. |
| `errorCode` | The code for the error. |
| `errorDescription` | A textual description of the error. |
| `timestamp` | The timestamp. |

-----

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).