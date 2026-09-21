# TrafficFlow — Implementation Notes

A short account of how each stage was built, the problems hit along the way,
and what I'd do differently with more time.

## Stage 1 — Ingestion

**What it does:** reads `intersections-legacy.csv` and normalizes casing,
whitespace, missing-value placeholders, and boolean flag variants, then
collapses duplicate records (same intersection under different ID casing)
into one.

**Struggles:**
- Deciding what to do with a value like `"unknown"` in the `active_flag`
  column — defaulting it to `false` would have been a guess, not a fact.
  Settled on keeping it as an explicit `null` so downstream consumers can
  tell the difference between "known false" and "we don't know."
- Getting the dedup logic right for ID casing — `INT-1005` and `int-1005`
  needed to collapse into a single record. Solved by uppercasing the ID both
  for storage and as the map key used to detect duplicates.

**Solution:** a small set of helper functions (`normalizeMissing`,
`parseBoolean`) applied uniformly to every field before anything else
touches the data, plus a `LinkedHashMap` keyed on the uppercased ID for
dedup with clear "last write wins" semantics.

## Stage 2 — REST wiring

**What it does:** intersection-service loads ingestion-service's cleaned
data over HTTP at startup; congestion-service exposes a simple 0-8 level;
routing-service calls both and computes a real, congestion-sensitive travel
time estimate.

**Struggles:**
- Handling the case where a dependency isn't up yet or goes down mid-run.
  Early versions let an unhandled exception crash the whole app on startup
  if ingestion-service wasn't reachable.
- Choosing sensible HTTP status codes for the unhappy paths — a bad
  intersection id and an unreachable service are different failure modes and
  deserve different codes (404 vs 503), not one generic 500.

**Solution:** wrapped startup fetches in try/catch with a clear warning
log instead of a crash, and split routing-service's error handling into a
custom `NotFoundException` (→ 404) versus any other failure (→ 503), so the
caller can tell "this route doesn't exist" apart from "try again later."

## Stage 3 — MQ decoupling

**What it does:** congestion-service publishes level changes to the
`congestion-topic` ActiveMQ topic; routing-service subscribes instead of
polling.

**Struggles:**
- Understanding *why* this is better than the Stage 2 direct call, beyond
  "it's what the brief asked for." The concrete difference only became
  obvious once I'd built and tested the polling version — with polling,
  routing-service depends on congestion-service being up and fast *right at
  request time*; with pub/sub, congestion-service can be briefly down and
  routing-service just serves the last level it received, no error at all.
- A missing `jackson-databind` dependency in several poms caused confusing
  runtime errors (`ctx.json(...)` failing) that had nothing to do with the
  MQ code itself — a reminder that Javalin needs an explicit JSON mapper
  dependency per module, since these are independent Maven projects with no
  shared parent pom to pull it in once.

**Solution:** a `MessageListener` on routing-service's JMS consumer updates
an `AtomicInteger` whenever a new level arrives; the REST handler just reads
that in-memory value instead of making an HTTP call.

## Stage 4 — Watchdog alerting

**What it does:** intersection-service publishes a heartbeat to the
`intersection-heartbeat-queue` every 5 seconds; intersection-watchdog tracks
the last-seen timestamp and raises an alert if too much time passes without
one.

**Struggles:**
- Distinguishing "the watchdog hasn't received a heartbeat yet because it
  just started" from "intersection-service actually died" — both look the
  same from the watchdog's point of view at t=0.
- Environment/tooling friction (Windows vs WSL vs `/mnt/c/...` paths, stale
  jars, ports left bound by processes I'd forgotten were still running) cost
  far more time than the actual heartbeat/alert logic did.

**Solution:** the alert threshold is set to 3x the heartbeat interval (15
seconds) rather than 1x, so a single missed beat or GC pause doesn't trigger
a false alarm — it takes a sustained absence to flip the alert.

## What I'd do differently with more time

- Pool JMS connections instead of opening a new one per publish in
  congestion-service and intersection-service's heartbeat — fine at this
  scale, but wasteful under real load.
- Add retry/backoff to the HTTP calls between services instead of failing
  immediately on the first connection error.
- Add a `GET /districts/{name}` endpoint to intersection-service for
  district-level validation, not just per-intersection lookup.
- Standardize where I run this project from one environment from day one —
  switching between Windows and WSL paths caused several self-inflicted
  bugs (stray files ending up in the wrong module, stale jars) that had
  nothing to do with the actual system design.