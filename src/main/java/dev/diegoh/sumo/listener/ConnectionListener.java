package dev.diegoh.sumo.listener;

import dev.diegoh.sumo.game.GameOrchestrator;
import dev.diegoh.sumo.player.InventoryStore;
import dev.diegoh.sumo.stats.StatsService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ConnectionListener implements Listener {
  private final GameOrchestrator orchestrator;
  private final InventoryStore inventoryStore;
  private final StatsService stats;

  public ConnectionListener(
      GameOrchestrator orchestrator, InventoryStore inventoryStore, StatsService stats) {
    this.orchestrator = orchestrator;
    this.inventoryStore = inventoryStore;
    this.stats = stats;
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    orchestrator.leave(event.getPlayer());
    // Drop the cached stats so the in-memory map doesn't grow unbounded over a long uptime.
    stats.invalidate(event.getPlayer().getUniqueId());
  }

  /**
   * If a player disconnected mid-game their inventory snapshot was kept (it can't be applied while
   * offline). Restore it when they come back so their items are never lost.
   */
  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    if (inventoryStore.has(event.getPlayer().getUniqueId())) {
      inventoryStore.restore(event.getPlayer());
    }
  }
}
