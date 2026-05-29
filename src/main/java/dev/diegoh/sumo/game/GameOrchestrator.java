package dev.diegoh.sumo.game;

import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.player.InventoryStore;
import dev.diegoh.sumo.player.SessionRegistry;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Owns every live {@link GameSession} at once — one per arena.
 *
 * <p>This is the single place the rest of the plugin asks "is this player in a game, and which
 * one?". A session is created lazily the first time someone joins an arena and removed when it
 * empties. A player can only be in one session at a time, enforced through {@link SessionRegistry}.
 */
public final class GameOrchestrator {
  private final Plugin plugin;
  private final InventoryStore inventoryStore;
  private final SessionRegistry registry;
  private final ConcurrentHashMap<String, GameSession> byArena = new ConcurrentHashMap<>();
  private Consumer<GameSession> onSessionCreated = s -> {};

  public GameOrchestrator(Plugin plugin, InventoryStore inventoryStore, SessionRegistry registry) {
    this.plugin = plugin;
    this.inventoryStore = inventoryStore;
    this.registry = registry;
  }

  public void setOnSessionCreated(Consumer<GameSession> hook) {
    this.onSessionCreated = hook == null ? s -> {} : hook;
  }

  public boolean join(Arena arena, Player player) {
    if (registry.find(player.getUniqueId()).isPresent()) return false;
    GameSession session =
        byArena.computeIfAbsent(
            arena.id(),
            id -> {
              GameSession s = new GameSession(plugin, arena, inventoryStore);
              onSessionCreated.accept(s);
              return s;
            });
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
