![Logo](images/sweden-connect.png)

# Development Guidelines for the SAML IdP for BankID

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

-----

**Note:** All contributors to this project are expected to follow the guidelines stated in the [Contributing to the BankID SAML IdP](https://github.com/swedenconnect/bankid-saml-idp/blob/main/CONTRIBUTING.md) document.

The BankID IdP backend is a Spring Boot application and the frontend is a Vue-application. It is 
expected that you are familiar with these frameworks before starting development activities.

<a name="obtaining-the-bankid-idp-source"></a>
## Obtaining the BankID IdP Source

The source for the project is located at https://github.com/swedenconnect/bankid-saml-idp and
you are free to clone the repository or create a [fork](https://docs.github.com/en/get-started/quickstart/fork-a-repo).

<a name="running-the-service-locally"></a>
## Running the Service Locally

To develop the service we recommend the use of the "local" profile.

In this mode the service will require two dependencies: 

- a configured [service provider](#saml-service-provider-for-local-test-and-development) (SP), and,
- a [local test instance of Redis](#local-redis-instance).

When the Redis instance has been started you should be able to start the BankID IdP-application with the "local" profile active. Next, start the [Test-SP](#saml-service-provider-for-local-test-and-development)
and send authentication requests.

To start the BankID IdP-application with the "local" profile the following environment variables should be set to load the relevant properties.
```bash
SPRING_CONFIG_IMPORT=bankid-idp/bankid-idp-backend/env/local/developer.yml
SPRING_PROFILES_ACTIVE=local
```


:exclamation: Don't you have a test-BankID installed on your device? See [BankID and Sweden Connect Resources](https://docs.swedenconnect.se/bankid-saml-idp/bankid-sc-resources.html).

<a name="saml-service-provider-for-local-test-and-development"></a>
### SAML Service Provider for Local Test and Development

A pre-configured SAML Service Provider that can be used to send requests to the BankID IdP when
running under the "local" profile can be found at https://github.com/swedenconnect/test-my-eid.

Clone or fork this repository and start the "Test my eID"-application with the "local" profile active.

Point your browser to `https://localhost:9445/testmyeid/` and you should see something like:

![Test-my-eid](images/test-my-eid.png)

Click the "BankID (local)" option and a SAML request should be sent to the IdP.

> Not working? Well, you need to start the IdP application ...

<a name="local-redis-instance"></a>
### Local Redis Instance

When running with the local profile, there is a docker-compose file for Redis that works out of the box under the following path [bankid-idp-backend/env/local/redis/docker-compose.yml](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/env/local/redis/docker-compose.yml).

To start Redis, simply run:

```
> docker compose -f ${CODE_ROOT}/bankid-idp-backend/env/local/redis/docker-compose.yml up
```

<a name="using-docker"></a>
## Using Docker

**Building Docker image and pushing to registry:**

```bash
export DOCKER_REPO=yourdockerrepo:port
mvn clean install
mvn -f bankid-idp/bankid-idp-backend jib:build
```

**Building, local Docker image only:**

```bash
export DOCKER_REPO=local
mvn clean install
mvn -f bankid-idp/bankid-idp-backend jib:dockerBuild
```

## Device Support
[Usage statistics][1]

> TODO: move

| OS       | Browser          | Supported |
|----------|------------------|-----------|
| iOS      | Safari           | 🟡        |
| Windows  | Chrome           | ✅         |
| Android  | Chrome           | 🟡        |
| Windows  | Edge             | ✅         |
| macOS    | Safari           | ✅         |
| macOS    | Chrome           | ✅         |
| ChromeOS | Chrome           | ❌         |
| Windows  | Firefox          | ✅         |
| Android  | Samsung Internet | 🟡        |

| Legend | Description                                   |
|--------|-----------------------------------------------|
| ✅      | Supported and works by default                |
| 🟡     | Supported but requires device detection       |
| ❌      | Might work but will not be tested for support |

[1]: https://analytics.usa.gov/data/ "OS & browser (combined)"


-----

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).