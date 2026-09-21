# RoutingServiceApp

## Overview

Provides estimated travel times based on congestion and intersection.

Part of the [TrafficFlow](../README.md) project. Independent Maven module, no
parent pom.

MQ: this service subscribes to the ActiveMQ topic `congestion-topic` — see [`../common/`](../common). Broker URL and topic name come from the common `co.wethinkcode.trafficflow.mq.MqConfig` class alongside it in this module.


## What's implemented
- `GET /routes/estimate?from={id}&to={id}` — validates both intersection ids
  against intersection-service (404 if unknown, 503 if intersection-service
  is unreachable), then computes an estimate as a function of the `from`
  intersection's signal type and the current congestion level
- Subscribes to the `congestion-topic` ActiveMQ topic at startup instead of
  polling `GET /congestion` — congestion level updates are received
  asynchronously and kept in memory
- Formula: `estimatedMinutes = baseMinutes(signalType) * (1 + congestionLevel / 8.0)`,
  where `baseMinutes` is 1.5 for roundabout, 1.0 for stop-sign, 2.0 for 4-way
  or unknown signal types


## Project structure

```
routing-service/
├── pom.xml
└── src/main/java/co/wethinkcode/trafficflow/
    ├── RoutingServiceApp.java
    ├── Intersection.java
    └── mq/
        └── MqConfig.java
```

## Build

```
mvn package
```

## Run
Requires intersection-service (7021) and the ActiveMQ broker in `../common/`
running first.

```
java -jar target/routing-service.jar
```

Listens on port `7023`.

## Test

No automated tests yet. Manually verify it's up:

```
curl http://localhost:7023/health   # -> OK
curl "http://localhost:7023/routes/estimate?from=INT-1001&to=INT-1005"
curl "http://localhost:7023/routes/estimate?from=NOPE&to=INT-1005" # -> 404
```

To add real tests, add JUnit 5 + the Surefire plugin to `pom.xml`, put tests under
`src/test/java/co/wethinkcode/trafficflow/`, and run `mvn test`.
