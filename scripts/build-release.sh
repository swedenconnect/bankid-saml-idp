#!/bin/bash
#
# Internal script for building and deploying the BankID IdP
#
set -e

usage() {
    echo "Usage: $0 [options...]" >&2
    echo
    echo "   -v      Version of BankID IdP to deploy"
    echo "   -c      Make GIT add and check-in of updated apidocs"
    echo "   -d      Publish Docker image to GitHub Container registry"
    echo "   -h      Prints this help"
    echo
}

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

VERSION=""
CHECK_IN_FLAG=false
PUBLISH_IMAGE_FLAG=false
export DOCKER_REPO=ghcr.io

while :
do
    case "$1" in
  -h | --help)
      usage
      exit 0
      ;;
  -v)
      VERSION="$2"
      shift 2
      ;;
  -c)
      CHECK_IN_FLAG=true
      shift 1
      ;;
  -d)
      PUBLISH_IMAGE_FLAG=true
      shift 1
      ;;
  -*)
      echo "Error: Unknown option: $1" >&2
      usage
      exit 0
      ;;
  *)
      break
      ;;
    esac
done

if [ "$VERSION" == "" ]; then
    echo "Error: Missing version" >&2
    usage
    exit 1
fi

if [ "$PUBLISH_IMAGE_FLAG" == true ]; then
    if [ -z "$GITHUB_USER" ]; then
      echo "The GITHUB_USER variable must be set"
      exit 1
    fi

    if [ -z "$GITHUB_ACCESS_TOKEN" ]; then
      echo "The GITHUB_ACCESS_TOKEN variable must be set"
      exit 1
    fi
    
    echo "Logging in to ${DOCKER_REPO} ..."
    echo $GITHUB_ACCESS_TOKEN | docker login $DOCKER_REPO -u $GITHUB_USER --password-stdin    
    
fi

echo "Building Javadoc ..."
mvn clean javadoc:aggregate -Prelease
cp -r target/site/apidocs/* docs/apidocs

# Commit
if [ "$CHECK_IN_FLAG" == true ]; then
    git add docs/apidocs
    git commit -S -m "Added javadoc for ${VERSION}"
fi

echo "Building BankID IdP and BankID RP API ..."

mvn -f ${SCRIPT_DIR}/../pom.xml clean install -Prelease
mvn -f ${SCRIPT_DIR}/../pom.xml deploy -Prelease

if [ "$PUBLISH_IMAGE_FLAG" == true ]; then
  mvn -f ${SCRIPT_DIR}/../bankid-idp/pom.xml jib:build
fi

echo "Version ${VERSION} was successfully deployed"
echo "Remember to tag your release!"




