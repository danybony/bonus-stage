# 0003 – Daily API contract monitoring

- Status: accepted
- Date: 2026-11-17

## Context

The RetroAchievements Web API can introduce breaking changes, and keeping up is the client's responsibility. Other companion apps have shipped fixes after API changes broke them.

## Decision

A scheduled GitHub Actions workflow runs once a day:

1. Calls every endpoint the app uses, with a dedicated RA account whose key is stored in repository secrets.
2. Decodes the responses with the app's own kotlinx.serialization models in strict mode: a missing or retyped field fails the job (breaking change).
3. Compares the responses with a schema snapshot committed to the repository: new fields only produce a warning.
4. Checks for new commits in https://github.com/RetroAchievements/api-docs .
5. On failure, opens an issue with the diff automatically.

## Consequences

- Breaking changes are caught within a day, usually before users notice.
- About a dozen calls a day: negligible against the rate limit.
- The models live in the `ra-api` module so the app and the check share them.
