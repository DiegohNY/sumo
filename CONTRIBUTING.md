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

## Code style

- Google Java Format (enforced by Spotless). Run `./gradlew spotlessApply` before committing.
- Conventional Commits required for PRs (`feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`, `ci:`).

## Test policy

- New code requires unit tests.
- Multi-step gameplay changes require an integration test using MockBukkit.

## Adding a language

Copy `src/main/resources/lang/messages_en_US.yml` to `messages_<locale>.yml`, translate, and open a PR.
