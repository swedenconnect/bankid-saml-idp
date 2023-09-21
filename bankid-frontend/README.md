![Logo](../docs/images/sweden-connect.png)

# BankID IdP Frontend

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.swedenconnect.bankid/bankid-idp/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.swedenconnect.bankid/bankid-idp)

-----

## About

This directory contains the BankID IdP Frontend code. It is packaged into a ZIP that is published
on Maven Central. 

```xml
<dependency>
  <groupId>se.swedenconnect.bankid</groupId>
  <artifactId>bankid-idp-frontend</artifactId>
  <version>${bankid.idp.version}</version>
  <type>zip</type>
</dependency>
```

Users that use the BankID IdP in its default mode where the frontend is integrated into the IdP
application do not have to download this artifact. However, if you want to make changes to the
frontend that go beyond [overrides and customization](https://docs.swedenconnect.se/bankid-saml-idp/override.html#customizing-the-bankid-idp-ui) you may want to download this artifact.

## Development

### Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur) + [TypeScript Vue Plugin (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.vscode-typescript-vue-plugin).

## Customize configuration

See [Vite Configuration Reference](https://vitejs.dev/config/).

### Project Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Compile and Minify for Production

```sh
npm run build
```


-----

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
