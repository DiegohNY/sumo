package dev.diegoh.sumo.game;

import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.player.InventoryStore;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class GameSession {
    private final Plugin plugin;
    private final Arena arena;
    private final InventoryStore inventoryStore;
    private final Deque<UUID> participants = new ArrayDeque<>();
    private GameState state = GameState.IDLE;
    private Match currentMatch;
    private UUID winner;

    public GameSession(Plugin plugin, Arena arena, InventoryStore inventoryStore) {
        this.plugin = plugin;
        this.arena = arena;
        this.inventoryStore = inventoryStore;
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
        state = GameState.WAITING;
        return true;
    }

    public boolean removePlayer(UUID uuid) {
        boolean removed = participants.remove(uuid);
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) inventoryStore.restore(p);
        if (currentMatch != null
                && (currentMatch.playerA().equals(uuid) || currentMatch.playerB().equals(uuid))) {
            UUID survivor =
                    currentMatch.playerA().equals(uuid)
                            ? currentMatch.playerB()
                            : currentMatch.playerA();
            advanceAfterMatch(survivor, uuid);
        }
        return removed;
    }

    public boolean startTournament() {
        if (state != GameState.WAITING) return false;
        if (participants.size() < arena.minPlayers()) return false;
        state = GameState.COUNTDOWN;
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
        state = GameState.COUNTDOWN;
        teleportToSpawns(a, b);
    }

    private void teleportToSpawns(UUID a, UUID b) {
        Player pa = Bukkit.getPlayer(a);
        Player pb = Bukkit.getPlayer(b);
        if (pa != null) pa.teleport(arena.spawnA());
        if (pb != null) pb.teleport(arena.spawnB());
    }

    public void skipCountdownForTesting() {
        state = GameState.ACTIVE;
    }

    public void onCountdownFinished() {
        state = GameState.ACTIVE;
    }

    public void recordElimination(UUID loser) {
        if (state != GameState.ACTIVE || currentMatch == null) return;
        UUID winnerId =
                currentMatch.playerA().equals(loser)
                        ? currentMatch.playerB()
                        : currentMatch.playerA();
        advanceAfterMatch(winnerId, loser);
    }

    private void advanceAfterMatch(UUID winnerId, UUID loserId) {
        participants.remove(loserId);
        participants.remove(winnerId);
        participants.addLast(winnerId);
        currentMatch = null;
        Player loser = Bukkit.getPlayer(loserId);
        if (loser != null) inventoryStore.restore(loser);
        if (participants.size() < 2) {
            endTournament();
        } else {
            startNextMatch();
        }
    }

    private void endTournament() {
        if (!participants.isEmpty()) winner = participants.peek();
        state = GameState.ENDING;
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
