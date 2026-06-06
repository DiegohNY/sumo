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
  private final int countdownSeconds;
  private final int endDelaySeconds;
  private final ConcurrentHashMap<String, GameSession> byArena = new ConcurrentHashMap<>();
  private Consumer<GameSession> onSessionCreated = s -> {};

  public GameOrchestrator(Plugin plugin, InventoryStore inventoryStore, SessionRegistry registry) {
    this(plugin, inventoryStore, registry, 5, 8);
  }

  public GameOrchestrator(
      Plugin plugin,
      InventoryStore inventoryStore,
      SessionRegistry registry,
      int countdownSeconds,
      int endDelaySeconds) {
    this.plugin = plugin;
    this.inventoryStore = inventoryStore;
    this.registry = registry;
    this.countdownSeconds = countdownSeconds;
    this.endDelaySeconds = endDelaySeconds;
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
              GameSession s = new GameSession(plugin, arena, inventoryStore, countdownSeconds);
              s.subscribe(event -> onSessionEvent(s, event));
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

  /**
   * Frees players and tears the session down at the right moments. An eliminated player leaves the
   * registry immediately; when the tournament ends, the winner is restored and the session removed
   * (after a short delay so the victory is visible) so the arena is fresh for the next game.
   */
  private void onSessionEvent(GameSession session, SessionEvent event) {
    if (event instanceof SessionEvent.PlayerEliminated e) {
      releasePlayer(e.player());
    } else if (event instanceof SessionEvent.TournamentEnded) {
      long delayTicks = Math.max(0L, endDelaySeconds) * 20L;
      plugin.getServer().getScheduler().runTaskLater(plugin, () -> endSession(session), delayTicks);
    }
  }

  private void endSession(GameSession session) {
    for (var uuid : session.participants()) {
      Player p = plugin.getServer().getPlayer(uuid);
      if (p != null) inventoryStore.restore(p);
      releasePlayer(uuid);
    }
    byArena.remove(session.arena().id());
  }

  /** Unbinds a player and clears their Sumo sidebar so they return to a normal state. */
  private void releasePlayer(java.util.UUID uuid) {
    registry.unbind(uuid);
    Player p = plugin.getServer().getPlayer(uuid);
    if (p != null && plugin.getServer().getScoreboardManager() != null) {
      p.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
    }
  }

  /** Restores every player still in a game and clears all sessions. Called on plugin disable. */
  public void shutdownAll() {
    for (GameSession session : byArena.values()) {
      for (var uuid : session.participants()) {
        Player p = plugin.getServer().getPlayer(uuid);
        if (p != null) inventoryStore.restore(p);
        releasePlayer(uuid);
      }
    }
    byArena.clear();
  }
}
