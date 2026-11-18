# Bonus Stage

Unofficial companion app for RetroAchievements (RA). Kotlin Multiplatform with Compose Multiplatform: one shared codebase, Android released first, iOS shortly after. Private for now; it may be open-sourced later under the MIT license, so keep everything MIT-compatible.

The full plan, with the research behind it, is in `docs/plan.md` (Italian). Decisions are recorded in `docs/adr/`. Read both before starting a new area of work. Work is tracked as GitHub issues: pick them in milestone order, one issue per branch and pull request.

## Architecture

- `composeApp/` – the app. `commonMain` holds UI (Compose Multiplatform), domain (use cases) and data (repositories, SQLDelight cache). `androidMain` and `iosMain` hold only platform glue: entry points, encrypted credential storage, background work, widgets.
- `ra-api/` – KMP library: Ktor client and kotlinx.serialization models for the RA Web API. No app logic. The daily contract tests live here too, so the app and the CI check use the same models.
- `iosApp/` – Xcode project hosting the shared UI.

Keep platform code to what cannot be shared. Anything that can live in `commonMain` goes there; platform differences go behind `expect`/`actual` or an interface injected per platform.

## RetroAchievements Web API

- Docs: https://api-docs.retroachievements.org/ . Reference clients for models: https://github.com/RetroAchievements/api-kotlin (JVM only, do not depend on it) and https://github.com/RetroAchievements/api-js .
- Every call carries the user's own web API key in the `y` query parameter. Each user enters username and key in the app.
- Store users by ULID, not username: usernames can change.
- Rate limiting is enabled and the limits are not published. Never call the API in loops without throttling. Every read goes through the local cache first.
- Breaking changes can happen without notice. The models must fail loudly on missing or retyped fields in contract tests (strict mode) and tolerate unknown fields in the app (`ignoreUnknownKeys = true`).

## Cache policy (ADR 0002)

Cache-first, local only, no backend. Show cached data immediately, refresh after. Initial refresh intervals (tune later, keep them in one config object):

| Data | Refresh |
| --- | --- |
| Consoles and game catalog | weekly, or on demand |
| Profile, points, awards | on open, if older than 15 minutes |
| Game progress and detail | on screen open, plus pull-to-refresh |
| Recent achievements, widget | background polling |
| Wishlist and active claims | background polling, a few times a day |

## Visual style (ADR 0004)

Retro, inspired by PokeDaisy (https://github.com/lidor30/pokedaisy): pixel fonts, screens styled after classic game menus, optional LCD/scanline/CRT effects later. Inspiration only: PokeDaisy is GPL-3.0, so never copy its code or assets into this repository. Check the license of every font and asset before adding it, and record it in `docs/assets.md`.

## Rules

- Never commit API keys or credentials. Local keys go in `local.properties` (git-ignored); CI uses repository secrets.
- Dependencies must be MIT-compatible (MIT, Apache-2.0, BSD). Ask before adding anything copyleft.
- The app is unofficial: never use the RetroAchievements name or logo in a way that suggests endorsement.
- Code, comments, commit messages and issues in English.
- Write tests for domain and data layers in `commonTest`. UI changes need a screenshot or preview in the pull request.
- Small pull requests that close one issue each. Do not merge your own pull requests to `main` without review.

## Commands

To be filled in once the project skeleton exists (build, test, run on Android and iOS, contract tests).
