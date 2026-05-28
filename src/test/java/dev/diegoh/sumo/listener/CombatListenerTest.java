package dev.diegoh.sumo.listener;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.diegoh.sumo.SumoPlugin;
import dev.diegoh.sumo.game.GameOrchestrator;
import dev.diegoh.sumo.player.InventoryStore;
import dev.diegoh.sumo.player.SessionRegistry;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CombatListenerTest {
  private ServerMock server;
  private SumoPlugin plugin;
  private GameOrchestrator orchestrator;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
    server.addSimpleWorld("world");
    plugin = MockBukkit.load(SumoPlugin.class);
    orchestrator = new GameOrchestrator(plugin, new InventoryStore(), new SessionRegistry());
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void damageBetweenNonGamePlayersIsCancelled() {
    CombatListener listener = new CombatListener(orchestrator);
    PlayerMock a = server.addPlayer();
    PlayerMock b = server.addPlayer();
    EntityDamageByEntityEvent event =
        new EntityDamageByEntityEvent(a, b, DamageCause.ENTITY_ATTACK, 1.0);
    listener.onHit(event);
    assertTrue(event.isCancelled());
  }
}
