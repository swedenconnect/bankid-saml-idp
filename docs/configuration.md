![Logo](images/sweden-connect.png)

# Configuration of the BankID SAML IdP

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

-----

There are three distinct parts in configuring the BankID SAML IdP:

- Spring Boot configuration where features such as TLS, management ports, session handling, Redis,
logging levels and so on are configured. Read more about this at https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html.

- SAML IdP configuration. This is described in the [Spring Security SAML Identity Provider](https://github.com/swedenconnect/saml-identity-provider) repository.

- BankID configuration. This is the BankID-specific configuration used by the BankID SAML IdP. See below for all possible settings.

Also check the [application.yml](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/resources/application.yml) file for an example of how to configure the service.

<a name="bankid-application-configuration"></a>
## BankID Application Configuration

This section describes all settings that are specific for the BankID IdP-application. These settings
comprise of configuration for the BankID integration and integration against the [IdP-base](https://github.com/swedenconnect/saml-identity-provider).

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `bankid.service-url` | The URL to the BankID API. | `String` | `https://appapi2.bankid.com/rp/v6.0` if `bankid.test-mode` is `false` (i.e., production setup), and `https://appapi2.test.bankid.com/rp/v6.0` if `bankid.test-mode` is `true` (BankID test environment). |
| `bankid.`<br />`server-root-certificate` | The root certificate of the BankID server TLS credential. | A `Resource` pointing at an X.509 certificate. | `classpath:trust/bankid-trust-prod.crt` if `bankid.test-mode` is `false` (production setup) and `classpath:trust/bankid-trust-test.crt` if `bankid.test-mode` is `true` (BankID test environment). |
| `bankid.test-mode` | Should be set to `true` if the BankID IdP is running in "test mode", i.e., if the test BankID RP API is used. | `Boolean` | `false` |
| `bankid.`<br />`built-in-frontend` | Whether we are using a built-in frontend, i.e., if we are using the built in Vue frontend app, this controller redirects calls made from the underlying SAML IdP library to our frontend start page. | `Boolean` | `true` |
| `bankid.authn.*` | IdP Authentication configuration. See [Authentication Configuration](#authentication-configuration) below. | [IdpConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | - |
| `bankid.qr-code.*` | See [QR Code Generation Configuration](#qr-code-generation-configuration) below. | [QrCodeConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | See defaults [below](#qr-code-generation-configuration) |
| `bankid.health.*` | Configuration for the Spring Boot actuator Health-endpoint. See [Health Configuration](#health-configuration) below. | [HealthConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | See defaults [below](#health-configuration) |
| `bankid.audit.*` | Audit logging configuration, see [Audit Logging Configuration](#audit-logging-configuration) below. | [AuditConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | See defaults [below](#audit-logging-configuration) |
| `bankid.`<br />`user-message-defaults.*` | Configuration for default text(s) to display during authentication/signing. See [Default User Messages Configuration](#default-user-messages-configuration) below. | [UserMessageConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | - |
| `bankid.`<br />`relying-parties[].*` | A list of configuration elements for each Relying Party that is allowed to communicate with the BankID SAML IdP. See [Relying Party Configuration](#relying-party-configuration) below. | [RelyingPartyConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | - |


<a name="authentication-configuration"></a>
### Authentication Configuration

This section contains the necessary configuration for injecting the BankID authentication as part
of the [Spring Security SAML Identity Provider](https://github.com/swedenconnect/saml-identity-provider).

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `provider-name` | The name of the Spring authentication provider that is being used for BankID operations. This name is only used for logging and should not have to be changed. | String | `BankID` |
| `authn-path` | The authentication path. Where the Spring Security flow directs the user for authentication by our implementation. Unless the BankID IdP is customized with advanced overrides for MVC handling this setting should not be assigned. | String | `/bankid` |
| `resume-path` | The resume path. Where the BankID IdP application redirects back the user after that we are done. Unless the BankID IdP is customized with advanced overrides for MVC handling this setting should not be assigned. | String | `/resume` |
| `supported-loas[]` | Contains a list of the Authentication Context Class Ref URI:s (Level of assurance URI:s) that are supported by this IdP.<br /><br />**Note:** For the BankID IdP it is recommended that only one LoA is supported. | List of strings | `[ "http://id.swedenconnect.se/`<br />`loa/1.0/uncertified-loa3" ]`<sup>1</sup> |
| `entity-categories[]` | A list of the SAML entity categories that this IdP supports/declares. Read more about entity categories at [Entity Categories for the Swedish eID Framework](https://docs.swedenconnect.se/technical-framework/latest/06_-_Entity_Categories_for_the_Swedish_eID_Framework.html). | List of strings | - |

> **\[1\]:** BankID as a eID provider is certified according to LoA 3 (tillitsnivå 3), but unless
the actual BankID IdP has been certified according to LoA 3 the "uncertified-loa3" URI should be used. Read 
more at https://www.digg.se/digitala-tjanster/e-legitimering/e-legitimering-for-dig-som-leverantor/idp-leverantor. If your IdP has been audited and certified according to LoA 3, the URI `http://id.elegnamnden.se/loa/1.0/loa3` should be used.

<a name="qr-code-generation-configuration"></a>
### QR Code Generation Configuration

Configuration for how to generate QR codes.

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `size` | The size in pixels (height and width) for the generated QR codes. | Integer | `300` |
| `image-format` | The image format for the generated QR code. Possible values are: `JPG` and `PNG`. <br /><br />**Note:** `SVG` is also a valid value, but currently not supported by the underlying QR code generator. | String | `PNG` |

<a name="health-configuration"></a>
### Health Configuration

Configuration for health endpoints.

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `rp-certificate-warn-threshold` | A threshold value for when the health endpoint should issue warnings telling the a configured BankID Relying Party certificate is about to expire. | Duration | 14 days |

For more details about health- and other monitoring endpoints, see [Monitoring the BankID IdP Application](monitoring.html).

<a name="audit-logging-configuration"></a>
### Audit Logging Configuration

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `repository` | Tells how the produced audit log entries should be stored. Possible values are: `memory` for an in-memory repository, `redislist` for a Redis list implementation, `redistimeseries` for a Redis time series implementation or `other` if you extend the BankID IdP with your own audit event repository implementation. See the [Audit Event Logging](https://docs.swedenconnect.se/bankid-saml-idp/logging.html) page for details concerning the configuration and customization of audit event logging. | String | `memory` |
| `log-file` | If assigned, the audit events will not only be stored according to the `repository` setting, but also be written to the given log file. If set, a complete path must be given. | String | - |
| `supported-events[]` | The supported events that will be logged to the given repository (and possibly the file). | List of strings | All events listed in [BankID Audit Events](https://docs.swedenconnect.se/bankid-saml-idp/logging.html#bankid-audit-events) and [SAML Audit Events](https://docs.swedenconnect.se/bankid-saml-idp/logging.html#saml-audit-events). |

<a name="default-user-messages-configuration"></a>
### Default User Messages Configuration

Each Relying Party can be configured with both login texts to use during login, and also fallback texts
to use if no sign message was received in the SAML request. However, an IdP supporting many RP:s may 
find it useful to declare default texts to use if no specific configuration exists for a particular
RP.

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `login-text.*` | The text to display in the BankID app when the user is authenticating. See [Relying Party Configuration](#relying-party-configuration) below for how to set a text that is specific for a specific RP. | `DisplayText` (see below) | - |
| `fallback-sign-text.*` | If no `SignMessage` was received in the SAML `AuthnRequest` message, and no specific text is set for an RP (see [Relying Party Configuration](#relying-party-configuration) below), this text will be displayed in the BankID app during a BankID signature operation. | `DisplayText` (see below) | - |

##### DisplayText

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `text` | The text string. | String | - |
| `format` | The format on the above text string. Can be either `plain_text` or `simple_markdown_v1` (see https://www.bankid.com/utvecklare/guider/formatera-text) | String | `plain_text` |

##### Example

```
bankid:
  ...
  user-message-defaults:
    fallback-sign-text:
      text: "I hereby sign the text that was displayed on the previous page."
      format: plain-text
    login-text:
      text: "*Note!*\n\nNever login using your BankID when someone is asking you to do this over the phone"
      format: simple-markdown-v1
      
```

<a name="relying-party-configuration"></a>
### Relying Party Configuration

Configuration for a BankID Relying Party. A BankID Relying Party can serve any number of SAML SP:s. Usually they are from the same organization. An organization that wants to use BankID both for logging
in users and having users signing using a signature services will have at least two SAML SP:s connected,
the ordinary SAML SP used for authentication and the Signature Service SP used for signing.

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `id` | The system unique ID for the Relying Party. This is will be visible in audit logs (in the `rp` field, see [Audit Event Logging](logging.html)). | String | - |
| `entity-ids[]` | A list of SAML entityID:s that belongs to this Relying Party.<br /><br />If the IdP is in test mode (`bankid.test-mode=true`) this list may be empty, meaning that all SP:s are served. In a "real" setup, this field must be assigned with at least one entityID. | List of strings | - |
| `credential.*` | The BankID relying party credential. See [PkiCredential](https://github.com/swedenconnect/credentials-support#generic-pkicredentialfactorybean-for-springboot-users). | [PkiCredentialConfigurationProperties](https://github.com/swedenconnect/credentials-support/blob/main/src/main/java/se/swedenconnect/security/credential/factory/PkiCredentialConfigurationProperties.java) | - |
| `user-message.*` | Relying Party specific display text for authentication (and signature). Overrides the default text described in the [Default User Messages Configuration](#default-user-messages-configuration) section above. See [Relying Party User Message](#relying-party-user-message) below. | [RpUserMessage](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | See below | 
| `ui-info.*` | The UI info (display name and logotype URL) for a Relying Party is normally extracted from the SAML metadata, but there are cases where you may want to manually configure these data elements (for example if the metadata does not contain this information, or you simply want to override it). This element holds this information. See [Relying Party UI Info](#relying-party-ui-info) below. | [RelyingPartyUiInfo](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/rp/RelyingPartyUiInfo.java) | - |
| `bankid-requirements.*` | Specific BankID requirements for this Relying Party. See [BankID Requirements](#bankid-requirements) below. | [BankIdRequirement](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdRequirement.java) | See below |

<a name="relying-party-user-message"></a>
##### Relying Party User Message

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `text` | The text string. | String | - |
| `format` | The format on the above text string. Can be either `plain_text` or `simple_markdown_v1` (see https://www.bankid.com/utvecklare/guider/formatera-text) | String | `plain_text` |
| `inherit-default-login-text` | If the default user message login text has been assigned, and a specific RP wishes to not use login messages it should set this flag to `false` (and not assign `login-text`). | Boolean | `true` |

<a name="relying-party-ui-info"></a>
##### Relying Party UI Info

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `display-names` | A mapping between language codes and display names. | Map | - |
| `logotype-url` | The URL for the Relying Party logotype. | String | - |
| `use-as-fallback` | Whether the data in this object should be used as a fallback to UI information gathered from the SAML metadata or not. If `true`, the data will only be used if data is not present in SAML metadata, and if `false`, the data from this object will have precedence over data found in SAML metadata. | Boolean | `true` |

Also see [RelyingPartyUiInfo](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/rp/RelyingPartyUiInfo.java).

<a name="bankid-requirements"></a>
##### BankID Requirements

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `pin-code-auth` | Tells whether users are required to use their PIN code during BankID authentication, even if they have biometrics activated. | Boolean | `false` |
| `pin-code-sign` | Tells whether users are required to use their PIN code during BankID signing, even if they have biometrics activated. | Boolean | `true` |
| `mrtd` | If `true`, the client needs to provide MRTD (Machine readable travel document) information to complete a BankID operation. Only Swedish passports and national ID cards are supported. | Boolean | `false` |
| `certificate-policies[]` | Object identifiers for which policies that should be used. See [BankID integration guide](https://www.bankid.com/utvecklare/guider/teknisk-integrationsguide/graenssnittsbeskrivning/auth) | List of strings | - |
| `card-reader` | Requirement for which type of smart card reader that is required (for BankID on card). See [BankID integration guide](https://www.bankid.com/utvecklare/guider/teknisk-integrationsguide/graenssnittsbeskrivning/auth). | String | - |

Also see [BankIdRequirement](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdRequirement.java).

<a name="saml-idp-configuration"></a>
## SAML IdP Configuration

> TODO

<a name="spring-boot-configuration"></a>
## Spring Boot Configuration

<a name="redis-configuration"></a>
### Redis Configuration

> TODO

# Configuring session / lock management

## Module Selection

### Redis (recommended)

We have configured a customizer that extends the spring redis configuration to simplify TLS configuration

Application.yml
```yaml
session:
  module: redis # Select module
spring:
  redis:
    host: host for redis instance
    port: port for redis instance
    password: password for redis instance
    ssl: true 
    tls: # Configuration Extension
      p12KeyStorePath: path for keystore
      p12KeyStorePassword: password for keystore
      p12TrustStorePath: path for truststore
      p12TrustStorePassword: password for truststore
      enableHostnameVerification: true # If you want to verify certificate hostname or not
```
### In memory (Not recommended for production)
```yaml
session:
  module: memory
```

### Implement your own module

You can implement your own module, if you want to be able to use something else than redis, e.g. psql, mysql

To implement your own module, please see how we have configured the redis module in RedisSessionConfiguration.
The main takeaways is that you need to have an implementation for the following
- LockRepository
- SessionReader (you can use fallback implementation for spring session, but a direct read/write implementation is recommended)
- SessionWriter (you can use fallback implementation for spring session, but a direct read/write implementation is recommended)
- Spring Session Configuration

-----

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
