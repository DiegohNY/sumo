# Sumo

> A modern, multi-arena Sumo minigame plugin for Minecraft 1.20.x – 1.21.x.

[![CI](https://github.com/DiegohNY/sumo/actions/workflows/ci.yml/badge.svg)](https://github.com/DiegohNY/sumo/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/DiegohNY/sumo?include_prereleases&sort=semver)](https://github.com/DiegohNY/sumo/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Server](https://img.shields.io/badge/Spigot%20%7C%20Paper%20%7C%20Purpur-1.20.x--1.21.x-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-17%2B-orange)]()
[![SpigotMC](https://img.shields.io/badge/SpigotMC-Sumo-orange)](https://www.spigotmc.org/resources/sumo.45639/)

Fast-paced 1v1 pushing minigame. Two players enter the ring; the one knocked into water or out of the arena loses. Round-robin tournament until one champion remains. Built for production servers: multi-arena, concurrent sessions, SQL-backed stats, async I/O, Adventure-powered UI, and full i18n.

## Features

- **Multi-arena** — run any number of arenas in parallel, each with its own config.
- **Per-arena config** — spawns, lobby, cylinder bounds, knockback tuning (strength, vertical boost, friction).
- **Matchmaking queue** — per-arena FIFO (programmatic; CLI exposure on the roadmap).
- **Persistent stats** — wins, losses, current streak, best streak — SQLite (zero-config) or MySQL/MariaDB via HikariCP, async I/O.
- **i18n** — English and Italian shipped; drop a `messages_<locale>.yml` for any other locale, with player-locale follow.
- **Adventure UI** — MiniMessage chat, action bars, titles on key events, and live scoreboard sidebar (toggle via `scoreboard.enabled`).
- **Chest GUI arena selector** — `/sumo menu` (toggle via `gui.enabled`).
- **Inventory snapshot** — captured on join, restored on leave/elimination/quit, survives crashes.
- **Water + out-of-bounds elimination** — classic Sumo rules.
- **Lightweight** — ~1.1 MB shaded jar; JDBC drivers downloaded on demand via `plugin.yml` `libraries:`.

## Requirements

| Component | Version |
|-----------|---------|
| Server software | Spigot / Paper / Purpur (or any fork) |
| Minecraft | 1.20.x – 1.21.x |
| Java | 17 or newer |
| Storage | SQLite (default, bundled at runtime) or MySQL / MariaDB |

## Install

1. Download the latest `sumo-x.y.z.jar` from [GitHub Releases](https://github.com/DiegohNY/sumo/releases) or [SpigotMC](https://www.spigotmc.org/resources/sumo.45639/).
2. Drop it into your server's `plugins/` directory.
3. Start the server — `plugins/Sumo/config.yml` is generated.
4. (Optional) Edit `plugins/Sumo/config.yml` and `plugins/Sumo/lang/messages_<locale>.yml`.
5. In game: `/sumo create main`, set spawns / lobby / bounds, then `/sumo join main`.

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/sumo join <arena>` | Join an arena. | `sumo.play` |
| `/sumo leave` | Leave your current game. | `sumo.play` |
| `/sumo list` | List arenas and their state. | `sumo.play` |
| `/sumo stats [player]` | View stats. | `sumo.play` |
| `/sumo menu` | Open the chest GUI arena selector (toggle via `gui.enabled`). | `sumo.play` |
| `/sumo reload` | Reload config, language files, and arenas. | `sumo.admin` |
| `/sumo create <id>` | Create an arena from your current location. | `sumo.admin` |
| `/sumo delete <id>` | Delete an arena. | `sumo.admin` |
| `/sumo setspawn <id> <a\|b>` | Set spawn point A or B. | `sumo.admin` |
| `/sumo setlobby <id>` | Set lobby spawn. | `sumo.admin` |
| `/sumo setbounds <id> <radius>` | Set arena cylinder radius (center = your location). | `sumo.admin` |
| `/sumo setkb <id> <strength> <vertical> <friction>` | Tune per-arena knockback. | `sumo.admin` |
| `/sumo forcestart <id>` | Force start a tournament. | `sumo.admin` |
| `/sumo forcestop <id>` | Force stop a tournament. | `sumo.admin` |

## Permissions

| Node | Default | Description |
|------|---------|-------------|
| `sumo.play` | `true` | Join arenas, view stats. |
| `sumo.admin` | `op` | Full admin access. Implies `sumo.play`. |

## Configuration

See [`src/main/resources/config.yml`](src/main/resources/config.yml) for the annotated default.

- `locale.default` / `locale.follow-player-locale` — i18n behavior.
- `storage.driver` = `sqlite` (zero-config) or `mysql` (HikariCP pool).
- `defaults.*` — applied to new arenas; each arena can override its own settings.

To use MySQL/MariaDB:

```yaml
storage:
  driver: mysql
  mysql:
    host: localhost
    port: 3306
    database: sumo
    username: sumo
    password: ""
```

## Languages

Drop `plugins/Sumo/lang/messages_<locale>.yml` to add or override a locale. Existing keys override the bundled defaults; missing keys fall back to `en_US`. Bundled: `en_US`, `it_IT`. Contributions for additional locales welcome — see [CONTRIBUTING.md](CONTRIBUTING.md#adding-a-language).

## FAQ

**Does it support multiple worlds?**
Yes. Arenas can live in any world, and different arenas can be in different worlds (e.g. one in your hub, one in the nether). Just stand in the world you want and run `/sumo create <name>`. The only rule is that a single arena's two spawns and lobby must share the same world, since it's a 1v1 ring.

**Can I run several arenas at the same time?**
Yes — each arena runs its own independent game concurrently.

**Do I need a database?**
No. SQLite is built in and needs zero setup. Switch to MySQL/MariaDB only for networks or large player counts.

**Are player inventories safe?**
Yes. They're snapshotted on join and restored on leave, elimination or disconnect — even after a crash.

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

Spawns a Paper 1.21.11 dev server in `./run/` with the plugin pre-loaded. Re-run after editing. For remote debug attach on port `5005`:

```bash
./gradlew runServer --debug-jvm
```

## Roadmap

See the `[Unreleased]` section of [CHANGELOG.md](CHANGELOG.md) for planned work (queue CLI, bStats, PlaceholderAPI, ELO leaderboard, tournament bracket, Brigadier-native commands, mid-match reconnect).

## Contributing

Contributions welcome. New to the codebase? Start with [ARCHITECTURE.md](ARCHITECTURE.md) — it maps out the packages and how a match flows, so you only need to understand the part you're touching. Then see [CONTRIBUTING.md](CONTRIBUTING.md) for setup, code style (Google Java Format), commit convention (Conventional Commits), and the testing policy. By participating you agree to the [Code of Conduct](CODE_OF_CONDUCT.md).

## Security

Found a vulnerability? Please follow [SECURITY.md](SECURITY.md) and do **not** open a public issue.

## Support & contact

- **Bug reports / feature requests:** [GitHub Issues](https://github.com/DiegohNY/sumo/issues)
- **Questions & ideas:** [GitHub Discussions](https://github.com/DiegohNY/sumo/discussions)
- **SpigotMC page:** <https://www.spigotmc.org/resources/sumo.45639/>
- **Email:** diegosici@icloud.com
- **Discord:** `@DiegohNY`

## License

[MIT](LICENSE). Free to use, modify, redistribute, and ship inside commercial products — only requirement is keeping the copyright notice and license text in any copy or substantial portion of the source.
