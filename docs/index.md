![Logo](images/sweden-connect.png)

# SAML Identity Provider for BankID

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

-----

## About

Many Swedish public organizations makes use of the [Sweden Connect](https://www.swedenconnect.se) 
SAML-federation in order to offer their users the possibility to login using a Swedish eID. However,
[BankID](https://www.bankid.com), which is the largest eID-provider in Sweden does not offer
 a SAML IdP (Identity Provider). This has lead to that a number of different work-arounds have 
emerged where some organizations have purchased third-party integrations against BankID outside of
the Sweden Connect-federation, others have implemented their own BankID-solutions and some
have even managed to buy or build BankID IdP:s from third party vendors and joined Sweden Connect.

The aim with the [SAML Identity Provider for BankID](https://github.com/swedenconnect/bankid-saml-idp)
open source initiative is to offer organizations a SAML IdP that is implemented according to
the [Swedish eID Framework](https://docs.swedenconnect.se/technical-framework/) and may be 
used within the [Sweden Connect Federation](https://www.swedenconnect.se).

An organization wishing to use the open source BankID-IdP basically takes the following steps:

- Make front-end changes to get an UI look and feel of the service that corresponds with the
organization's requirements.

- Possibly make some back-end changes regarding specific issues (for example to have supervision
or audit logging according to the organization's requirements).

- Deploy the IdP-service within the organization's domain.

- Publish SAML metadata to Sweden Connect (or other federation).

Section XX, YY and ZZ will go into details around this.

<a name="development"></a>
## Development

All contributors to this project are expected to follow the guidelines stated in the [Contributing to the BankID SAML IdP](https://github.com/swedenconnect/bankid-saml-idp/blob/main/CONTRIBUTING.md) document.






<a name="technical-cocumentation"></a>
## Technical Documentation

<a name="obtaining-the-bankid-idp-artifacts"></a>
### Obtaining the BankID IdP Artifacts

<a name="configuration-of-the-bankid-idp"></a>
### Configuration of the BankID IdP


## Building

> TODO

### Building docker image and pushing to registry

```bash
export DOCKER_REPO=yourdockerrepo:port
mvn clean install
mvn -f bankid-idp/bankid-idp-backend jib:build
```

### Building, local docker file only
```bash
export DOCKER_REPO=local
mvn clean install
mvn -f bankid-idp/bankid-idp-backend jib:dockerBuild
```

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

