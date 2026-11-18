# Bonus Stage

Unofficial companion app for RetroAchievements (RA). Kotlin Multiplatform with Compose Multiplatform: one shared codebase, Android released first, iOS shortly after. Private for now; it may be open-sourced later under the MIT license, so keep everything MIT-compatible.

The full plan, with the research behind it, is in `docs/plan.md` (Italian). Decisions are recorded in `docs/adr/`. Read both before starting a new area of work. Work is tracked as GitHub issues: pick them in milestone order, one issue per branch and pull request.

## Architecture

- `composeApp/` – the shared app, a KMP library. `commonMain` holds UI (Compose Multiplatform), domain (use cases), data (repositories, SQLDelight cache), navigation (Navigation 3, destinations in `navigation/`) and DI (Koin, modules in `di/`). `androidMain` and `iosMain` hold only platform glue: encrypted credential storage, background work, the iOS view controller.
- `androidApp/` – the Android application module: `Application`, `MainActivity`, manifest, widgets. AGP 9 does not allow the application plugin in a KMP module, so the Android entry point lives here and depends on `composeApp`.
- `ra-api/` – KMP library: Ktor client and kotlinx.serialization models for the RA Web API. No app logic. Also has a plain `jvm` target for the daily contract tests, so the app and the CI check use the same models.
- `iosApp/` – Xcode project hosting the shared UI. Its build phase runs `:composeApp:embedAndSignAppleFrameworkForXcode`.

Package and application id: `net.bonysoft.bonusstage`. Versions live in `gradle/libs.versions.toml`.

Every Navigation 3 destination must be registered in `navSavedStateConfiguration` (`navigation/Destinations.kt`): iOS has no reflection to save and restore the back stack.

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

UI code uses `RetroTheme.colors` and `RetroTheme.typography` (`ui/theme/`) and the components in `ui/components/` (`RetroWindow`, `RetroMenuItem`, `RetroProgressBar`, `RetroBadge`, `RetroButton`, `RetroDialog`, `RetroTextField`). Do not use Material components directly for visible UI. Images go through `RetroBadge` or Coil's `AsyncImage` with `FilterQuality.None` so pixel art stays sharp.

## Rules

- Never commit API keys or credentials. Local keys go in `local.properties` (git-ignored); CI uses repository secrets.
- Dependencies must be MIT-compatible (MIT, Apache-2.0, BSD). Ask before adding anything copyleft.
- The app is unofficial: never use the RetroAchievements name or logo in a way that suggests endorsement.
- Code, comments, commit messages and issues in English.
- Write tests for domain and data layers in `commonTest`. UI changes need a screenshot or preview in the pull request.
- Small pull requests that close one issue each. Do not merge your own pull requests to `main` without review.

## Commands

Requires JDK 17+ (CI uses 21) and an Android SDK with `sdk.dir` in `local.properties`. iOS needs macOS with Xcode.

| Task | Command |
| --- | --- |
| Build the Android app | `./gradlew :androidApp:assembleDebug` |
| Install and run on a device or emulator | `./gradlew :androidApp:installDebug`, then `adb shell am start -n net.bonysoft.bonusstage/.MainActivity` |
| Shared tests on the JVM (what CI runs on Linux) | `./gradlew :composeApp:testAndroidHostTest :ra-api:jvmTest :ra-api:testAndroidHostTest` |
| Shared tests on the iOS simulator (macOS) | `./gradlew :composeApp:iosSimulatorArm64Test :ra-api:iosSimulatorArm64Test` |
| Run on iOS | open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme; set `TEAM_ID` in `iosApp/Configuration/Config.xcconfig` for a physical device |
| Build iOS from the command line | `xcodebuild build -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO` |
| API contract check against the live API | `RA_USERNAME=… RA_API_KEY=… ./gradlew :ra-api:jvmContractTest` (skips without the variables; add `RA_UPDATE_SNAPSHOTS=true` to rewrite the snapshots in `ra-api/src/jvmTest/resources/contract/`) |

CI: `.github/workflows/ci.yml` builds Android and runs the JVM tests on every pull request. `.github/workflows/ios.yml` runs on macOS only when a pull request touches shared or iOS code, and on every push to `main`. `.github/workflows/api-contract.yml` runs the contract check daily (ADR 0003) with the `RA_USERNAME` and `RA_API_KEY` repository secrets and opens an `api` issue when it fails.
