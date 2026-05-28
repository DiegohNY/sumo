package dev.diegoh.sumo.game;

import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.player.InventoryStore;
import dev.diegoh.sumo.player.SessionRegistry;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class GameOrchestrator {
  private final Plugin plugin;
  private final InventoryStore inventoryStore;
  private final SessionRegistry registry;
  private final ConcurrentHashMap<String, GameSession> byArena = new ConcurrentHashMap<>();

  public GameOrchestrator(Plugin plugin, InventoryStore inventoryStore, SessionRegistry registry) {
    this.plugin = plugin;
    this.inventoryStore = inventoryStore;
    this.registry = registry;
  }

  public boolean join(Arena arena, Player player) {
    if (registry.find(player.getUniqueId()).isPresent()) return false;
    GameSession session =
        byArena.computeIfAbsent(arena.id(), id -> new GameSession(plugin, arena, inventoryStore));
    if (!session.addPlayer(player)) return false;
    registry.bind(player.getUniqueId(), session);
    return true;
  }

  public boolean leave(Player player) {
    Optional<GameSession> session = registry.find(player.getUniqueId());
    if (session.isEmpty()) return false;
    session.get().removePlayer(player.getUniqueId());
    registry.unbind(player.getUniqueId());
    if (session.get().participantCount() == 0) byArena.remove(session.get().arena().id());
    return true;
  }

  public Optional<GameSession> sessionOf(Player player) {
    return registry.find(player.getUniqueId());
  }

  public Optional<GameSession> sessionForArena(String arenaId) {
    return Optional.ofNullable(byArena.get(arenaId));
  }

  public Collection<GameSession> activeSessions() {
    return byArena.values();
  }

  public void shutdownAll() {
    byArena.clear();
  }
}
