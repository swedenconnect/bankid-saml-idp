![Logo](images/sweden-connect.png)

# Release Notes

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.swedenconnect.bankid/bankid-idp/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.swedenconnect.bankid/bankid-idp)

### Version 1.2.4

**Date:** 2025-05-21

- Bugfix: The actuator endpoint required authentication making it impossible to reach. This has been fixed.

- Dependency updates where reported vulnerabilities were fixed.

### Version 1.2.3

**Date:** 2025-03-19

- Bugfix: The BankID frontend can be refreshed when a user has navigated to a sub route.

- Dependency updates, and most specifically update to latest Tomcat version to avoid [CVE-2025-24813](https://thehackernews.com/2025/03/apache-tomcat-vulnerability-comes-under.html). 

### Version 1.2.2

**Date:** 2025-02-27

- The code was updated to use the latest version of the [credentials-support](https://github.com/swedenconnect/credentials-support) library, enabling Bundle-based configuration of credentials. See https://docs.swedenconnect.se/credentials-support/.

- The BankID frontend can now be hosted at any context path.

### Version 1.2.1

**Date:** 2024-06-26

- No functional additions. Only updates of dependencies.

### Version 1.2.0

**Date:** 2024-04-17

- The configuration for Redis and Audit logging has been changed.

	- The bankid.session.module setting is deprecated and has been replaced with `saml.idp.session.module`.

	- Settings for Audit logging previously configured under `bankid.audit.*` has been moved to `saml.idp.audit.*`.

	- Redis configuration has been updated to use SslBundles for configuration of TLS. See [Redis Configuration](https://docs.swedenconnect.se/bankid-saml-idp/configuration.html#redis-configuration).

### Version 1.1.3

**Date:** 2024-01-23

- Favicon provided.

- Invalid user message leads to wrong error reported by the IdP. This has been fixed.

- Corrected wrong size for image width in SAML metadata.

### Version 1.1.2

**Date:** 2023-12-12

- Redis cluster read mode is now configurable.

- If no metadata signing credential was provided an error would occur. This has been fixed.

----

Copyright &copy;
2022-2025, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se).
Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
