package dev.diegoh.sumo.game;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.diegoh.sumo.SumoPlugin;
import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaBounds;
import dev.diegoh.sumo.arena.KnockbackConfig;
import dev.diegoh.sumo.player.InventoryStore;
import dev.diegoh.sumo.player.SessionRegistry;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameOrchestratorTest {
  private ServerMock server;
  private WorldMock world;
  private SumoPlugin plugin;
  private GameOrchestrator orchestrator;
  private Arena alpha;
  private Arena bravo;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
    world = server.addSimpleWorld("world");
    plugin = MockBukkit.load(SumoPlugin.class);
    orchestrator = new GameOrchestrator(plugin, new InventoryStore(), new SessionRegistry());
    alpha = arenaAt("alpha", world, 100);
    bravo = arenaAt("bravo", world, 200);
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void joiningCreatesSessionPerArena() {
    PlayerMock a = server.addPlayer();
    PlayerMock b = server.addPlayer();
    assertTrue(orchestrator.join(alpha, a));
    assertTrue(orchestrator.join(bravo, b));
    assertEquals(2, orchestrator.activeSessions().size());
  }

  @Test
  void cannotJoinTwoArenas() {
    PlayerMock a = server.addPlayer();
    assertTrue(orchestrator.join(alpha, a));
    assertFalse(orchestrator.join(bravo, a));
  }

  private Arena arenaAt(String id, WorldMock w, int x) {
    return Arena.builder()
        .id(id)
        .spawnA(new Location(w, x, 64, 5))
        .spawnB(new Location(w, x, 64, -5))
        .lobby(new Location(w, x, 80, 0))
        .bounds(ArenaBounds.cylinder(new Location(w, x, 64, 0), 10.0))
        .knockback(new KnockbackConfig(1.0, 0.4, 0.5))
        .minPlayers(2)
        .maxPlayers(4)
        .build();
  }
}
