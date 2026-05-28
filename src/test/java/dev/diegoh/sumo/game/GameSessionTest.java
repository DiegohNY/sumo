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
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameSessionTest {
  private ServerMock server;
  private WorldMock world;
  private SumoPlugin plugin;
  private Arena arena;
  private InventoryStore inv;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
    world = server.addSimpleWorld("world");
    plugin = MockBukkit.load(SumoPlugin.class);
    arena =
        Arena.builder()
            .id("main")
            .spawnA(new Location(world, 0, 64, 5))
            .spawnB(new Location(world, 0, 64, -5))
            .lobby(new Location(world, 0, 80, 0))
            .bounds(ArenaBounds.cylinder(new Location(world, 0, 64, 0), 15.0))
            .knockback(new KnockbackConfig(1.0, 0.4, 0.5))
            .minPlayers(2)
            .maxPlayers(4)
            .build();
    inv = new InventoryStore();
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void addPlayerTransitionsToWaiting() {
    GameSession s = new GameSession(plugin, arena, inv);
    PlayerMock a = server.addPlayer();
    assertTrue(s.addPlayer(a));
    assertEquals(GameState.WAITING, s.state());
    assertEquals(1, s.participantCount());
  }

  @Test
  void cannotAddBeyondMaxPlayers() {
    GameSession s = new GameSession(plugin, arena, inv);
    for (int i = 0; i < arena.maxPlayers(); i++) assertTrue(s.addPlayer(server.addPlayer()));
    assertFalse(s.addPlayer(server.addPlayer()));
  }

  @Test
  void startBelowMinPlayersFails() {
    GameSession s = new GameSession(plugin, arena, inv);
    s.addPlayer(server.addPlayer());
    assertFalse(s.startTournament());
    assertEquals(GameState.WAITING, s.state());
  }

  @Test
  void winnerOfFinalMatchTransitionsToEnding() {
    GameSession s = new GameSession(plugin, arena, inv);
    PlayerMock a = server.addPlayer();
    PlayerMock b = server.addPlayer();
    s.addPlayer(a);
    s.addPlayer(b);
    assertTrue(s.startTournament());
    s.skipCountdownForTesting();
    assertEquals(GameState.ACTIVE, s.state());
    s.recordElimination(b.getUniqueId());
    assertEquals(GameState.ENDING, s.state());
    assertEquals(a.getUniqueId(), s.winner().orElseThrow());
  }
}
