# Changelog

All notable changes to this project are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Planned

- CLI surface for the matchmaking queue (currently programmatic only).
- bStats opt-in metrics.
- PlaceholderAPI integration.
- ELO/MMR matchmaking and `/sumo top` leaderboard.
- Tournament bracket display.
- Mid-match reconnect with reserved slot.
- Brigadier-native commands for Paper.

## [0.2.0] — 2026-05-29

### Added

- Live session UI: action bar on join, titles on match start / FIGHT! / tournament end, chat broadcasts on elimination, auto-applied scoreboard sidebar that refreshes on every state change.
- Typed `SessionEvent` stream with `subscribe(Consumer)` API on `GameSession` for any future presenter.
- `/sumo menu` — chest-style arena selector with one color-coded item per arena (idle/waiting/countdown/active/ending). Toggle via `gui.enabled`.
- Config keys: `scoreboard.enabled`, `gui.enabled`.
- Contributor docs: `ARCHITECTURE.md`, package-level Javadoc, and a pre-release smoke-test checklist.

### Changed

- Bumped `sqlite-jdbc` to 3.53.1.0, `mariadb-java-client` to 3.5.8, and CI actions (checkout v6, setup-java v5, upload-artifact v7, action-gh-release v3).

### CI

- 80% instruction-coverage gate on domain logic and auto-merge for Dependabot patch/minor updates.

## [0.1.0] — 2026-05-28

### Added

- Multi-arena support with YAML-backed per-arena configuration.
- SQLite and MySQL persistent stats (wins, losses, streak, best streak, total games) via HikariCP.
- Per-arena knockback tuning.
- Per-arena matchmaking queue (programmatic).
- Adventure-powered scoreboard sidebar.
- i18n (English, Italian) with player-locale follow.
- Inventory snapshot/restore.
- Full admin command surface for arena CRUD + force start/stop.
- GitHub Actions CI with JaCoCo coverage and release workflow.
