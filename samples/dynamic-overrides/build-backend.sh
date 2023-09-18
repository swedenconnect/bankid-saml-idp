mvn clean install -f ../../bankid-idp/pom.xml -DskipTests=true

if [ `uname -m` = "arm64" ]; then
    DOCKER_REPO='local.dev.swedenconnect.se' mvn clean compile jib:dockerBuild -f ../../bankid-idp/bankid-idp-backend/pom.xml \
	-Djib.from.platforms=linux/arm64\
	-Djib.from.image=arm64v8/openjdk:17
else
    DOCKER_REPO='local.dev.swedenconnect.se' mvn clean compile jib:dockerBuild -f ../../bankid-idp/bankid-idp-backend/pom.xml
fi