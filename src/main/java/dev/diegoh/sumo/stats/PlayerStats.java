package dev.diegoh.sumo.stats;

import java.util.UUID;

public record PlayerStats(
    UUID uuid,
    int wins,
    int losses,
    int currentStreak,
    int bestStreak,
    int totalGames,
    long lastPlayedEpochMillis) {

  public static PlayerStats empty(UUID uuid) {
    return new PlayerStats(uuid, 0, 0, 0, 0, 0, 0L);
  }

  public PlayerStats withWin() {
    int streak = currentStreak + 1;
    return new PlayerStats(
        uuid,
        wins + 1,
        losses,
        streak,
        Math.max(bestStreak, streak),
        totalGames + 1,
        System.currentTimeMillis());
  }

  public PlayerStats withLoss() {
    return new PlayerStats(
        uuid, wins, losses + 1, 0, bestStreak, totalGames + 1, System.currentTimeMillis());
  }
}
