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
| `bankid.health.*` | Configuration for the Spring Boot actuator Health-endpoint. See [Health Configuration](#health-configuration) below. | [HealthConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | See defaults [below](#health-configuration) | [UserMessageConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java)
| `bankid.audit.*` | Audit logging configuration, see [Audit Logging Configuration](#audit-logging-configuration) below. | [AuditConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | See defaults [below](#audit-logging-configuration) |
| `bankid.`<br />`user-message-defaults.*` | Configuration for default text(s) to display during authentication/signing. See [Default User Messages Configuration](#default-user-messages-configuration) below. | [UserMessageConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | - |
| `bankid.`<br />`relying-parties[].*` | A list of configuration elements for each Relying Party that is allowed to communicate with the BankID SAML IdP. See [Relying Party Configuration](#relying-party-configuration) below. | [RelyingPartyConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/config/BankIdConfigurationProperties.java) | - |

> **\[1\]:** The URL for BankID in test is `https://appapi2.test.bankid.com/rp/v6.0`.

> **\[2\]:** The root certificate of the BankID server TLS credential in test is available at `classpath:trust/bankid-trust-test.crt`.

<a name="authentication-configuration"></a>
### Authentication Configuration

This section contains the necessary configuration for injecting the BankID authentication as part
of the [Spring Security SAML Identity Provider](https://github.com/swedenconnect/saml-identity-provider).

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |

Configuration for how to generate QR codes.

<a name="qr-code-generation-configuration"></a>
### QR Code Generation Configuration

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |

<a name="health-configuration"></a>
### Health Configuration

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |

<a name="audit-logging-configuration"></a>
### Audit Logging Configuration

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `repository` | Tells how the produced audit log entries should be stored. Possible values are: `memory` for an in-memory repository, `redislist` for a Redis list implementation, `redistimeseries` for a Redis time series implementation or `other` if you extend the BankID IdP with your own audit event repository implementation. See the [Audit Event Logging](https://docs.swedenconnect.se/bankid-saml-idp/logging.html) page for details concerning the configuration and customization of audit event logging. | String | `memory` |
| `log-file` | If assigned, the audit events will not only be stored according to the `repository` setting, but also be written to the given log file. If set, a complete path must be given. | String | - |
| `supported-events` | The supported events that will be logged to the given repository (and possibly the file). | List of strings | All events listed in [BankID Audit Events](https://docs.swedenconnect.se/bankid-saml-idp/logging.html#bankid-audit-events) and [SAML Audit Events](https://docs.swedenconnect.se/bankid-saml-idp/logging.html#saml-audit-events). |

<a name="default-user-messages-configuration"></a>
### Default User Messages Configuration

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |

<a name="relying-party-configuration"></a>
### Relying Party Configuration

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |

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
