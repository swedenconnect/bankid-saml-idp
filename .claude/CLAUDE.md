# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BankID SAML Identity Provider — a SAML IdP for Swedish BankID, built for the Sweden Connect Federation and the Swedish eID Framework. Maintained by DIGG (Swedish Agency for Digital Government).

## Build Commands

```bash
# Full build (backend + frontend)
mvn clean package

# Backend only (skip frontend)
mvn clean package -Dbackend-only

# Run unit tests only
mvn clean test

# Run unit + integration tests
mvn clean verify

# Run a single test class
mvn test -pl bankid-idp -Dtest=MyTestClass

# Run a single test method
mvn test -pl bankid-idp -Dtest=MyTestClass#myMethod

# Frontend (from bankid-frontend/)
npm install && npm run build
npm run test          # Vitest
npm run lint          # ESLint
npm run format        # Prettier
```

## Test Conventions

- **Unit tests** (surefire): files matching `**/*IT` pattern (note: this project uses `*IT` suffix for unit tests run by surefire, despite the unconventional naming)
- **Integration tests** (failsafe, bankid-idp only): files matching `**/*Suite.java`, run during `mvn verify`

## Multi-Module Structure

```
saml-bankid-idp-parent (pom)
├── bankid-api        (jar)  — BankID Relying Party API client library
├── bankid-frontend   (pom)  — Vue 3 + TypeScript frontend, packaged as ZIP
└── bankid-idp        (jar)  — Spring Boot application (the IdP itself)
```

- **bankid-api** (`se.swedenconnect.bankid.rpapi`) — types, service interfaces, and support classes for communicating with the BankID RP API v6.0
- **bankid-frontend** — Vue 3 SPA built via `frontend-maven-plugin`; output is unpacked into bankid-idp's static resources during build
- **bankid-idp** (`se.swedenconnect.bankid.idp`) — the main application, wiring SAML IdP logic (via Sweden Connect's `saml-idp-spring-boot-starter`) with BankID authentication flows

## Architecture (bankid-idp)

Key packages under `se.swedenconnect.bankid.idp`:

| Package | Responsibility |
|---------|---------------|
| `authn` | Authentication controllers and handlers — the core BankID auth/sign flow |
| `config` | Spring `@Configuration` classes and `@ConfigurationProperties` bindings |
| `config.session` | Redis-backed session management (Spring Session + Redisson) |
| `rp` | Integration layer to the BankID RP API (uses bankid-api) |
| `health` | Custom Actuator health indicators (API, RP certificate, SAML metadata) |
| `ext` | OpenSAML/SAML extensions (signature message preprocessing, Redis replay checker) |
| `audit` | Audit event handling |
| `concurrency` | Redis-based distributed locking |

## Tech Stack

- **Java 25**, Spring Boot 4.0.x, Spring Framework 7.x
- **SAML**: Sweden Connect OpenSAML extensions + SAML IdP Spring Boot Starter
- **Frontend**: Vue 3, TypeScript, Vite
- **Session/Cache**: Redis via Redisson + Spring Session
- **Resilience**: Resilience4j
- **Build**: Maven 3.9+, Jib for Docker images
- **Jackson 3** (tools.jackson group)

## Code Style

Follows [Spring Framework Code Style](https://github.com/spring-projects/spring-framework/wiki/Code-Style). Templates in `code-style/` for Eclipse and IntelliJ.

All generated Java code must comply with the rules defined in the project's IntelliJ inspections configuration - code-style/inspection.xml.

## License Header

All Java files require the Apache 2.0 header. Use current year:

```java
/*
 * Copyright 2023-${year} Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * ...
 */
```

## Local Development

Environment variables for running with the `local` profile:

```bash
BANKID_INSTALL_DIR=<repo-root>
SPRING_CONFIG_IMPORT=${BANKID_INSTALL_DIR}/bankid-idp/env/local/developer.yml
SPRING_PROFILES_ACTIVE=local
```

Requires Redis — start via: `docker compose -f bankid-idp/env/local/redis/docker-compose.yml up`

Use [test-my-eid](https://github.com/swedenconnect/test-my-eid) SP with its `local` profile for end-to-end testing.

## Docker

```bash
# Local image only
export DOCKER_REPO=local
mvn clean install
mvn -f bankid-idp/bankid-idp jib:dockerBuild@local
```
