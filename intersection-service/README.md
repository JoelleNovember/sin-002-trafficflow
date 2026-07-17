# IntersectionServiceApp

## Overview

Validates intersection/district names (source of truth).

Part of the [TrafficFlow](../README.md) project. Independent Maven module, no
parent pom.

## Project structure

```
intersection-service/
├── pom.xml
└── src/main/java/co/wethinkcode/trafficflow/IntersectionServiceApp.java
```

## Build

```
mvn package
```

## Run

```
java -jar target/intersection-service.jar
```

Listens on port `7021`.

## Test

No automated tests yet. Manually verify it's up:

```
curl http://localhost:7021/health   # -> OK
```

To add real tests, add JUnit 5 + the Surefire plugin to `pom.xml`, put tests under
`src/test/java/co/wethinkcode/trafficflow/`, and run `mvn test`.
