# Sumo

> A modern, multi-arena Sumo minigame plugin for Minecraft 1.20.x – 1.21.x.

[![CI](https://github.com/diegoh/sumo/actions/workflows/ci.yml/badge.svg)](https://github.com/diegoh/sumo/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Server](https://img.shields.io/badge/Spigot%20%7C%20Paper%20%7C%20Purpur-1.20.x--1.21.x-brightgreen)]()

## Features

- **Multi-arena**: create and run independent arenas concurrently.
- **Per-arena config**: spawns, lobby, bounds, knockback tuning.
- **Matchmaking queue**: per-arena FIFO queue (programmatic; CLI exposure planned).
- **Persistent stats**: wins, losses, current streak, best streak — stored in SQLite (zero-config) or MySQL/MariaDB.
- **i18n**: English and Italian shipped; drop a `messages_<locale>.yml` to add more.
- **Adventure UI**: MiniMessage-formatted chat and scoreboard sidebar.
- **Inventory snapshot** captured on join and restored on leave/elimination.

## Install

1. Download the latest `sumo-x.y.z.jar` from [Releases](https://github.com/diegoh/sumo/releases).
2. Drop it into your server's `plugins/` directory.
3. Start the server — `plugins/Sumo/config.yml` is generated.
4. Optionally edit `plugins/Sumo/config.yml` and `plugins/Sumo/lang/messages_<locale>.yml`.
5. In game: `/sumo create main`, set spawns / lobby / bounds, then `/sumo join main`.

## Commands

| Command | Description | Permission |
|--------|-------------|------------|
| `/sumo join <arena>` | Join an arena. | `sumo.play` |
| `/sumo leave` | Leave your current game. | `sumo.play` |
| `/sumo list` | List arenas and state. | `sumo.play` |
| `/sumo stats [player]` | View stats. | `sumo.play` |
| `/sumo reload` | Reload config, language files, and arenas. | `sumo.admin` |
| `/sumo create <id>` | Create an arena from your current location. | `sumo.admin` |
| `/sumo delete <id>` | Delete an arena. | `sumo.admin` |
| `/sumo setspawn <id> <a\|b>` | Set spawn point. | `sumo.admin` |
| `/sumo setlobby <id>` | Set lobby spawn. | `sumo.admin` |
| `/sumo setbounds <id> <radius>` | Set arena cylinder radius (center = your location). | `sumo.admin` |
| `/sumo setkb <id> <strength> <vertical> <friction>` | Tune knockback. | `sumo.admin` |
| `/sumo forcestart <id>` | Force start. | `sumo.admin` |
| `/sumo forcestop <id>` | Force stop. | `sumo.admin` |

## Permissions

| Node | Default | Description |
|------|---------|-------------|
| `sumo.play` | `true` | Join arenas, view stats. |
| `sumo.admin` | `op` | Full admin access. Implies `sumo.play`. |

## Config

See `src/main/resources/config.yml` for the annotated default.

- `locale.default` / `locale.follow-player-locale` — i18n behavior.
- `storage.driver` = `sqlite` (zero-config) or `mysql` (HikariCP pool).
- `defaults.*` — applied to new arenas; each arena can override its own settings.

## Languages

Drop `plugins/Sumo/lang/messages_<locale>.yml` to add or override a locale. Existing keys override the bundled defaults; missing keys fall back to `en_US`.

## Building

Requires JDK 17.

```bash
./gradlew build
```

The shaded jar lands in `build/libs/sumo-<version>.jar`.

## Live testing

```bash
./gradlew runServer
```

Spawns a Paper 1.20.6 dev server in `./run/` with the plugin pre-loaded. Re-run after editing.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[MIT](LICENSE).
