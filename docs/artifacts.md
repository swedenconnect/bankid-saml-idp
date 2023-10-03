![Logo](images/sweden-connect.png)

# Downloading Artifacts

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.swedenconnect.bankid/bankid-idp/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.swedenconnect.bankid/bankid-idp)

-----

<a name="maven-central"></a>
## Maven Central

The BankID IdP artifacts are published to [Maven Central](https://central.sonatype.com/).

**BankID IdP with packaged frontend:**

```xml
<dependency>
  <groupId>se.swedenconnect.bankid</groupId>
  <artifactId>bankid-idp</artifactId>
  <version>${bankid.idp.version}</version>
</dependency>
```

**BankID IdP with no frontend:**

```xml
<dependency>
  <groupId>se.swedenconnect.bankid</groupId>
  <artifactId>bankid-idp-backend</artifactId>
  <version>${bankid.idp.version}</version>
</dependency>
```

> This is typically used if you supply your own frontend application, see [Extending the BankID Backend Application](https://docs.swedenconnect.se/bankid-saml-idp/override.html#extending-the-bankid-backend-application).

**Note:** Currently, the `bankid-idp-backend` artifact is not published to Maven central. If you need
this artifact, build it from source:

```bash
cd bankid-idp
mvn clean install -Dbackend-only
```

**BankID IdP Frontend distribution:**
```xml
<dependency>
  <groupId>se.swedenconnect.bankid</groupId>
  <artifactId>bankid-idp-frontend</artifactId>
  <version>${bankid.idp.version}</version>
  <type>zip</type>
</dependency>
```

> Used in projects where you need to modify the frontend code beyond [customizations](https://docs.swedenconnect.se/bankid-saml-idp/override.html#customizing-the-bankid-idp-ui).

**BankID Relying Party API Implementation:**

```xml
<dependency>
  <groupId>se.swedenconnect.bankid</groupId>
  <artifactId>bankid-rp-api</artifactId>
  <version>${bankid.idp.version}</version>
</dependency>
```

> If you want to get access to the Java library implementing the BankID Relying Party API.

-----

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
