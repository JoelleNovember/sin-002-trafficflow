# IntersectionWatchdogApp

## Overview

Cries for help if the Intersection Service crashes, since routes can no longer be validated.

Part of the [TrafficFlow](../README.md) project — its alerting service.
Independent Maven module, no parent pom.

MQ: this service subscribes to the ActiveMQ queue `intersection-heartbeat-queue` — see [`../common/`](../common). Broker URL and queue name come from the common `co.wethinkcode.trafficflow.mq.MqConfig` class alongside it in this module. Mechanism: watch for missed heartbeats and/or dead-lettered messages from `intersection-service` and raise an alert.

## What's implemented
- Subscribes to the `intersection-heartbeat-queue` ActiveMQ queue at startup
- Tracks the timestamp of the last heartbeat received
- A background check runs every 2 seconds; if no heartbeat has arrived in
  over 3x the expected interval (15 seconds), it raises an alert
- `GET /alert` — exposes `{"alerting": true/false, "lastHeartbeatAt": <ms>}`
  as an observable signal, alongside a log line when the alert first fires

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
Requires the ActiveMQ broker in `../common/` running first.

```
java -jar target/intersection-watchdog.jar
```

Listens on port `7024`.

## Test

No automated tests yet. Manually verify it's up:

```
curl http://localhost:7024/health   # -> OK
curl http://localhost:7024/alert
```
To see it detect a real failure: stop intersection-service, wait ~20 seconds,
then check `/alert` again — it should flip to `"alerting": true`.

To add real tests, add JUnit 5 + the Surefire plugin to `pom.xml`, put tests under
`src/test/java/co/wethinkcode/trafficflow/`, and run `mvn test`.
