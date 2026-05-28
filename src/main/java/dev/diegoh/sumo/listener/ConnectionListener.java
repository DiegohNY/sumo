package dev.diegoh.sumo.listener;

import dev.diegoh.sumo.game.GameOrchestrator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ConnectionListener implements Listener {
  private final GameOrchestrator orchestrator;

  public ConnectionListener(GameOrchestrator orchestrator) {
    this.orchestrator = orchestrator;
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    orchestrator.leave(event.getPlayer());
  }
}
