package dev.diegoh.sumo.game;

import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.player.InventoryStore;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Runs one tournament inside one {@link Arena}.
 *
 * <p>This is a small state machine. Players are added while the session is {@code WAITING}; calling
 * {@link #startTournament()} pairs the first two into a {@link Match} and moves through {@link
 * GameState} ({@code COUNTDOWN → ACTIVE → ENDING}). Each elimination (via {@link
 * #recordElimination(UUID)}) advances to the next match until one fighter remains — the winner.
 *
 * <p>Inventories are snapshotted on join and restored on removal through the shared {@link
 * InventoryStore}. Interested components (scoreboard, titles) can {@link #subscribe(Consumer)} to
 * {@link SessionEvent}s instead of polling.
 *
 * <p>Not thread-safe: all gameplay runs on the server main thread.
 */
public final class GameSession {
  private final Plugin plugin;
  private final Arena arena;
  private final InventoryStore inventoryStore;
  private final Deque<UUID> participants = new ArrayDeque<>();
  private final List<Consumer<SessionEvent>> subscribers = new CopyOnWriteArrayList<>();
  private final int countdownSeconds;
  private GameState state = GameState.IDLE;
  private Match currentMatch;
  private UUID winner;
  // Incremented on every match transition so a stale scheduled countdown task can detect that the
  // match it was started for is no longer current and skip flipping the state.
  private int matchEpoch;

  public GameSession(Plugin plugin, Arena arena, InventoryStore inventoryStore) {
    this(plugin, arena, inventoryStore, 5);
  }

  public GameSession(
      Plugin plugin, Arena arena, InventoryStore inventoryStore, int countdownSeconds) {
    this.plugin = plugin;
    this.arena = arena;
    this.inventoryStore = inventoryStore;
    this.countdownSeconds = Math.max(0, countdownSeconds);
  }

  public void subscribe(Consumer<SessionEvent> subscriber) {
    subscribers.add(subscriber);
  }

  public void unsubscribe(Consumer<SessionEvent> subscriber) {
    subscribers.remove(subscriber);
  }

  private void fire(SessionEvent event) {
    for (Consumer<SessionEvent> s : subscribers) {
      try {
        s.accept(event);
      } catch (Exception ex) {
        plugin.getLogger().warning("Session subscriber threw: " + ex.getMessage());
      }
    }
  }

  private void setState(GameState next) {
    if (state == next) return;
    GameState previous = state;
    state = next;
    fire(new SessionEvent.StateChanged(previous, next));
  }

  public boolean addPlayer(Player player) {
    if (state != GameState.IDLE && state != GameState.WAITING) return false;
    if (participants.size() >= arena.maxPlayers()) return false;
    if (participants.contains(player.getUniqueId())) return false;
    inventoryStore.capture(player);
    player.getInventory().clear();
    player.getInventory().setArmorContents(new org.bukkit.inventory.ItemStack[4]);
    player.teleport(arena.lobby());
    participants.add(player.getUniqueId());
    setState(GameState.WAITING);
    fire(new SessionEvent.PlayerJoined(player.getUniqueId()));
    return true;
  }

  public boolean removePlayer(UUID uuid) {
    boolean removed = participants.remove(uuid);
    Player p = Bukkit.getPlayer(uuid);
    if (p != null) inventoryStore.restore(p);
    if (removed) fire(new SessionEvent.PlayerLeft(uuid));
    if (currentMatch != null
        && (currentMatch.playerA().equals(uuid) || currentMatch.playerB().equals(uuid))) {
      UUID survivor =
          currentMatch.playerA().equals(uuid) ? currentMatch.playerB() : currentMatch.playerA();
      advanceAfterMatch(survivor, uuid, false);
    }
    return removed;
  }

  public boolean startTournament() {
    if (state != GameState.WAITING) return false;
    if (participants.size() < arena.minPlayers()) return false;
    setState(GameState.COUNTDOWN);
    startNextMatch();
    return true;
  }

  private void startNextMatch() {
    if (participants.size() < 2) {
      endTournament();
      return;
    }
    UUID a = participants.poll();
    UUID b = participants.poll();
    participants.addFirst(b);
    participants.addFirst(a);
    currentMatch = new Match(a, b);
    setState(GameState.COUNTDOWN);
    teleportToSpawns(a, b);
    fire(new SessionEvent.MatchStarted(a, b));
    scheduleCountdown();
  }

  /**
   * Schedules the COUNTDOWN → ACTIVE transition. Without this, players stay frozen forever because
   * movement is blocked during COUNTDOWN. The epoch guard ensures a task scheduled for an earlier
   * match (e.g. one cut short by a disconnect) cannot flip the state of a later one.
   */
  private void scheduleCountdown() {
    int epoch = ++matchEpoch;
    if (countdownSeconds <= 0) {
      onCountdownFinished();
      return;
    }
    fire(new SessionEvent.CountdownTick(countdownSeconds));
    scheduleCountdownTick(epoch, countdownSeconds);
  }

  private void scheduleCountdownTick(int epoch, int remaining) {
    plugin
        .getServer()
        .getScheduler()
        .runTaskLater(
            plugin,
            () -> {
              if (state != GameState.COUNTDOWN || epoch != matchEpoch) return;
              int left = remaining - 1;
              if (left <= 0) {
                onCountdownFinished();
              } else {
                fire(new SessionEvent.CountdownTick(left));
                scheduleCountdownTick(epoch, left);
              }
            },
            20L);
  }

  private void teleportToSpawns(UUID a, UUID b) {
    Player pa = Bukkit.getPlayer(a);
    Player pb = Bukkit.getPlayer(b);
    if (pa != null) pa.teleport(arena.spawnA());
    if (pb != null) pb.teleport(arena.spawnB());
  }

  public void skipCountdownForTesting() {
    setState(GameState.ACTIVE);
  }

  public void onCountdownFinished() {
    setState(GameState.ACTIVE);
  }

  public void recordElimination(UUID loser) {
    if (state != GameState.ACTIVE || currentMatch == null) return;
    UUID winnerId =
        currentMatch.playerA().equals(loser) ? currentMatch.playerB() : currentMatch.playerA();
    advanceAfterMatch(winnerId, loser, true);
  }

  private void advanceAfterMatch(UUID winnerId, UUID loserId, boolean eliminated) {
    // Remove the loser BEFORE firing, so listeners that re-render the scoreboard for the current
    // participants no longer include them (otherwise the loser's sidebar gets re-applied).
    participants.remove(loserId);
    participants.remove(winnerId);
    participants.addLast(winnerId);
    currentMatch = null;
    Player loser = Bukkit.getPlayer(loserId);
    if (loser != null) inventoryStore.restore(loser);
    if (eliminated) fire(new SessionEvent.PlayerEliminated(loserId, winnerId));
    if (participants.size() < 2) {
      endTournament();
    } else {
      startNextMatch();
    }
  }

  private void endTournament() {
    if (!participants.isEmpty()) winner = participants.peek();
    setState(GameState.ENDING);
    if (winner != null) fire(new SessionEvent.TournamentEnded(winner));
  }

  public List<UUID> participants() {
    return new ArrayList<>(participants);
  }

  public int participantCount() {
    return participants.size();
  }

  public GameState state() {
    return state;
  }

  public Arena arena() {
    return arena;
  }

  public Optional<Match> currentMatch() {
    return Optional.ofNullable(currentMatch);
  }

  public Optional<UUID> winner() {
    return Optional.ofNullable(winner);
  }
}
