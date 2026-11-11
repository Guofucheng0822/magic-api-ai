# Building magic-api

## Supported JDKs

| JDK | Build | Notes |
|-----|-------|-------|
| 17 (LTS) | ✅ | Minimum / bytecode target (`--release 17`) |
| 18–20 | ✅ | Supported |
| 21 (LTS) | ✅ | Supported; verified in CI |
| ≤16 | ❌ | Enforcer fails; jakarta / springdoc modules need 17+ |

**Choice:** this fork bumps the toolchain to **Java 17** (not multi-release JARs keeping Java 8 bytecode). Reasons:

1. `magic-api-servlet-jakarta` and `magic-api-plugin-springdoc` already required Java 17.
2. A single `maven.compiler.release=17` across the reactor is simpler than mixed 8/17 outputs.
3. Downstream AI work on this fork is expected to run on modern JDKs.

Spring Boot BOM for most modules remains **2.4.5** (javax / Java EE servlet path). The jakarta servlet module is for Spring Boot 3 / Jakarta EE consumers and keeps its own Boot 3-oriented APIs. Prefer a Boot version that officially supports your runtime JDK when embedding the starter (e.g. Boot 2.7+ on JDK 17, Boot 3.x with the jakarta artifact).

## Prerequisites

- JDK 17–21
- Maven 3.6.3+
- Node.js / npm (plugin frontend assets under `magic-api-plugin-task` and `magic-api-plugin-component` run `npm` during package)

## Commands

```bash
# Compile all modules
mvn -DskipTests compile

# Package all modules
mvn -DskipTests package

# Clean package
mvn -DskipTests clean package

# Verify with a specific JDK (examples)
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 mvn -DskipTests clean package
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -DskipTests clean package
```

The Maven Enforcer plugin requires `[17,22)` so accidental builds on JDK 8/11/22+ fail fast with a clear message.

## Modules

| Module | Role | JDK notes |
|--------|------|-----------|
| `magic-api` | Core | release 17 |
| `magic-editor` | Editor static resources | release 17 |
| `magic-api-servlet-javaee` | javax.servlet bridge | release 17 |
| `magic-api-servlet-jakarta` | jakarta.servlet bridge | release 17 (Jakarta Servlet 6) |
| `magic-api-plugins/*` | Optional plugins | release 17; springdoc needs Jakarta |
| `magic-api-spring-boot-starter` | Boot auto-config | release 17 |
