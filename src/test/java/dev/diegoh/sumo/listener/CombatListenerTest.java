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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CombatListenerTest {
  private ServerMock server;
  private WorldMock world;
  private SumoPlugin plugin;
  private GameOrchestrator orchestrator;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
    world = server.addSimpleWorld("world");
    plugin = MockBukkit.load(SumoPlugin.class);
    orchestrator = new GameOrchestrator(plugin, new InventoryStore(), new SessionRegistry());
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void damageBetweenNonGamePlayersIsNotTouched() {
    // Players who aren't in a Sumo game must keep normal combat — the listener ignores them.
    CombatListener listener = new CombatListener(orchestrator);
    PlayerMock a = server.addPlayer();
    PlayerMock b = server.addPlayer();
    EntityDamageByEntityEvent event =
        new EntityDamageByEntityEvent(a, b, DamageCause.ENTITY_ATTACK, 1.0);
    listener.onHit(event);
    assertFalse(event.isCancelled());
  }

  @Test
  void damageToInGamePlayerFromMobIsCancelled() {
    Arena arena =
        Arena.builder()
            .id("main")
            .spawnA(new Location(world, 0, 64, 5))
            .spawnB(new Location(world, 0, 64, -5))
            .lobby(new Location(world, 0, 80, 0))
            .bounds(ArenaBounds.cylinder(new Location(world, 0, 64, 0), 15.0))
            .knockback(new KnockbackConfig(0.5, 0.35, 0.5))
            .minPlayers(2)
            .maxPlayers(4)
            .build();
    PlayerMock victim = server.addPlayer();
    orchestrator.join(arena, victim); // victim now in a session (WAITING)
    var zombie = world.spawn(new Location(world, 0, 80, 1), org.bukkit.entity.Zombie.class);
    EntityDamageByEntityEvent event =
        new EntityDamageByEntityEvent(zombie, victim, DamageCause.ENTITY_ATTACK, 4.0);

    new CombatListener(orchestrator).onHit(event);

    assertTrue(event.isCancelled());
  }
}
