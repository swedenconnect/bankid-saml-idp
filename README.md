![Logo](docs/images/sweden-connect.png)

# SAML Identity Provider for BankID

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.swedenconnect.bankid/bankid-idp/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.swedenconnect.bankid/bankid-idp)

A SAML Identity Provider for BankID. 

-----

## About

This repository comprises of a SAML Identity Provider (IdP) for BankID. The IdP is built according
to the [Swedish eID Framework](https://docs.swedenconnect.se/technical-framework/) and may be
used within the [Sweden Connect Federation](https://www.swedenconnect.se).

The BankID IdP uses the [SAML IdP Spring Boot starter](https://github.com/swedenconnect/saml-identity-provider) project, so most of the SAML-specific code resides in that repository.

The repository also contains a Java library implementing the [BankID Relying Party API](https://www.bankid.com/utvecklare/guider/teknisk-integrationsguide).

## Documentation

Visit the [BankID SAML IdP Documentation](https://docs.swedenconnect.se/bankid-saml-idp/) to learn how
to customize, extend, and build and deploy the application.

You may also want to read the [Sweden Connect eID Framework](https://docs.swedenconnect.se/technical-framework/) specifications and [BankID Development Guides](https://www.bankid.com/utvecklare/guider).

## Notices

### Upgrading to 1.2.0

The configuration for Redis and Audit logging has been changed.

- The `bankid.session.module` setting is deprecated and has been replaced with `saml.idp.session.module`.

- Settings for Audit logging previously configured under `bankid.audit.*` has been moved to `saml.idp.audit.*`.

- Redis configuration has been updated to use SslBundles for configuration of TLS. See [Redis Configuration](https://docs.swedenconnect.se/bank-saml-idp/configuration.html#redis-configuration).

See [Configuration of the BankID SAML IdP](https://docs.swedenconnect.se/bank-saml-idp/configuration.html) for details.

## Contributing

Pull requests are welcome. See the [Contributor Guidelines](CONTRIBUTING.md) for details.

## License

The BankID SAML IdP is Open Source software released under the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).

-----

Copyright &copy; 2023-2025, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
