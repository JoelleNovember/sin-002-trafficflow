# TrafficFlow — Rubric

**For reviewers only. Do not share with candidates.**

Scores against the stages in the root [README.md](README.md#your-task) "Your task"
section. Stages 1-2 are required; stages 3-4 are stretch — weight accordingly if a
candidate ran out of time on the stretch stages.

## Stage 1 — Ingestion (required)

- [ ] Handles casing inconsistencies (IDs, district, signal type)
- [ ] Trims padding (leading/trailing/double spaces)
- [ ] Detects and collapses duplicate records for the same real-world intersection
- [ ] Handles missing/placeholder values explicitly (not silently dropped, not guessed)
- [ ] Normalizes the `active_flag` boolean representations (`Y`/`N`, `yes`/`no`, `1`/`0`, `true`/`FALSE`)
- [ ] Cleaned data is exposed for other services to consume (any reasonable shape)

## Stage 2 — Domain services + REST wiring (required)

- [ ] `intersection-service` exposes a way to validate/look up an intersection or district
- [ ] `congestion-service` exposes the current city-wide congestion level (0-8)
- [ ] `routing-service` calls both of the above over HTTP and produces an estimated
      travel time that's actually a function of congestion + intersection validity
      (not a hardcoded value)
- [ ] Sensible HTTP status codes / error handling for the unhappy path (unknown
      intersection, unreachable dependency, etc.)

## Stage 3 — MQ decoupling (stretch)

- [ ] `congestion-service` publishes to `congestion-topic` on level change
- [ ] `routing-service` subscribes instead of polling `congestion-service` directly
- [ ] Broker URL/topic name sourced from `MqConfig`, not hardcoded inline

## Stage 4 — Watchdog alerting (stretch)

- [ ] `intersection-service` publishes heartbeats to `intersection-heartbeat-queue`
- [ ] `intersection-watchdog` detects a missed heartbeat / dead-lettered message and
      surfaces it somehow (log, endpoint, exit code — any observable signal is fine)

## Cross-cutting (all stages)

- [ ] **Readability** — reasonable naming, no dead code, code a teammate could pick up
- [ ] **Incremental commits** — history shows the stages being built up, not one giant commit
- [ ] **Tests** — at least some automated coverage of the ingestion cleaning logic in particular
- [ ] **Tradeoffs** — candidate can articulate what they'd do differently with more
      time, and why they made the calls they did (especially around the parts the
      README deliberately left open, like exact endpoint/message shapes)

## Notes for reviewers

- Exact field names, endpoint paths, and message schemas are intentionally
  unspecified in the README — don't penalize a candidate for choosing something
  different than what's shown in the "Integration contracts" section, as long as
  it's internally consistent and the intent is right.
- `intersections-legacy.csv` (see [`ingestion-service/`](ingestion-service)) doesn't
  actually contain a date or numeric column, even though the "Known data issues"
  list in that README mentions inconsistent date formats and invalid numeric
  values as things to watch for generally. Don't dock a candidate for not handling
  those two categories against this specific file.
