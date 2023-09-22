# Sweden Connect Sandbox Config
In this sample we will explore how deploy and run the BankID-IdP Application in Sweden Connect, Sandbox environment.

The following files are excluded from the example, but they are ordinarily located at the `./config` directory
* idp-keystore.jks
* redis/redis.p12
* redis/trust.p12

Secret values have been changed to`REDACTED`


## Redis
We configure redis similarly to the [local development profile](https://docs.swedenconnect.se/bankid-saml-idp/development.html#local-redis-instance).
The only major difference is that, since we put both Redis and the BankID-IdP Application in docker, we configure them to the same network.

## Deployment
When Redis is up and running we use the `deploy.sh` script.

This script is designed to remove all current versions of the image and fetch the newest one from a docker repository.
To build an image to your own repository, [see how to build your own image](https://docs.swedenconnect.se/bankid-saml-idp/development.html#using-docker)
