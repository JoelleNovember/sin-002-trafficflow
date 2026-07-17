# IntersectionWatchdogApp

## Overview

Cries for help if the Intersection Service crashes, since routes can no longer be validated.

Part of the [TrafficFlow](../README.md) project — its alerting service.
Independent Maven module, no parent pom.

Mechanism: ActiveMQ Queue heartbeat/dead-letter

## Project structure

```
intersection-watchdog/
├── pom.xml
└── src/main/java/co/wethinkcode/trafficflow/IntersectionWatchdogApp.java
```

## Build

```
mvn package
```

## Run

```
java -jar target/intersection-watchdog.jar
```

Listens on port `7024`.

## Test

No automated tests yet. Manually verify it's up:

```
curl http://localhost:7024/health   # -> OK
```

To add real tests, add JUnit 5 + the Surefire plugin to `pom.xml`, put tests under
`src/test/java/co/wethinkcode/trafficflow/`, and run `mvn test`.
