package dev.diegoh.sumo.stats;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatsServiceTest {
    private SqlStatsRepository repo;
    private StatsService service;

    @BeforeEach
    void setUp() {
        repo = SqlStatsRepository.sqlite("jdbc:sqlite::memory:");
        repo.init();
        service = new StatsService(repo);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
        repo.close();
    }

    @Test
    void recordWinPersistsAndCaches() throws Exception {
        UUID uuid = UUID.randomUUID();
        service.recordWin(uuid).get();
        assertEquals(1, service.getCached(uuid).orElseThrow().wins());
        assertEquals(1, repo.load(uuid).get().wins());
    }

    @Test
    void recordLossKeepsStreakReset() throws Exception {
        UUID uuid = UUID.randomUUID();
        service.recordWin(uuid).get();
        service.recordLoss(uuid).get();
        PlayerStats stats = repo.load(uuid).get();
        assertEquals(1, stats.wins());
        assertEquals(1, stats.losses());
        assertEquals(0, stats.currentStreak());
        assertEquals(1, stats.bestStreak());
    }
}
