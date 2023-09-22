#!/bin/bash
#
# Internal script for building and deploying the BankID IdP
#
set -e

usage() {
    echo "Usage: $0 [options...]" >&2
    echo
    echo "   -v      Version of BankID IdP to deploy"
    echo "   -c      Make GIT add and check-in of updated apidocs."
    echo "   -h      Prints this help"
    echo
}

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

VERSION=""
CHECK_IN_FLAG=false

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

pushd ${SCRIPT_DIR}/.. > /dev/null

echo "Building Javadoc ..."
mvn clean javadoc:aggregate -Prelease
cp -r target/site/apidocs/* docs/apidocs

# Commit
if [ "$CHECK_IN_FLAG" == true ]; then
    git add docs/apidocs
    git commit -m "Added javadoc for ${VERSION}"
fi

echo "Building BankID IdP and BankID RP API ..."

mvn clean deploy -Prelease
mvn nexus-staging:release -Prelease

echo "Building BankID IdP without frontend ..."
popd > /dev/null
pushd ${SCRIPT_DIR}/../bankid-idp > /dev/null

mvn clean deploy -Prelease -Dbackend-only
mvn nexus-staging:release -Prelease -Dbackend-only

popd > /dev/null

echo "Version ${VERSION} was successfully deployed"
echo "Remember to tag your release!"




