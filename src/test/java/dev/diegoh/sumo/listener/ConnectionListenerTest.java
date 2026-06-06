package dev.diegoh.sumo.listener;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.diegoh.sumo.SumoPlugin;
import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaBounds;
import dev.diegoh.sumo.arena.KnockbackConfig;
import dev.diegoh.sumo.game.GameOrchestrator;
import dev.diegoh.sumo.player.InventoryStore;
import dev.diegoh.sumo.player.SessionRegistry;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConnectionListenerTest {
  private ServerMock server;
  private WorldMock world;
  private SumoPlugin plugin;
  private GameOrchestrator orchestrator;
  private InventoryStore inventoryStore;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
    world = server.addSimpleWorld("world");
    plugin = MockBukkit.load(SumoPlugin.class);
    inventoryStore = new InventoryStore();
    orchestrator = new GameOrchestrator(plugin, inventoryStore, new SessionRegistry());
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void quitRemovesPlayerFromSession() {
    Arena arena =
        Arena.builder()
            .id("main")
            .spawnA(new Location(world, 0, 64, 5))
            .spawnB(new Location(world, 0, 64, -5))
            .lobby(new Location(world, 0, 80, 0))
            .bounds(ArenaBounds.cylinder(new Location(world, 0, 64, 0), 10.0))
            .knockback(new KnockbackConfig(1.0, 0.4, 0.5))
            .minPlayers(2)
            .maxPlayers(4)
            .build();
    PlayerMock p = server.addPlayer();
    assertTrue(orchestrator.join(arena, p));
    assertTrue(orchestrator.sessionOf(p).isPresent());

    ConnectionListener listener = new ConnectionListener(orchestrator, inventoryStore);
    listener.onQuit(new PlayerQuitEvent(p, (String) null));

    assertTrue(orchestrator.sessionOf(p).isEmpty());
  }

  @Test
  void quitForPlayerNotInGameIsHarmless() {
    ConnectionListener listener = new ConnectionListener(orchestrator, inventoryStore);
    PlayerMock p = server.addPlayer();
    assertDoesNotThrow(() -> listener.onQuit(new PlayerQuitEvent(p, (String) null)));
  }
}
