# IntersectionWatchdogApp

## Overview

Cries for help if the Intersection Service crashes, since routes can no longer be validated.

Part of the [TrafficFlow](../README.md) project — its alerting service.
Independent Maven module, no parent pom.

MQ: this service subscribes to the ActiveMQ queue `intersection-heartbeat-queue` — see [`../common/`](../common). Broker URL and queue name come from the common `co.wethinkcode.trafficflow.mq.MqConfig` class alongside it in this module. Mechanism: watch for missed heartbeats and/or dead-lettered messages from `intersection-service` and raise an alert.

## Project structure

```
intersection-watchdog/
├── pom.xml
└── src/main/java/co/wethinkcode/trafficflow/
    ├── IntersectionWatchdogApp.java
    └── mq/
        └── MqConfig.java
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
