package dev.diegoh.sumo.listener;

import dev.diegoh.sumo.game.GameOrchestrator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public final class ProtectionListener implements Listener {
  private final GameOrchestrator orchestrator;

  public ProtectionListener(GameOrchestrator orchestrator) {
    this.orchestrator = orchestrator;
  }

  @EventHandler
  public void onBreak(BlockBreakEvent event) {
    if (orchestrator.sessionOf(event.getPlayer()).isPresent()) event.setCancelled(true);
  }

  @EventHandler
  public void onDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player p)) return;
    if (orchestrator.sessionOf(p).isEmpty()) return;
    switch (event.getCause()) {
      case FALL, VOID, LAVA, FIRE, FIRE_TICK, DROWNING, SUFFOCATION, STARVATION ->
          event.setCancelled(true);
      default -> {}
    }
  }

  @EventHandler
  public void onFood(FoodLevelChangeEvent event) {
    if (!(event.getEntity() instanceof Player p)) return;
    if (orchestrator.sessionOf(p).isPresent()) {
      event.setCancelled(true);
      p.setFoodLevel(20);
      p.setSaturation(20f);
    }
  }
}
