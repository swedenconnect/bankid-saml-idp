![Logo](images/sweden-connect.png)

# Audit Event Logging

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This page describes the audit event logging feature of the BankID IdP.

<a name="audit-configuration"></a>
## Audit Configuration

The [Configuration of the BankID SAML IdP](https://docs.swedenconnect.se/bankid-saml-idp/configuration.html) has a section about [Audit Logging Configuration](https://docs.swedenconnect.se/bankid-saml-idp/configuration.html#audit-logging-configuration). This chapter describes this
configuration in more detail.

The setting `bankid.audit.repository` should be set to any of the following values:

- `memory` - An in-memory audit event repository is used. The events can be accessed via Spring Boot Actuator's `auditevents` endpoint. Using the defaults from [application.yml](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/resources/application.yml) this URL would then be `https://<yourdomain>:8444/actuator/auditevents`. This is the default if no setting is provided.

- `redislist` - Events are persisted using a Redis List. In order for this setting to function, Redis must also have been configured, see [Redis Configuration](https://docs.swedenconnect.se/bankid-saml-idp/configuration.html#redis-configuration). Also see [RedisListAuditEventRepository](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/audit/RedisListAuditEventRepository.java) for details.

- `redistimeseries` - Events are persisted using the Redis Time series-feature. In order for this setting to function, Redis must also have been configured, see [Redis Configuration](https://docs.swedenconnect.se/bankid-saml-idp/configuration.html#redis-configuration). Also see [RedisTimeSeriesAuditEventRepository](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/audit/RedisTimeSeriesAuditEventRepository.java) for details.

- `other` - This setting should be assigned if you extend the BankID SAML IdP and wants to provide a audit repository of your own. See [Providing a Custom Audit Event Repository](#providing-a-custom-audit-event-repository) below.

By assigning the setting `bankid.audit.log-file` the BankID IdP Auditing feature will also write audit logs to file. Each line in this file will contain an audit event in JSON format.

> The log file feature will use a rolling date handler, meaning that for each day a new log file is created and the previous log file is renamed to `<logfile>-<date>.log`.

Finally, using the `bankid.audit.supported-events` setting it is also possible to exclude certain events from being stored/written. See all events below.

<a name="providing-a-custom-audit-event-repository"></a>
## Providing a Custom Audit Event Repository

If you extend the BankID SAML IdP you can provide your own implementation of the audit event repository. For example, you may want to store audit events in a database.

Your custom implementation should extend the [AbstractBankIdAuditEventRepository](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/audit/AbstractBankIdAuditEventRepository.java) class if you want to keep the support for
file logging and filtering of events. If that is not relevant for you, you may instead implement Spring's 
[AuditEventRepository](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/actuate/audit/AuditEventRepository.html).

Next, you need a Spring configuration class to create your bean. The example below illustrates this class:

```
@Configuration
@EnableConfigurationProperties(BankIdConfigurationProperties.class)
public class CustomAuditRepositoryConfiguration {

  private final BankIdConfigurationProperties.AuditConfiguration config;

  public CustomAuditRepositoryConfiguration(final BankIdConfigurationProperties properties) {
    this.config = Objects.requireNonNull(properties, "properties must not be null").getAudit();
  }

  @Bean
  @ConditionalOnProperty(value = "bankid.audit.repository", havingValue = "other", matchIfMissing = false)
  AuditEventRepository customAuditRepository(final AuditEventMapper mapper) throws IOException {
    return new MyCustomAuditEventRepository(this.config.getLogFile(), mapper,
      this.config.getSupportedEvents());
  }

}

```

Finally, don't forget to set `bankid.audit.repository` to `other` to activate your extension.

<a name="bankid-audit-events"></a>
## BankID Audit Events

This section lists all audit events that are produced by the BankID IdP.

Common properties for all type of event are the following:

- `type` - The type of the audit entry, see below.

- `timestamp` - The time at which the event occurred.

- `principal` - The "owner" of the entry. This will always the SAML entityID of the Service Provider that requested authentication.

- `data` - Auditing data that is specific to the type of audit event. However, the following fields will always be present:

 - `rp` - The name of the Relying Party.

  - `sp-entity-id` - The "owner" of the entry. This will always the SAML entityID of the Service Provider that requested authentication.
  
 - `authn-request-id` - The ID of the authentication request that is being processed (`AuthnRequest`).
 
 - `operation` - The type of operation. Possible values are `auth` and `sign`.

### Received Request

**Type:** `BANKID_RECEIVED_REQUEST`

**Description:** A request for authentication or signing was received. Note that this event is
published before any communication with the BankID server is initiated.

**Additional Parameters:** No additional parameters other than those described above.

### Initiated Operation

**Type:** `BANKID_INIT`

**Description:** The BankID operation has been initiated, i.e., the underlying BankID server has been invoked.

**Additional Parameters:**

| Parameter | Description |
|:--- |:--- |
| `order-ref` | The BankID order reference. |

### Authentication Completed

**Type:** `BANKID_AUTH_COMPLETE`

**Description:** An BankID authentication operation has been successfully completed.

**Additional Parameters:**

| Parameter | Description |
|:--- |:--- |
| `order-ref` | The BankID order reference. |
| `user.personal-number` | The Swedish personal identity number of the user that was authenticated. |
| `user.name` | The full name of the user that was authenticated. |
| `user.device.ip-address` | The IP address of the user's device holding the BankID. |
| `user.device.uhi` | The Unique Hardware Identifier for the user's device holding the BankID. |


### Signing Completed

**Type:** `BANKID_SIGN_COMPLETE`

**Description:** An BankID signature operation has been successfully completed.

**Additional Parameters:**

| Parameter | Description |
|:--- |:--- |
| `order-ref` | The BankID order reference. |
| `user.personal-number` | The Swedish personal identity number of the user that was authenticated. |
| `user.name` | The full name of the user that was authenticated. |
| `user.device.ip-address` | The IP address of the user's device holding the BankID. |
| `user.device.uhi` | The Unique Hardware Identifier for the user's device holding the BankID. |


### Operation cancelled

**Type:** `BANKID_CANCEL`

**Description:** An operation that was started was cancelled by the user.

**Additional Parameters:**

| Parameter | Description |
|:--- |:--- |
| `order-ref` | The BankID order reference. |

### Erroneous Operation

**Type:** `BANKID_ERROR`

**Description:** An error occurred when processing a BankID request.

**Additional Parameters:**

| Parameter | Description |
|:--- |:--- |
| `order-ref` | The BankID order reference (if known). |
| `error-code` | The code for the error. | 
| `error-description` | A textual description of the error (if available). |

-----

<a name="saml-audit-events"></a>
## SAML Audit Events

This section lists all audit events that come from the saml-identity-provider dependency.

For the most up to date information,
visit https://github.com/swedenconnect/saml-identity-provider/blob/main/docs/audit.md

### Common Parameters

All audit events will contain the following fields:

- `type` - The type of the audit entry, see below.

- `timestamp` - The timestamp of when the audit event entry was created.

- `principal` - The "owner" of the entry. This will always the SAML entityID of the Service
  Provider that requested authentication.

- `data` - Auditing data that is specific to the type of audit event. However, the following fields
  will always be present:

    - `sp-entity-id` - The "owner" of the entry. This will always the SAML entityID of the Service Provider that
      requested authentication. If not available, `unknown` is used.

    - `authn-request-id` - The ID of the authentication request that is being processed (`AuthnRequest`). If not
      available, `unknown` is used.

### Authentication Request Received

**Type:** `SAML2_REQUEST_RECEIVED`

**Description:** An event that is created when a SAML `AuthnRequest` has been received. At this point
the IdP has not performed any checks to validate the correctness of the message.

**Audit data**: `authn-request`

| Parameter                  | Description                                                                                         | Type              |
|:---------------------------|:----------------------------------------------------------------------------------------------------|:------------------|
| `id`                       | The ID of the `AuthnRequest`.                                                                       | String            |
| `issuer`                   | The entity that issued the authentication request (SP entityID).                                    | String            |
| `authn-context-class-refs` | The requested Authentication Context Class References, or, the requested Level of Assurance levels. | A list of strings |
| `force-authn`              | Tells whether the SP requires the user to be authenticated.                                         | Boolean           |
| `is-passive`               | Tells whether the SP requires that no user authentication is performed (i.e., requires SSO).        | Boolean           |
| `relay-state`              | The RelayState variable of the request.                                                             | String            |

### Before User Authentication

**Type:** `SAML2_BEFORE_USER_AUTHN`

**Description:** The received authentication request has been successfully validated. No additional
data except for the common fields is included. The data is the same as for `SAML2_REQUEST_RECEIVED`
described above.

### After User Authentication

**Type:** `SAML2_AFTER_USER_AUTHN`

**Description:** The Identity Provider has successfully authenticated the user. This can also be
a re-use of a previously performed authentication (SSO). In those cases this is reflected in the
audit data.

**Audit data**: `user-authentication-info`

| Parameter                 | Description                                                                                                                                                                                                                                                                                                                                                            | Type                                               |
|:--------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------|
| `authn-instant`           | The instant when the user authenticated.                                                                                                                                                                                                                                                                                                                               | String                                             |
| `subject-locality`        | The subject's locality (IP address).                                                                                                                                                                                                                                                                                                                                   | String                                             |
| `authn-context-class-ref` | The URI for the Authentication Context Class (LoA) under which the authentication was made.                                                                                                                                                                                                                                                                            | String                                             |
| `authn-authority`         | Optional identity of an "authenticating authority", used for proxy IdP:s.                                                                                                                                                                                                                                                                                              | String                                             |
| `user-attributes`         | A list of elements listing the user attributes that was issued.<br/>**Note:** This will be a complete list of user attributes as seen be the authenticator. It is not sure that all of them are released in the resulting SAML assertion. This depends on the release policy used.                                                                                     | List of attributes with fields `name` and `value`. |
| `sign-message-displayed`  | If the request was sent by a "signature service" SP this field will indicate whether a "sign message" was displayed for the user or not.                                                                                                                                                                                                                               | Boolean                                            |
| `allowed-to-reuse`        | Tells whether the IdP will allow this particular authentication to be re-used in forthcoming operations (i.e., can it be used for SSO?).                                                                                                                                                                                                                               | Boolean                                            |
| `sso-information`         | If the current authentication was re-used from a previous user authentication (SSO) this field contains the fields `original-requester` and `original-authn-request-id`. These fields identify the requesting entity and the ID of the authentication request when the user authenticated. The `authn-instant` (see above) will in these cases be set to this instant. | SsoInfo                                            |

### Successful SAML Response

**Type:** `SAML2_SUCCESS_RESPONSE`

**Description:** An event that is created before a success SAML response is sent. This means that the
request has been processed, the user authenticated and a SAML assertion created.

**Audit data**: `saml-response`

| Parameter        | Description                                                                                   | Type    |
|:-----------------|:----------------------------------------------------------------------------------------------|:--------|
| `id`             | The ID of the SAML `Response` message.                                                        | String  |
| `in-response-to` | The ID of the `AuthnRequest` message that triggered this operation.                           | String  |
| `status.code`    | The status code of the operation. Will always be `urn:oasis:names:tc:SAML:2.0:status:Success` | String  |
| `issued-at`      | The time of issuance.                                                                         | String  |
| `destination`    | The "destination" of the response message, i.e., the URL to which the message is posted.      | String  |
| `is-signed`      | Tells whether the message is signed.                                                          | Boolean |

**Audit data**: `saml-assertion`

| Parameter                 | Description                                                                                 | Type                                               |
|:--------------------------|:--------------------------------------------------------------------------------------------|:---------------------------------------------------|
| `id`                      | The ID of the SAML `Assertion`.                                                             | String                                             |
| `in-response-to`          | The ID of the `AuthnRequest` message that triggered this operation.                         | String                                             |
| `is-signed`               | Tells whether the assertion is signed.                                                      | Boolean                                            |
| `is-encrypted`            | Tells whether the assertion is encrypted before being included in the response message.     | String                                             |
| `issued-at`               | The issuance time for the assertion.                                                        | String                                             |
| `issuer`                  | The entityID of the issuing entity (IdP).                                                   | String                                             |
| `authn-instant`           | The instant when the user authenticated.                                                    | String                                             |
| `subject-id`              | The `Subject` identity included in the assertion.                                           | String                                             |
| `subject-locality`        | The subject's locality (IP address).                                                        | String                                             |
| `authn-context-class-ref` | The URI for the Authentication Context Class (LoA) under which the authentication was made. | String                                             |
| `authn-authority`         | Optional identity of an "authenticating authority", used for proxy IdP:s.                   | String                                             |
| `attributes`              | A list of elements listing the SAML attributes that was issued.                             | List of attributes with fields `name` and `value`. |

### Error SAML Response

**Type:** `SAML2_AUDIT_ERROR_RESPONSE`

**Description:** An event that is created before an error SAML response is sent. The error can represent
a bad request or that the user authentication failed.

Note: The case when the user has cancelled the operation is represented by setting the
`status.subordinate-code` field to `http://id.elegnamnden.se/status/1.0/cancel`.

**Audit data**: `saml-response`

| Parameter                 | Description                                                                                                  | Type    |
|:--------------------------|:-------------------------------------------------------------------------------------------------------------|:--------|
| `id`                      | The ID of the SAML `Response` message.                                                                       | String  |
| `in-response-to`          | The ID of the `AuthnRequest` message that triggered this operation.                                          | String  |
| `status.code`             | The main status code of the operation (was the error due to an error by the requester or by the responder?). | String  |
| `status.subordinate-code` | The subordinate status code.                                                                                 | String  |
| `status.message`          | Textual error message.                                                                                       | String  |
| `issued-at`               | The time of issuance.                                                                                        | String  |
| `destination`             | The "destination" of the response message, i.e., the URL to which the message is posted.                     | String  |
| `is-signed`               | Tells whether the message is signed.                                                                         | Boolean |

### Unrecoverable Error

**Type:** `SAML2_UNRECOVERABLE_ERROR`

**Description:** If an error occurs during processing of an request and the IdP has no means of posting
a SAML error response back, this error is displayed in the user interface. In these cases this is also audited.

**Audit data**: `unrecoverable-error`

| Parameter       | Description        | Type   |
|:----------------|:-------------------|:-------|
| `error-code`    | The error code.    | String |
| `error-message` | The error message. | String |

---


Copyright &copy;
2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed
under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).