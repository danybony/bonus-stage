# Contributing

Thanks for helping with Bonus Stage. Issues and pull requests are welcome.

## Before you start

- Look at the [open issues](../../issues). For anything bigger than a small fix, open or comment on an issue first, so we can agree on the approach.
- Read [docs/plan.md](docs/plan.md) (Italian) and the decisions in [docs/adr/](docs/adr/) before starting a new area of work. [CLAUDE.md](CLAUDE.md) is a compact summary of the architecture and the rules below, written for coding agents but useful for people too.

## Build

You need JDK 17 or newer, the Android SDK (with `sdk.dir` in `local.properties`), and the rcheevos submodule:

```sh
git clone --recurse-submodules https://github.com/danybony/bonus-stage.git
# or, in an existing clone:
git submodule update --init
```

Gradle installs the NDK and CMake versions it needs for the ROM hashing library.

| Task | Command |
| --- | --- |
| Build and install the Android app | `./gradlew :androidApp:installDebug` |
| Shared tests on the JVM | `./gradlew :composeApp:testAndroidHostTest :ra-api:jvmTest :ra-api:testAndroidHostTest` |
| Shared tests on the iOS simulator (macOS) | `./gradlew :composeApp:iosSimulatorArm64Test :ra-api:iosSimulatorArm64Test` |
| Run on iOS (macOS) | open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme; set `TEAM_ID` in `iosApp/Configuration/Config.xcconfig` for a physical device |

To use the app you need a RetroAchievements account: your web API key is on [retroachievements.org/settings](https://retroachievements.org/settings). The demo profile on the sign-in screen works without one.

## Rules

- **Never commit API keys or credentials.** Keep them in `local.properties` (git-ignored) or environment variables.
- **The RetroAchievements API has unpublished rate limits.** Every read goes through the local cache; never call the API in a loop without throttling.
- **Licenses.** Dependencies, fonts and assets must be MIT-compatible (MIT, Apache-2.0, BSD, OFL, CC0). Record every font or asset in [docs/assets.md](docs/assets.md). Visual inspiration from other projects is fine; copying their code or assets is not.
- **Unofficial.** Do not use the RetroAchievements name or logo in a way that suggests endorsement.
- **Shared first.** Code that can live in `commonMain` goes there; platform code stays behind `expect`/`actual` or an injected interface.
- **Database changes** need a migration in `composeApp/src/commonMain/sqldelight/migrations/`.
- **UI** uses the retro theme and the components in `ui/components/`, not Material components directly.
- Code, comments, commits and issues in English.

## Pull requests

- One issue per pull request, kept small.
- Add tests for domain and data code in `commonTest`.
- Include a screenshot for UI changes.
- CI builds Android and runs the JVM tests on every pull request; pull requests touching shared or iOS code also build iOS and attach simulator screenshots.

By contributing, you agree that your contributions are licensed under the [MIT License](LICENSE).
