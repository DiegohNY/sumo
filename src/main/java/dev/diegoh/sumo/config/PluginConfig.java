package dev.diegoh.sumo.config;

import dev.diegoh.sumo.stats.DatabaseDriver;

public record PluginConfig(
    String defaultLocale,
    boolean followPlayerLocale,
    DatabaseDriver databaseDriver,
    String sqliteFile,
    String mysqlHost,
    int mysqlPort,
    String mysqlDatabase,
    String mysqlUsername,
    String mysqlPassword,
    boolean mysqlUseSsl,
    int poolMaxSize,
    int poolMinIdle,
    long poolConnectionTimeoutMs,
    int defaultMinPlayers,
    int defaultMaxPlayers,
    double defaultKnockbackStrength,
    double defaultKnockbackVertical,
    double defaultKnockbackFriction,
    int matchCountdownSeconds,
    int endDelaySeconds,
    boolean scoreboardEnabled,
    boolean guiEnabled) {}
