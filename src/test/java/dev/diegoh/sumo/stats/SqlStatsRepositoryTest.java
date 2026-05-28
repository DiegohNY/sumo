package dev.diegoh.sumo.stats;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SqlStatsRepositoryTest {
  private SqlStatsRepository repo;

  @BeforeEach
  void setUp() {
    repo = SqlStatsRepository.sqlite("jdbc:sqlite::memory:");
    repo.init();
  }

  @AfterEach
  void tearDown() {
    repo.close();
  }

  @Test
  void loadReturnsEmptyForUnknownPlayer() throws Exception {
    UUID uuid = UUID.randomUUID();
    PlayerStats stats = repo.load(uuid).get();
    assertEquals(0, stats.wins());
    assertEquals(0, stats.losses());
  }

  @Test
  void saveThenLoadRoundTrip() throws Exception {
    UUID uuid = UUID.randomUUID();
    PlayerStats stats = PlayerStats.empty(uuid).withWin().withWin().withLoss();
    repo.save(stats).get();
    PlayerStats loaded = repo.load(uuid).get();
    assertEquals(2, loaded.wins());
    assertEquals(1, loaded.losses());
    assertEquals(0, loaded.currentStreak());
    assertEquals(2, loaded.bestStreak());
    assertEquals(3, loaded.totalGames());
  }
}
