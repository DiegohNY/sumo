# Changelog

All notable changes to this project are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Planned

- CLI surface for the matchmaking queue (currently programmatic only).
- PlaceholderAPI integration.
- ELO/MMR matchmaking.
- Tournament bracket display.
- Mid-match reconnect with reserved slot.
- Brigadier-native commands for Paper.
- Persistent inventory snapshots (crash safety) and GUI pagination for >54 arenas.

## [0.2.1] — 2026-06-06

### Added

- **`/sumo top`** — win leaderboard (top 10), backed by an indexed SQL query.
- **Auto-start matchmaking**: an arena starts on its own once it reaches `min-players` (after `join-period-seconds`), and immediately when it fills if `queue.auto-start-when-full` is set. `/sumo forcestart` still works for admins.
- `/sumo setplayers <id> <min> <max>` to change an arena's player limits at runtime.
- Optional **bStats** metrics (off until a service id is configured), reporting arena count, database driver and scoreboard usage.

### Fixed

- Mobs (and any non-opponent entity) could damage a fighter in the lobby and mid-match; now the only damage allowed to an in-game player is a hit from their current opponent while the round is live. Normal combat between players outside Sumo is no longer affected.
- Reworked knockback into a crisp horizontal push plus a vertical pop (velocity is replaced, not blended), fixing the janky/weak feel.
- Match countdown never ended, leaving both fighters frozen for the whole match (movement is blocked during the countdown). The COUNTDOWN → ACTIVE transition is now scheduled and runs after `defaults.match-countdown-seconds`, with a guard so a countdown from an interrupted match can't affect a later one.
- The match countdown is now shown to players as a ticking title (`5… 4… 3…`) followed by a FIGHT! title when it ends, so they know when the round starts.
- At the end of a tournament the winner was never released: they stayed stuck in the arena with the sidebar still showing, the cleared inventory unreturned, and the arena left unjoinable for everyone else. The winner is now restored (inventory + location) and the session is torn down after `defaults.end-delay-seconds`, freeing the arena. Eliminated players are released immediately and their sidebar cleared.
- An eliminated player's sidebar was re-applied right after being cleared, leaving them visually "still in game" — the elimination event now fires after the player leaves the roster, so the clear sticks.
- Arena bounds now auto-fit around the two spawns whenever a spawn is set, so a freshly configured arena is playable without a manual `/sumo setbounds` (previously fighters could be eliminated instantly for spawning outside the default bounds). `/sumo setbounds` still works as a manual override.
- Countdown and FIGHT! titles are shorter and the countdown shows as a subtitle, so they no longer block the view once the round is live.
- Player stats were never recorded (always zero). Each finished match now records a win for the victor and a loss for the eliminated player.
- Auto-fitted arena bounds are now much more generous (buffer raised and a minimum radius of 16) so a push no longer ejects a fighter on the first hit.
- Added input guards: `/sumo setbounds` rejects a non-positive radius and `/sumo setkb` rejects invalid knockback values instead of throwing.
- Inventories are restored on a clean server stop/disable and on rejoin after a mid-game disconnect, so items are no longer lost.
- Fighters are now immune to all non-combat damage (lava, fire, magic, cactus, freeze, …) during a match, and start each match at full health and food.
- Eliminated players are no longer teleported twice (they return to where they came from), and `/sumo forcestop` cleanly restores everyone and frees the arena.
- Player stats use an atomic read-modify-write, preventing lost win/loss updates under concurrent matches.
- Default knockback softened (strength 0.5, vertical 0.35) and auto-fitted bounds widened so a single push no longer ejects a fighter.
- Null-world guards before teleports; joins are refused for arenas whose world isn't loaded.

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
