# IntersectionServiceApp

## Overview

Validates intersection/district names (source of truth).

Part of the [TrafficFlow](../README.md) project. Independent Maven module, no
parent pom.

MQ: this service publishes to the ActiveMQ queue `intersection-heartbeat-queue` — see [`../common/`](../common). Broker URL and queue name come from the common `co.wethinkcode.trafficflow.mq.MqConfig` class alongside it in this module.

## Project structure

```
intersection-service/
├── pom.xml
└── src/main/java/co/wethinkcode/trafficflow/
    ├── IntersectionServiceApp.java
    └── mq/
        └── MqConfig.java
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
