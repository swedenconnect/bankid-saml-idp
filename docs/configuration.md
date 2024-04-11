![Logo](images/sweden-connect.png)

# Configuration of the BankID SAML IdP

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.swedenconnect.bankid/bankid-idp/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.swedenconnect.bankid/bankid-idp)

-----

There are three distinct parts in configuring the BankID SAML IdP:

- Spring Boot configuration where features such as TLS, management ports, session handling, Redis,
logging levels and so on are configured. Read more about this at [https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html).

- SAML IdP configuration. This is described in the [Spring Security SAML Identity Provider](https://github.com/swedenconnect/saml-identity-provider) repository.

- BankID configuration. This is the BankID-specific configuration used by the BankID SAML IdP. See below for all possible settings.

Also check the [application.yml](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/resources/application.yml) or [sandbox.yml](https://github.com/swedenconnect/bankid-saml-idp/tree/main/samples/sandbox/config) files for an examples of how to configure the service.

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
| `bankid.start-retry-duration`| Duration from initial request to allow restart of the BankID session.<br /><br />In practice this setting has effect on the time the user has to scan a QR-code, or to start his or her app.<br /><br />The BankID session will enter the state "startFailed" if no client application connects within 30 seconds. If the current time is between start and start + startRetryDuration the application will silently start a new session. If the current time is outside this duration the user will be presented with an error. The duration will only be checked on startFailed i.e. every 30 seconds. If you want to disable silent retries set the duration to something lower than 30 seconds, e.g., 0 seconds.  | Duration | 3 minutes |
| `bankid.authn.*` | IdP Authentication configuration. See [Authentication Configuration](#authentication-configuration) below. | [IdpConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | - |
| `bankid.health.*` | Configuration for the Spring Boot actuator Health-endpoint. See [Health Configuration](#health-configuration) below. | [HealthConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | See defaults [below](#health-configuration) |
| ~~`bankid.session.module`~~ | Configuration for which session module that should be active. Supported values are `memory` and `redis`. Set to other value if you extend the BankID IdP with your own session handling (see [Writing Your Own Session Handling Module](override.html#writing-your-own-session-handling-module)).<br /><br />Deprecated. Use `saml.idp.session.module` instead. | String | `memory` |
| ~~`bankid.audit.*`~~ | Audit logging configuration.<br /><br />Deprecated. Instead use the `saml.idp.audit.` settings. See the [Audit Configuration](https://docs.swedenconnect.se/saml-identity-provider/configuration.html#audit-configuration) for the SAML IdP library. | ~~[AuditConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java).~~ | - |
| `bankid.ui.*` | Configuration concerning the BankID IdP UI (including texts displayed in the BankID app). See [UI Configuration](#ui-configuration) below. | [UiProperties](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/config/UiProperties.java) | See defaults [below](#ui-configuration) |
| `bankid.`<br />`relying-parties[].*` | A list of configuration elements for each Relying Party that is allowed to communicate with the BankID SAML IdP. See [Relying Party Configuration](#relying-party-configuration) below. | [RelyingPartyConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | - |


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
the actual BankID IdP has been certified according to LoA 3 the "uncertified-loa3" URI should be used. Read more at [https://www.digg.se/digitala-tjanster/e-legitimering/e-legitimering-for-dig-som-leverantor/idp-leverantor](https://www.digg.se/digitala-tjanster/e-legitimering/e-legitimering-for-dig-som-leverantor/idp-leverantor). If your IdP has been audited and certified according to LoA 3, the URI `http://id.elegnamnden.se/loa/1.0/loa3` should be used.

<a name="health-configuration"></a>
### Health Configuration

Configuration for health endpoints.

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `rp-certificate-warn-threshold` | A threshold value for when the health endpoint should issue warnings telling the a configured BankID Relying Party certificate is about to expire. | Duration | 14 days |

For more details about health- and other monitoring endpoints, see [Monitoring the BankID IdP Application](monitoring.html).

<a name="audit-logging-configuration"></a>
### Audit Logging Configuration

**Deprecated**. Instead use the `saml.idp.audit.` settings. See the [Audit Configuration](https://docs.swedenconnect.se/saml-identity-provider/configuration.html#audit-configuration) for the SAML IdP library.

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `repository` | Tells how the produced audit log entries should be stored. Possible values are: `memory` for an in-memory repository, `redislist` for a Redis list implementation, `redistimeseries` for a Redis time series implementation or `other` if you extend the BankID IdP with your own audit event repository implementation. See the [Audit Event Logging](https://docs.swedenconnect.se/bankid-saml-idp/logging.html) page for details concerning the configuration and customization of audit event logging. | String | `memory` |
| `log-file` | If assigned, the audit events will not only be stored according to the `repository` setting, but also be written to the given log file. If set, a complete path must be given. | String | - |
| `supported-events[]` | The supported events that will be logged to the given repository (and possibly the file). | List of strings | All events listed in [BankID Audit Events](https://docs.swedenconnect.se/bankid-saml-idp/logging.html#bankid-audit-events) and [SAML Audit Events](https://docs.swedenconnect.se/bankid-saml-idp/logging.html#saml-audit-events). |

<a name="ui-configuration"></a>
### UI Configuration

| Property | Description | Type  | Default value |
| :--- | :--- | :--- | :--- |
| `user-message-defaults.*`  | Configuration for default text(s) to display during authentication/signing. See [Default User Messages Configuration](#default-user-messages-configuration) below. | [UserMessageProperties](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/config/UiProperties.java) | - |
| `provider.svg-logotype` | The icon/logotype to be displayed in UI footer. This logotype should be the logotype for the provider of the service (as opposed for the logotype displayed in the left upper corner which is the logotype for the calling SP). The logotype must be in SVG format. If no logotype is assigned, the UI footer will hold no logotype. | [Resource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/Resource.html) | - |
| `provider.svg-favicon` | The favicon to be displayed in the browser tab, etc. This favicon must be in SVG format. SVG favicons are supported in Firefox and Chromium-based browsers. If no favicon is assigned, the BankID logo will be used. | [Resource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/Resource.html) | - |
| `provider.png-favicon` | The favicon to be displayed in the browser tab, etc. This favicon must be in PNG format and should be 32 x 32 pixels in dimension. PNG favicons are supported by all modern browsers, including Internet Explorer 11. If no favicon is assigned, the BankID logo will be used. | [Resource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/Resource.html) | - |
| `provider.name.*` | The name for the provider as a map where the keys are language codes and the values the name in respective language. This name will primarily be used at the bottom of the page in the copyright statement, but may (later) by used in other UI places as well. If no value is set, no copyright statement is displayed. | Map of strings | - |
| `qr-code.*` | See [QR Code Configuration](#qr-code-configuration) below. | [QrCodeConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/config/UiProperties.java) | See defaults [below](#qr-code-configuration) |
| `show-sp-message` | Enables an extra informational message in the UI about which SP that ordered authentication/signature. The SP display name will be read from the SAML metadata (can be overridden in RP configuration). | Boolean | `false` |
| `accessibility-report-link` | Swedish public e-services are required to include a link to the "accessibility report" (tillgänglighetsrapport) of their web site. By assigning this setting with a link, this link will be included in the device selection view of the UI. | String | - |
| `user-error.*` | UI properties for how to display errors for the user. See [User Error Configuration](#user-error-configuration) below. | [UserErrorProperties](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/config/UiProperties.java) | See [below](#user-error-configuration) |
| `override.directory-path` | Optional path where CSS, message and content override files can be put. See [Customizing the BankID IdP UI](https://docs.swedenconnect.se/bankid-saml-idp/override.html#customizing-the-bankid-idp-ui). | String | - |

**Note:** A BankID that is "generic", meaning that it serves Service Providers from different
organizations,  should enable the `show-sp-message` setting to provide textual information about the
Service Provider that requested authentication/signing.

<a name="default-user-messages-configuration"></a>
#### Default User Messages Configuration

Each Relying Party can be configured with both login texts to use during login, and also fallback texts
to use if no sign message was received in the SAML request. However, an IdP supporting many RP:s may 
find it useful to declare default texts to use if no specific configuration exists for a particular
RP.

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `login-text.*` | The text to display in the BankID app when the user is authenticating. See [Relying Party Configuration](#relying-party-configuration) below for how to set a text that is specific for a specific RP. | [DisplayText](#display-text) | - |
| `fallback-sign-text.*` | If no `SignMessage` was received in the SAML `AuthnRequest` message, and no specific text is set for an RP (see [Relying Party Configuration](#relying-party-configuration) below), this text will be displayed in the BankID app during a BankID signature operation. | [DisplayText](#display-text) | - |

<a name="display-text"></a>
##### Display Text

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `text` | The text string. | String | - |
| `format` | The format on the above text string. Can be either `plain_text` or `simple_markdown_v1` (see https://www.bankid.com/utvecklare/guider/formatera-text) | String | `plain_text` |

<a name="qr-code-configuration"></a>
#### QR Code Configuration

Configuration for how to generate and display QR codes.

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `size` | The size in pixels (height and width) for the generated and displayed QR codes. | Integer | `200` |
| `image-format` | The image format for the generated QR code. Possible values are: `JPG` and `PNG`. <br /><br />**Note:** `SVG` is also a valid value, but currently not supported by the underlying QR code generator. | String | `PNG` |
| `display-qr-help` | Tells whether we should display an intermediate view before displaying the QR-code. This page/view will contain extra help texts to assist the user in understanding the steps for scanning the QR code. | Boolean | `false` |


<a name="user-error-configuration"></a>
#### User Error Configuration

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `contact-email` | E-mail address to use in the UI. For example to report errors. | String | - |
| `show-contact-information` | Predicate that tells whether contact information should be displayed in the UI. | Boolean | `false` |
| `show-trace-id` | Whether an ID should be displayed for the user when an error has occurred. Using this ID, the user can contact user support. | Boolean | `false` |

#### Example

```yaml
bankid:
  ...
  ui:
    user-message-defaults:
      fallback-sign-text:
        text: "I hereby sign the text that was displayed on the previous page."
        format: plain_text
      login-text:
        text: "*Note!*\n\nNever login using your BankID when someone is asking you to do this over the phone"
        format: simple-markdown-v1
    user-error:
      contact-email: support@example.com
      show-contact-information: true
      show-trace-id: true
      
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
| `user-message.*` | Relying Party specific display text for authentication (and signature). Overrides the default text described in the [Default User Messages Configuration](#default-user-messages-configuration) section above. See [Relying Party User Message](#relying-party-user-message) below. | [RpUserMessage](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | See below | 
| `ui-info.*` | The UI info (display name and logotype URL) for a Relying Party is normally extracted from the SAML metadata, but there are cases where you may want to manually configure these data elements (for example if the metadata does not contain this information, or you simply want to override it). This element holds this information. See [Relying Party UI Info](#relying-party-ui-info) below. | [RelyingPartyUiInfo](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/rp/RelyingPartyUiInfo.java) | - |
| `bankid-requirements.*` | Specific BankID requirements for this Relying Party. See [BankID Requirements](#bankid-requirements) below. | [BankIdRequirement](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/config/BankIdRequirement.java) | See below |

<a name="relying-party-user-message"></a>
##### Relying Party User Message

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `login-text.*` | RP specific text to display when authenticating. See [Display Text](#display-text). | [DisplayText](#display-text) | - |
| `inherit-default-login-text` | If the default user message login text has been assigned, and a specific RP wishes to not use login messages it should set this flag to `false` (and not assign `login-text` above). | Boolean | `true` |
| `fallback-sign-text.*` | RP specific text to display when signing (if no SignMessage extension was received in the AuthnRequest message). | [DisplayText](#display-text) | - |

<a name="relying-party-ui-info"></a>
##### Relying Party UI Info

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `display-name` | A mapping between language codes and display names. | Map | - |
| `logotype-url` | The URL for the Relying Party logotype. | String | - |
| `use-as-fallback` | Whether the data in this object should be used as a fallback to UI information gathered from the SAML metadata or not. If `true`, the data will only be used if data is not present in SAML metadata, and if `false`, the data from this object will have precedence over data found in SAML metadata. | Boolean | `true` |

Also see [RelyingPartyUiInfo](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/rp/RelyingPartyUiInfo.java).

<a name="bankid-requirements"></a>
##### BankID Requirements

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `pin-code-auth` | Tells whether users are required to use their PIN code during BankID authentication, even if they have biometrics activated. | Boolean | `false` |
| `pin-code-sign` | Tells whether users are required to use their PIN code during BankID signing, even if they have biometrics activated. | Boolean | `true` |
| `mrtd` | If `true`, the client needs to provide MRTD (Machine readable travel document) information to complete a BankID operation. Only Swedish passports and national ID cards are supported. | Boolean | `false` |
| `certificate-policies[]` | Object identifiers for which policies that should be used. See [BankID integration guide](https://www.bankid.com/utvecklare/guider/teknisk-integrationsguide/graenssnittsbeskrivning/auth) | List of strings | - |
| `card-reader` | Requirement for which type of smart card reader that is required (for BankID on card). See [BankID integration guide](https://www.bankid.com/utvecklare/guider/teknisk-integrationsguide/graenssnittsbeskrivning/auth). | String | - |

Also see [BankIdRequirement](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/config/BankIdRequirement.java).

<a name="configuring-a-http-proxy"></a>
## Configuring a HTTP Proxy

The BankID IdP uses the system settings for HTTP proxies. Therefore there are no specific configuration
for HTTP proxies. 

If you require a HTTP Proxy follow the steps in [Java Networking and Proxies](https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html).

<a name="saml-idp-configuration"></a>
## SAML IdP Configuration

The BankID SAML IdP is built upon the [Spring Security SAML Identity Provider](https://github.com/swedenconnect/saml-identity-provider) module. This module is a Sweden Connect Open Source module for
a generic SAML IdP that is compatible with the [Sweden Connect eID Framework](https://docs.swedenconnect.se/technical-framework/).

All Spring configuration settings for this module are documented [here](https://docs.swedenconnect.se/saml-identity-provider/configuration.html).

The [application.yml](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/resources/application.yml) contains sensible defaults and you basically need to change
the following:

- `saml.idp.entity-id` - The SAML entityID of your IdP.

- `saml.idp.credentials.*` - Your IdP credentials (keys and certificates).

- `saml.idp.metadata-providers.*` - Where to download federation metadata.

- `saml.idp.metadata.*` - SAML metadata for your IdP.

The BankID IdP also extends this configuration with the following setting:

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `saml.idp.replay-ttl` | The time-to-live for items handled by the [MessageReplayChecker](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/response/replay/MessageReplayChecker.java) | Duration | 5 minutes |

<a name="spring-boot-configuration"></a>
## Spring Boot Configuration

The BankID SAML IdP is a Spring Boot application, and apart from the above described configuration you
need to supply Spring Boot configuration. See [Spring Boot Common Application Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html) for a listing
of available settings.

Also check the [application.yml](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/resources/application.yml) file for an example of how to configure the service.

<a name="management-and-supervision"></a>
### Management and Supervision

In order for the health-endpoint and any other Spring Boot Actuator endpoints to function, the 
[Spring Boot Actuator](https://www.baeldung.com/spring-boot-actuators) needs to be configured.

Recommended settings are:

```yml
management:
  server:
    port: 8444
  endpoint:
    health:
      status:
        order:
        - DOWN
        - OUT_OF_SERVICE
        - UP
        - WARNING
        - UNKNOWN
        http-mapping:
          WARNING: 503
      show-details: always
    info:
      enabled: true
  endpoints:
    web:
      exposure:
        include: health, metrics, prometheus, loggers
```

**Note:** In order to get a "Quarkus-style" for the endpoints, it is possible to re-configure the
base path for the management server. Each endpoint may also be re-named. In the example below
we use `/q` instead of `/actuator` to fit Quarkus needs. We also change the endpoint name for
Spring's `metrics`-endpoint since it clashes with Quarkus' that uses `metrics` for Prometheus. 

```yml
management:
  server:
    port: 8444
  ...
  endpoints:
    web:
      base-path: /q
      path-mapping:
        metrics: springmetrics
        prometheus: metrics
```

The health-endpoint is now exposed at `https://<your-domain>:8444/q/health`.

<a name="tomcat-configuration-extension"></a>
### Tomcat Configuration Extension

Most of Tomcat's behaviour can be configured using [Spring Boot's configuration properties](https://www.baeldung.com/spring-boot-configure-tomcat), but there is no way of setting up the Tomcat AJP
protocol using Spring Boot's settings. Therefore, we add the following configuration properties:

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `tomcat.ajp.enabled` | Is the Tomcat AJP protocol enabled? | Boolean | `false` |
| `tomcat.ajp.port` | The Tomcat AJP port. | Integer | `8009` |
| `tomcat.ajp.secret` | The Tomcat AJP secret/password. | String | - |
| `tomcat.ajp.secret-required` | Is AJP secret required? | Boolean | `false` |

> Needless to say. The above settings are only relevant if you use the Tomcat AJP protocol for your service.

<a name="redis-configuration"></a>
### Redis Configuration

If `bankid.session.module` is set to `redis` Redis will be used for session management. You will then
have to configure Redis further using the Spring Boot Redis configuration.

Good resources for how to configure Redis under Spring Boot are:

- [https://www.baeldung.com/spring-data-redis-properties](https://www.baeldung.com/spring-data-redis-properties)

- [https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)

- [https://github.com/redisson/redisson/wiki/2.-Configuration](https://github.com/redisson/redisson/wiki/2.-Configuration)

Example:
```yml
spring:
  config:
    activate:
      on-profile: local
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: supersecret
      ssl: 
        enabled: true
      ssl-ext:
        <see below>
```

<a name="redis-ssltls-configuration-extension"></a>
#### Redis SSL/TLS Configuration Extension

Spring Boot 3.1 introduced the concept of **SslBundles**, where the SSL credentials are configured separately and referred to as a bundle. See the article [Securing Spring Boot Applications With SSL](https://spring.io/blog/2023/06/07/securing-spring-boot-applications-with-ssl) for a good overview. 

The Redis support in Spring offers possibilities to use bundles when configuring TLS for Redis connections. This is the recommended way of configuring TLS for Redis.

**Example:**

```yaml
spring:
  ...
  ssl:
    bundle:
      jks:
        redis-tls:
          key:
            alias: 1
            password: changeit
          keystore:
            location: classpath:redis.p12
            password: changeit
            type: pkcs12
          truststore:
            location: classpath:trust.p12
            password: changeit
            type: pkcs12
  ...
  data:
    redis:
      host: redis.example.com
      port: 6379
      password: supersecret
      ssl:
        enabled: true
        bundle: redis
      ssl-ext:
        enable-hostname-verification: true        

```

See the [Redis Configuration](https://docs.swedenconnect.se/saml-identity-provider/configuration.html#redis-configuration) in the Spring Security SAML Identity Provider for details.

The previous configuration settings for how to set up TLS for connections against the Redis server is deprecated. The only extension still remaining is:

- `spring.data.redis.ssl-ext.enable-hostname-verification` - Should we verify the the peer's hostname as part of the SSL/TLS handshake? The default is `true`.

The following settings under the key `spring.data.redis.ssl-ext` are deprecated and will be removed in future releases:

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| ~~`credential.resource`~~ | ~~The path to the `KeyStore` holding the client TLS credential (private key and certificate).~~ | ~~[Resource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/Resource.html)~~ | - |
| ~~`credential.password`~~ | ~~The password to unlock the above `KeyStore`. <br />**Note:** Due to Spring's poor handling of SSL/TLS in general there is no way of having separate passwords for the store itself and the key entries of the `KeyStore`.~~ | ~~String~~ | - |
| ~~`trust.resource`~~ | ~~The path to the `KeyStore` holding the trusted certificates for verifying the server certificate in the SSL/TLS handshake. If not assigned, the system defaults are used.~~ | ~~[Resource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/Resource.html)~~ | - |
| ~~`trust.password`~~ | ~~The password to unlock the trust `KeyStore`. If this `KeyStore` does not have a password, no setting should be supplied.~~ | - |

*The settings will be active if `spring.redis.data.ssl.enabled` is set to `true`.*

<a name="redis-cluster-configuration"></a>
#### Redis Cluster Configuration

See the [Redis Configuration](https://docs.swedenconnect.se/saml-identity-provider/configuration.html#redis-configuration) in the Spring Security SAML Identity Provider for details
on how to configure Redis Clusters NAT translation for addresses.

<a name="adding-your-own-application-yml-file"></a>
### Adding Your Own application.yml File

To add multiple overrides for configuration properties at the same time you can do so by supplying your own application.yml file.

This file will override the base application.yml (both will be loaded).

To load an external file simply supply the application with the following environment variable
```shell
SPRING_CONFIG_IMPORT=/path/to/your/application.yml
```

<a name="logging-configuration"></a>
### Logging Configuration

See [Spring Boot Logging](https://docs.spring.io/spring-boot/docs/2.1.8.RELEASE/reference/html/howto-logging.html) for general configuration about Spring Boot Logging.

Also, the BankID IdP extends these settings with the following:

To enable JSON logs, run the application with the `jsonlog` profile.

Configure this either by settings in the application.yml or by setting environment variable.

**application.yml:**

```yml
spring:
  profiles:
    active: jsonlog
```

**Environment variable:** `SPRING_PROFILES_ACTIVE=jsonlog, ...`

-----

Copyright &copy; 2023-2024, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
