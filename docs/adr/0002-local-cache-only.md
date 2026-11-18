# 0002 – Local cache only, no backend

- Status: accepted
- Date: 2026-11-17

## Context

The RetroAchievements Web API has rate limiting with unpublished limits and asks clients to cache data. Every user calls the API with their own web API key.

## Decision

Cache everything locally in the app with SQLDelight. No backend. The app is cache-first: it shows cached data immediately and refreshes afterwards, so it also works offline. Initial refresh intervals are listed in `CLAUDE.md` and kept in one configuration object.

## Consequences

- No server costs or operations, and the API load is spread across users' own keys.
- No shared cache: each install fetches its own catalog.
- Revisit when the user base is large, for example with a backend proxy that shares the catalog cache.
