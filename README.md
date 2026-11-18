# Bonus Stage

An unofficial companion app for [RetroAchievements](https://retroachievements.org), built with Kotlin Multiplatform and Compose Multiplatform. Android first, iOS shortly after.

> Status: planning. No code yet, see [the plan](docs/plan.md) and the open issues.

## What it does (MVP)

- Sign in with your RetroAchievements username and web API key (stored encrypted on the device)
- Profile with points, rank and awards
- Library with completion progress per game and per console
- Game detail with achievements and a spoiler filter
- Recently unlocked achievements
- Wishlist synced with your "Want to Play" list on RetroAchievements, with a notification when a game's achievement set is in development or gets published

All of it wrapped in a retro look: pixel fonts and screens styled after the menus of classic games.

## Tech stack

- Kotlin Multiplatform, Compose Multiplatform for the shared UI
- Ktor and kotlinx.serialization for the RetroAchievements Web API
- SQLDelight for the local cache
- WorkManager (Android) and BGTaskScheduler (iOS) for background checks

The architecture and the decisions behind it are in [docs/plan.md](docs/plan.md) and [docs/adr/](docs/adr/).

## Disclaimer

Bonus Stage is a fan project. It is not affiliated with or endorsed by RetroAchievements. It uses the public [RetroAchievements Web API](https://api-docs.retroachievements.org/) with each user's own API key.

## License

[MIT](LICENSE)
