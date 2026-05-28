package dev.diegoh.sumo.gui;

import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaService;
import dev.diegoh.sumo.game.GameOrchestrator;
import dev.diegoh.sumo.game.GameState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Renders a chest-style inventory of arenas. Click → join. State of each arena is rendered in the
 * item's lore.
 */
public final class ArenaSelectorGui {
  static final String TITLE = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Sumo arenas";

  private final ArenaService arenas;
  private final GameOrchestrator orchestrator;
  private final Map<UUID, Map<Integer, String>> slotToArena = new HashMap<>();

  public ArenaSelectorGui(ArenaService arenas, GameOrchestrator orchestrator) {
    this.arenas = arenas;
    this.orchestrator = orchestrator;
  }

  public void open(Player player) {
    Collection<Arena> all = arenas.all();
    int rows = Math.max(1, (int) Math.ceil(all.size() / 9.0));
    int size = Math.min(54, rows * 9);
    Inventory inv = Bukkit.createInventory(player, size, TITLE);

    Map<Integer, String> mapping = new HashMap<>();
    int slot = 0;
    List<Arena> sorted = new ArrayList<>(all);
    sorted.sort((a, b) -> a.id().compareTo(b.id()));
    for (Arena arena : sorted) {
      if (slot >= size) break;
      inv.setItem(slot, buildItem(arena));
      mapping.put(slot, arena.id());
      slot++;
    }
    slotToArena.put(player.getUniqueId(), mapping);
    player.openInventory(inv);
  }

  String arenaForSlot(UUID viewer, int slot) {
    Map<Integer, String> mapping = slotToArena.get(viewer);
    return mapping == null ? null : mapping.get(slot);
  }

  void cleanup(UUID viewer) {
    slotToArena.remove(viewer);
  }

  private ItemStack buildItem(Arena arena) {
    GameState state =
        orchestrator.sessionForArena(arena.id()).map(s -> s.state()).orElse(GameState.IDLE);
    int count = orchestrator.sessionForArena(arena.id()).map(s -> s.participantCount()).orElse(0);

    Material material =
        switch (state) {
          case ACTIVE -> Material.RED_WOOL;
          case COUNTDOWN -> Material.YELLOW_WOOL;
          case WAITING -> Material.LIME_WOOL;
          case ENDING -> Material.ORANGE_WOOL;
          case IDLE -> Material.WHITE_WOOL;
        };

    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + arena.id());
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.GRAY + "State: " + ChatColor.YELLOW + state.name());
      lore.add(
          ChatColor.GRAY
              + "Players: "
              + ChatColor.GREEN
              + count
              + ChatColor.GRAY
              + " / "
              + arena.maxPlayers());
      lore.add("");
      lore.add(ChatColor.AQUA + "Click to join");
      meta.setLore(lore);
      item.setItemMeta(meta);
    }
    return item;
  }
}
