/**
 * Persistent player statistics.
 *
 * <p>{@link dev.diegoh.sumo.stats.PlayerStats} is the immutable data (wins, losses, streaks).
 * {@link dev.diegoh.sumo.stats.StatsRepository} is the storage contract; {@link
 * dev.diegoh.sumo.stats.SqlStatsRepository} implements it for SQLite and MySQL/MariaDB, doing all
 * I/O off the main thread via {@link java.util.concurrent.CompletableFuture}. {@link
 * dev.diegoh.sumo.stats.StatsService} sits in front with a small cache so reads are cheap.
 */
package dev.diegoh.sumo.stats;
