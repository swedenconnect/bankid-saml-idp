![Logo](docs/images/sweden-connect.png)

# SAML Identity Provider for BankID

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This repository comprises of a SAML Identity Provider (IdP) for BankID. 

-----

> NOTE: Development of the SAML IdP for BankID is work in progress. Feel free to follow the progress. Use the [Issues](https://github.com/swedenconnect/bankid-saml-idp/issues)-section for any questions or suggestions.

## About

This repository comprises of a SAML Identity Provider (IdP) for BankID. The IdP is built according
to the [Swedish eID Framework](https://docs.swedenconnect.se/technical-framework/) and may be
used within the [Sweden Connect Federation](https://www.swedenconnect.se).

The BankID IdP uses the [SAML IdP Spring Boot starter](https://github.com/swedenconnect/saml-identity-provider) project, so most of the SAML-specific code resides in that repository.

The repository also contains a Java library implementing the [BankID Relying Party API](https://www.bankid.com/utvecklare/guider/teknisk-integrationsguide).

## Building

> TODO

## Configuring the IdP

There are three distinct parts in configuring the BankID SAML IdP:

- Spring Boot configuration where features such as TLS, management ports, session handling, Redis,
logging levels and so on are configured. Read more about this at https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html.

- SAML IdP configuration. This is described in the [Spring Security SAML Identity Provider](https://github.com/swedenconnect/saml-identity-provider) repository.

- BankID configuration. This is the BankID-specific configuration used by the BankID SAML IdP. See below for all possible settings.

Also check the [application.yml](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/resources/application.yml) file for an example of how to configure the service.

### BankID Configuration

Below follows all BankID-specific settings:

| Property | Description | Type | Default value |
| :--- | :--- | :--- | :--- |
| `bankid.service-url` | The URL to the BankID API. | `String` | `https://appapi2.bankid.com/rp/v5.1` |
| `bankid.`<br />`server-root-certificate` | The root certificate of the BankID server TLS credential. | A `Resource` pointing at an X.509 certificate. | `classpath:bankid-trust-prod.crt` |
| `bankid.authn.*` | IdP Authentication configuration. See [Authentication Configuration](#authentication-configuration) below. | - | - |
| `bankid.qr-code.*` | See [QR Code Generation Configuration](#qr-code-generation-configuration) below. | - | - |
| TODO: more | - | - | - |

<a name="authentication-configuration"></a>
#### Authentication Configuration

> TODO

<a name="qr-code-generation-configuration"></a>
#### QR Code Generation Configuration

> TODO

## Customizing the BankID IdP

### Customizing the BankID UI

> TODO: We describe how the UI can be modified.

### Audit Logging

### Session Handling


-----

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).

