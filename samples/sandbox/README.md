![Logo](../docs/images/sweden-connect.png)

# Sample: How the BankID IdP is Deployed in Sweden Connect Sandbox

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

---

## About this Sample

This sample illustrates how we deploy the BankID IdP to the Sweden Connect Sandbox-environment. This
should be a good example of a typical installation of the IdP.

## Configuration

The [config](https://github.com/swedenconnect/bankid-saml-idp/tree/main/samples/sandbox/config)
directory contains the full configuration for the IdP-instance. See [sandbox.yml](https://github.com/swedenconnect/bankid-saml-idp/tree/main/samples/sandbox/config/sandbox.yml) for the configuration
settings.

> Note: In the [sandbox.yml](https://github.com/swedenconnect/bankid-saml-idp/tree/main/samples/sandbox/config/sandbox.yml) we refer to `idp-keystore.jks`, `redis/redis.p12` and `redis/trust.p12` which
are not packaged in the sample (for security reasons). Also, all secret values have been changed to
`REDACTED`.


## Overrides
In the sandbox environment we add a prompt on how to get and install BankID for Test.
We define what to show in [sandbox.content](config/overrides/sandbox.content) and message definitions with translations in [sandbox.messages](config/overrides/sandbox.messages)

## Redis

We configure Redis similarly to the [Local Development Profile](https://docs.swedenconnect.se/bankid-saml-idp/development.html#local-redis-instance).

The only major difference is that, since we put both Redis and the BankID IdP application in Docker,
we configure them to the same network.

## Deployment

When Redis is up and running we use the `deploy.sh` script.

This script is designed to remove all current versions of the image and fetch the newest one
from a Docker repository.

To build an image to your own repository, see [How to Build Your Own Image](https://docs.swedenconnect.se/bankid-saml-idp/development.html#using-docker).

---

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).