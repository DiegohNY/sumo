# Architecture

This document explains how Sumo is put together so you can find your way around
the codebase and contribute confidently.

## The big picture

Sumo is a standard Bukkit/Spigot plugin. The server loads `SumoPlugin` (declared
in `plugin.yml`), which wires everything together once on startup and tears it
down on shutdown.

The design is deliberately straightforward: small classes, one responsibility
each, no hidden magic.

```
                       ┌─────────────────┐
        server start → │   SumoPlugin    │  ← composition root: builds every
                       │   (onEnable)    │     service once and connects them
                       └────────┬────────┘
                                │ creates & wires
        ┌───────────────┬───────┼─────────────┬───────────────┐
        ▼               ▼       ▼             ▼               ▼
   ArenaService    GameOrchestrator     StatsService      Messages
   (arenas CRUD)   (live games)         (player stats)    (i18n text)
        │               │                   │               │
        ▼               ▼                   ▼               ▼
   ArenaRepository  GameSession         StatsRepository   lang/*.yml
   (YAML files)     (one game's         (SQLite/MySQL)
                     state machine)
```

Two more groups plug into these services:

- **Commands** (`command/`) — translate what a player types into service calls.
- **Listeners** (`listener/`) — react to in-game events (hits, movement, quitting)
  and forward them to the right `GameSession`.

## Package guide

| Package | What lives here |
|---------|-----------------|
| `dev.diegoh.sumo` | `SumoPlugin`, the entry point that builds and connects everything. |
| `arena` | What an arena *is* (`Arena`, `ArenaBounds`, `KnockbackConfig`) and how it is stored (`ArenaRepository`) and managed (`ArenaService`). |
| `game` | The gameplay: a single tournament's state machine (`GameSession`), the manager of all live games (`GameOrchestrator`), and small value types (`GameState`, `Match`). |
| `queue` | Optional per-arena waiting line (`MatchmakingQueue`, `QueueService`). |
| `stats` | Player statistics: the data (`PlayerStats`), the storage contract (`StatsRepository`), a SQL implementation (`SqlStatsRepository`), and a caching front (`StatsService`). |
| `player` | Per-player helpers: inventory save/restore (`InventoryStore`) and a fast "which game is this player in?" lookup (`SessionRegistry`). |
| `scoreboard` | The live sidebar (`ArenaScoreboard`). |
| `i18n` | Translated text: message keys (`MessageKey`), the loader (`Messages`), and per-player language selection (`LocaleResolver`). |
| `config` | Reading `config.yml` (`ConfigLoader` → `PluginConfig`) and converting locations to/from text (`LocationCodec`). |
| `command` | The `/sumo` command, split into one small class per subcommand. |
| `listener` | Bukkit event handlers, one per concern (combat, bounds, protection, connection). |
| `util` | Small shared helpers (`AdventureUtil`). |

## How a match actually flows

1. A player runs `/sumo join <arena>`. `JoinSub` asks `GameOrchestrator` to join.
2. `GameOrchestrator` finds (or creates) the `GameSession` for that arena and
   adds the player. The player's inventory is snapshotted by `InventoryStore`.
3. An admin (or auto-start) calls `startTournament()`. `GameSession` pairs up two
   players into a `Match` and walks through its `GameState`:
   `WAITING → COUNTDOWN → ACTIVE → ENDING`.
4. During `ACTIVE`, `CombatListener` applies custom knockback on hits and
   `BoundsListener` watches for a player leaving the ring or touching water.
5. When someone is eliminated, `GameSession.recordElimination(...)` advances to
   the next match, restoring the loser's inventory. The last fighter standing is
   the winner.

## Design rules we follow

- **One job per class.** If a class is doing two things, it's two classes.
- **Immutable where possible.** `Arena`, `PlayerStats` and configs are value
  objects you copy, not mutate.
- **No `null` across boundaries.** Methods that might not find something return
  `Optional`, never `null`.
- **Don't block the main thread.** All database work in `SqlStatsRepository`
  runs on a background executor and returns a `CompletableFuture`.
- **Test the logic.** Game and arena logic is covered by MockBukkit tests under
  `src/test`. New behaviour should come with a test.

## Want to contribute?

You don't need to understand the whole plugin — just the package you're touching.
Pick a class, read its Javadoc, run `./gradlew test`, and open a pull request.
See [CONTRIBUTING.md](CONTRIBUTING.md) for the dev setup and code style.
