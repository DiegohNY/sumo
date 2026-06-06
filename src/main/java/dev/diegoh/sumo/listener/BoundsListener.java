package dev.diegoh.sumo.listener;

import dev.diegoh.sumo.game.GameOrchestrator;
import dev.diegoh.sumo.game.GameSession;
import dev.diegoh.sumo.game.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class BoundsListener implements Listener {
  private final GameOrchestrator orchestrator;

  public BoundsListener(GameOrchestrator orchestrator) {
    this.orchestrator = orchestrator;
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    if (event.getTo() == null) return;
    // Ignore pure head rotation / sub-block movement: nothing about bounds or water can change
    // unless the block the player stands in changes. Cheap guard before any session/block lookup.
    if (event.getFrom().getBlockX() == event.getTo().getBlockX()
        && event.getFrom().getBlockY() == event.getTo().getBlockY()
        && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
      return;
    }
    Player p = event.getPlayer();
    GameSession s = orchestrator.sessionOf(p).orElse(null);
    if (s == null) return;
    if (s.state() == GameState.COUNTDOWN) {
      event.setCancelled(true);
      return;
    }
    if (s.state() != GameState.ACTIVE) return;
    boolean outOfBounds = !s.arena().bounds().contains(event.getTo());
    boolean inWater = event.getTo().getBlock().getType() == Material.WATER;
    if (outOfBounds || inWater) {
      // recordElimination -> restore() already teleports the loser back to where they came from.
      // Don't add a second teleport here or it overrides the restore and dumps them at the lobby.
      s.recordElimination(p.getUniqueId());
    }
  }
}
