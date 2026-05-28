package dev.diegoh.sumo.gui;

import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaService;
import dev.diegoh.sumo.game.GameOrchestrator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public final class MenuListener implements Listener {
  private final ArenaSelectorGui gui;
  private final ArenaService arenas;
  private final GameOrchestrator orchestrator;

  public MenuListener(ArenaSelectorGui gui, ArenaService arenas, GameOrchestrator orchestrator) {
    this.gui = gui;
    this.arenas = arenas;
    this.orchestrator = orchestrator;
  }

  @EventHandler
  public void onClick(InventoryClickEvent event) {
    if (event.getView().getTitle() == null
        || !event.getView().getTitle().equals(ArenaSelectorGui.TITLE)) {
      return;
    }
    event.setCancelled(true);
    if (!(event.getWhoClicked() instanceof Player p)) return;
    String arenaId = gui.arenaForSlot(p.getUniqueId(), event.getRawSlot());
    if (arenaId == null) return;
    Arena arena = arenas.find(arenaId).orElse(null);
    if (arena == null) return;
    p.closeInventory();
    orchestrator.join(arena, p);
  }

  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    if (event.getView().getTitle() != null
        && event.getView().getTitle().equals(ArenaSelectorGui.TITLE)) {
      gui.cleanup(event.getPlayer().getUniqueId());
    }
  }
}
