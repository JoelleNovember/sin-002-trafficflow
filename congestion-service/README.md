# CongestionServiceApp

## Overview

Tracks the city-wide Congestion Level (0-8).

Part of the [TrafficFlow](../README.md) project. Independent Maven module, no
parent pom.

MQ: this service publishes to the ActiveMQ topic `congestion-topic` — see [`../common/`](../common). Broker URL and topic name come from the common `co.wethinkcode.trafficflow.mq.MqConfig` class alongside it in this module.

## What's implemented
- `GET /congestion` — returns the current level as `{"level": N}`
- `PUT /congestion` — updates the level; validates the body is an integer
  between 0 and 8, returning `400` on invalid input
- On a successful update, publishes the new level to the `congestion-topic`
  ActiveMQ topic (see [`../common/`](../common)), so routing-service can react
  without polling this service directly

## Project structure

```
congestion-service/
├── pom.xml
└── src/main/java/co/wethinkcode/trafficflow/
    ├── CongestionServiceApp.java
    └── mq/
        └── MqConfig.java
```

## Build

```
mvn package
```

## Run
Requires the ActiveMQ broker in `../common/` to be running.

```
java -jar target/congestion-service.jar
```

Listens on port `7022`.

## Test

No automated tests yet. Manually verify it's up:

```
curl http://localhost:7022/health   # -> OK
curl http://localhost:7022/congestion
curl -X PUT http://localhost:7022/congestion -H "Content-Type: application/json" -d '{"level": 5}'
```

To add real tests, add JUnit 5 + the Surefire plugin to `pom.xml`, put tests under
`src/test/java/co/wethinkcode/trafficflow/`, and run `mvn test`.
