package dev.diegoh.sumo.stats;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlayerStatsTest {

  @Test
  void emptyStartsAtZero() {
    PlayerStats s = PlayerStats.empty(UUID.randomUUID());
    assertEquals(0, s.wins());
    assertEquals(0, s.losses());
    assertEquals(0, s.currentStreak());
    assertEquals(0, s.bestStreak());
    assertEquals(0, s.totalGames());
  }

  @Test
  void winsIncrementStreakAndBest() {
    PlayerStats s = PlayerStats.empty(UUID.randomUUID()).withWin().withWin().withWin();
    assertEquals(3, s.wins());
    assertEquals(3, s.currentStreak());
    assertEquals(3, s.bestStreak());
    assertEquals(3, s.totalGames());
  }

  @Test
  void lossResetsCurrentStreakButKeepsBest() {
    PlayerStats s = PlayerStats.empty(UUID.randomUUID()).withWin().withWin().withLoss();
    assertEquals(2, s.wins());
    assertEquals(1, s.losses());
    assertEquals(0, s.currentStreak());
    assertEquals(2, s.bestStreak());
    assertEquals(3, s.totalGames());
  }

  @Test
  void bestStreakSurvivesLaterShorterStreak() {
    PlayerStats s =
        PlayerStats.empty(UUID.randomUUID())
            .withWin()
            .withWin()
            .withWin() // best = 3
            .withLoss()
            .withWin(); // current = 1, best still 3
    assertEquals(1, s.currentStreak());
    assertEquals(3, s.bestStreak());
  }
}
