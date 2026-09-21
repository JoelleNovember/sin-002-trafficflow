# IntersectionServiceApp

## Overview

Validates intersection/district names (source of truth).

Part of the [TrafficFlow](../README.md) project. Independent Maven module, no
parent pom.

MQ: this service publishes to the ActiveMQ queue `intersection-heartbeat-queue` — see [`../common/`](../common). Broker URL and queue name come from the common `co.wethinkcode.trafficflow.mq.MqConfig` class alongside it in this module.


## What's implemented
- On startup, fetches `GET /intersections` from ingestion-service and caches
  it in memory (case-insensitive ID lookup)
- If ingestion-service is unreachable at startup, logs a warning and starts
  with an empty store instead of crashing — lookups will 404 until restarted
  with ingestion-service available
- `GET /intersections/{id}` — returns the record (200) or a clear error (404)
  if the id is unknown
- Publishes a heartbeat message to the `intersection-heartbeat-queue` ActiveMQ
  queue every 5 seconds, consumed by intersection-watchdog (see
  [`../common/`](../common) and [`../intersection-watchdog/`](../intersection-watchdog))


## Project structure

```
intersection-service/
├── pom.xml
└── src/main/java/co/wethinkcode/trafficflow/
    ├── IntersectionServiceApp.java
    ├── Intersection.java
    └── mq/
        └── MqConfig.java
```

## Build

```
mvn package
```

## Run
Requires ingestion-service running first (port 7020), and the ActiveMQ broker
in `../common/` for heartbeats.

```
java -jar target/intersection-service.jar
```

Listens on port `7021`.

## Test

No automated tests yet. Manually verify it's up:

```
curl http://localhost:7021/health   # -> OK
curl http://localhost:7021/intersections/INT-1001 # -> 200 + record
curl http://localhost:7021/intersections/DOES-NOT-EXIST # -> 404
```

To add real tests, add JUnit 5 + the Surefire plugin to `pom.xml`, put tests under
`src/test/java/co/wethinkcode/trafficflow/`, and run `mvn test`.
