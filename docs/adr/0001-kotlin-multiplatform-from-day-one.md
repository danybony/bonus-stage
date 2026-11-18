# 0001 – Kotlin Multiplatform and Compose Multiplatform from day one

- Status: accepted
- Date: 2026-11-17

## Context

Android is the first target: the Android market for RetroAchievements companion apps is nearly empty, while iOS already has several. An iOS version should still follow soon after Android.

## Decision

Start as a Kotlin Multiplatform project with Compose Multiplatform for the UI. UI, domain, data and the API client live in `commonMain`. Platform code is limited to entry points, encrypted credential storage, background work, widgets and, later, native rhash integration.

## Consequences

- One codebase for both platforms; iOS is mostly packaging and platform glue.
- Widgets are not shared: Glance on Android, WidgetKit (SwiftUI) on iOS.
- Background work is not shared: WorkManager on Android, BGTaskScheduler on iOS.
- Android-only libraries cannot be used in shared code.
