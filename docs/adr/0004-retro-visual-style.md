# 0004 – Retro visual style, inspired by PokeDaisy

- Status: accepted
- Date: 2026-11-17

## Context

The app should feel like part of the retro games it tracks. PokeDaisy (https://github.com/lidor30/pokedaisy) is the reference: pixel fonts, a companion UI drawn in the style of the games' own menus, and optional LCD, scanline and CRT shaders.

## Decision

- Pixel fonts throughout: Pixel Operator for the interface, Pixelify Sans for long text. (Amended 2026-11-18: the first version paired Pixel Operator with Atkinson Hyperlegible, a non-pixel font, which clashed on screens mixing both; Pixelify Sans keeps long text readable while staying pixel. To be revisited if it reads poorly on real devices.)
- Screens and components styled after classic game menus.
- LCD, scanline and CRT effects after the MVP (AGSL shaders on Android, Metal on iOS).
- No Material You.

PokeDaisy is GPL-3.0 with an extra attribution term. It is a visual reference only: no code or assets are copied from it.

## Consequences

- The theme has to be designed before most screens, because changing it later is costly.
- Every font and asset needs a license compatible with MIT, recorded in `docs/assets.md`.
