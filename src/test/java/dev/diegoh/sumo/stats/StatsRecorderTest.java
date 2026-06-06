package dev.diegoh.sumo.stats;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.diegoh.sumo.SumoPlugin;
import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaBounds;
import dev.diegoh.sumo.arena.KnockbackConfig;
import dev.diegoh.sumo.game.GameSession;
import dev.diegoh.sumo.player.InventoryStore;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatsRecorderTest {
  private ServerMock server;
  private WorldMock world;
  private SumoPlugin plugin;
  private SqlStatsRepository repo;
  private StatsService service;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
    world = server.addSimpleWorld("world");
    plugin = MockBukkit.load(SumoPlugin.class);
    repo = SqlStatsRepository.sqlite("jdbc:sqlite::memory:");
    repo.init();
    service = new StatsService(repo);
  }

  @AfterEach
  void tearDown() {
    service.shutdown();
    repo.close();
    MockBukkit.unmock();
  }

  @Test
  void eliminationRecordsWinAndLoss() throws Exception {
    GameSession session = new GameSession(plugin, arena(), new InventoryStore(), 0);
    new StatsRecorder(service, Logger.getLogger("test")).attach(session);
    PlayerMock winner = server.addPlayer();
    PlayerMock loser = server.addPlayer();
    session.addPlayer(winner);
    session.addPlayer(loser);
    assertTrue(session.startTournament()); // countdown 0 -> ACTIVE immediately

    session.recordElimination(loser.getUniqueId());

    assertEquals(1, await(winner.getUniqueId()).wins());
    assertEquals(1, await(loser.getUniqueId()).losses());
  }

  /** Stats are written asynchronously; poll the repository until the write lands. */
  private PlayerStats await(UUID uuid) throws Exception {
    for (int i = 0; i < 100; i++) {
      PlayerStats s = repo.load(uuid).get();
      if (s.totalGames() > 0) return s;
      Thread.sleep(20);
    }
    return repo.load(uuid).get();
  }

  private Arena arena() {
    return Arena.builder()
        .id("main")
        .spawnA(new Location(world, 0, 64, 5))
        .spawnB(new Location(world, 0, 64, -5))
        .lobby(new Location(world, 0, 80, 0))
        .bounds(ArenaBounds.cylinder(new Location(world, 0, 64, 0), 15.0))
        .knockback(new KnockbackConfig(1.0, 0.4, 0.5))
        .minPlayers(2)
        .maxPlayers(4)
        .build();
  }
}
