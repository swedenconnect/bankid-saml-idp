#!/bin/bash

#
# Deployment script for the bankid-idp service in the Sandbox environment
# This script removes any current version of the service and pulls a new one from a docker repository
#

docker rm bankid-saml-idp --force
docker pull ${DOCKER_REPO}/bankid-saml-idp
BANKID_IDP_HOME=/opt/bankid-idp

echo Redeploying docker container bankid-idp ...
docker run -d --name bankid-saml-idp --restart=always \
  --network=bankid-idp \
  -p 8012:8012 \
  -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=sandbox \
  -e SERVER_SERVLET_CONTEXT_PATH="/bankid/idp" \
  -e SAML_IDP_BASE_URL=https://sandbox.swedenconnect.se/bankid/idp \
  -e "TZ=Europe/Stockholm" \
  -e "SPRING_CONFIG_IMPORT=${BANKID_IDP_HOME}/config/sandbox.yml" \
  -v /etc/localtime:/etc/localtime:ro \
  -v /opt/docker/bankid-idp:${BANKID_IDP_HOME} \
  -v /home/ubuntu/deploy/bankid-idp/config:"${BANKID_IDP_HOME}/config" \
  "{DOCKER_REPO}/bankid-saml-idp"

echo Done!