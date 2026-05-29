package dev.diegoh.sumo.game;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import dev.diegoh.sumo.SumoPlugin;
import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaBounds;
import dev.diegoh.sumo.arena.KnockbackConfig;
import dev.diegoh.sumo.player.InventoryStore;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionEventTest {
  private ServerMock server;
  private WorldMock world;
  private SumoPlugin plugin;
  private Arena arena;

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
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void joinEmitsStateChangeAndPlayerJoined() {
    GameSession session = new GameSession(plugin, arena, new InventoryStore());
    List<SessionEvent> events = new ArrayList<>();
    session.subscribe(events::add);

    session.addPlayer(server.addPlayer());

    assertTrue(
        events.stream()
            .anyMatch(
                e ->
                    e instanceof SessionEvent.StateChanged sc
                        && sc.previous() == GameState.IDLE
                        && sc.next() == GameState.WAITING));
    assertTrue(events.stream().anyMatch(e -> e instanceof SessionEvent.PlayerJoined));
  }

  @Test
  void unsubscribeStopsDelivery() {
    GameSession session = new GameSession(plugin, arena, new InventoryStore());
    List<SessionEvent> events = new ArrayList<>();
    java.util.function.Consumer<SessionEvent> sub = events::add;
    session.subscribe(sub);
    session.unsubscribe(sub);

    session.addPlayer(server.addPlayer());

    assertTrue(events.isEmpty());
  }
}
