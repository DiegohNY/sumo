# Contributing

Thanks for your interest! Here's how to get set up.

## Prerequisites

- JDK 17 (Temurin recommended).
- Git.

## Build and test

```bash
./gradlew build
./gradlew test
```

## Live test

```bash
./gradlew runServer
```

This launches a temporary Paper 1.21.11 server in `./run/` with the plugin pre-loaded. Edit any file → re-run.

Attach a debugger on port `5005`:

```bash
./gradlew runServer --debug-jvm
```

## Manual smoke test before a release

Automated tests cover the game logic, but a few things can only be checked on a
real server (teleports, knockback feel, scoreboard/title rendering, the chest
GUI, and a live database). Run this once before tagging a release:

1. `./gradlew runServer` and join with at least two accounts (a second client or
   an alt).
2. `/sumo create test`, then set spawns / lobby / bounds.
3. `/sumo join test` with both players, then `/sumo forcestart test`.
4. Verify: countdown freezes movement, hits apply knockback, leaving the ring or
   touching water eliminates, the scoreboard and titles appear, and inventories
   are restored on leave.
5. Open `/sumo menu` and confirm the arena selector shows the live state.
6. (If you touched storage) set `storage.driver: mysql` and confirm stats persist.

## Code style

- Google Java Format (enforced by Spotless). Run `./gradlew spotlessApply` before committing.
- Conventional Commits required for PRs (`feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`, `ci:`).

## Test policy

- New code requires unit tests.
- Multi-step gameplay changes require an integration test using MockBukkit.

## Adding a language

Copy `src/main/resources/lang/messages_en_US.yml` to `messages_<locale>.yml`, translate, and open a PR.
