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

## Configuring the IdP

> TODO
>

## Building docker image and pushing to registry
```bash
export DOCKER_REPO=yourdockerrepo:port
mvn clean install
mvn -f bankid-idp/bankid-idp-backend jib:build
```

## Building, local docker file only
```bash
export DOCKER_REPO=local
mvn clean install
mvn -f bankid-idp/bankid-idp-backend jib:dockerBuild
```

-----

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).

