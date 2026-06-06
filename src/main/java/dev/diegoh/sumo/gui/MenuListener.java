package dev.diegoh.sumo.gui;

import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaService;
import dev.diegoh.sumo.game.GameOrchestrator;
import dev.diegoh.sumo.i18n.LocaleResolver;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import dev.diegoh.sumo.util.AdventureUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public final class MenuListener implements Listener {
  private final ArenaSelectorGui gui;
  private final ArenaService arenas;
  private final GameOrchestrator orchestrator;
  private final Messages messages;
  private final LocaleResolver localeResolver;
  private final AdventureUtil adventure;

  public MenuListener(
      ArenaSelectorGui gui,
      ArenaService arenas,
      GameOrchestrator orchestrator,
      Messages messages,
      LocaleResolver localeResolver,
      AdventureUtil adventure) {
    this.gui = gui;
    this.arenas = arenas;
    this.orchestrator = orchestrator;
    this.messages = messages;
    this.localeResolver = localeResolver;
    this.adventure = adventure;
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
    boolean alreadyInGame = orchestrator.sessionOf(p).isPresent();
    if (orchestrator.join(arena, p)) {
      adventure
          .audiences()
          .player(p)
          .sendMessage(
              messages.get(
                  localeResolver.resolve(p),
                  MessageKey.JOIN_SUCCESS,
                  Placeholder.parsed("id", arena.id())));
    } else {
      MessageKey reason = alreadyInGame ? MessageKey.JOIN_ALREADY_IN_GAME : MessageKey.JOIN_FULL;
      adventure.audiences().player(p).sendMessage(messages.get(localeResolver.resolve(p), reason));
    }
  }

  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    if (event.getView().getTitle() != null
        && event.getView().getTitle().equals(ArenaSelectorGui.TITLE)) {
      gui.cleanup(event.getPlayer().getUniqueId());
    }
  }
}
