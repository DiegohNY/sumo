package dev.diegoh.sumo.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class InventoryStore {

    private record Snapshot(
            ItemStack[] contents, ItemStack[] armor, GameMode mode, Location location) {}

    private final Map<UUID, Snapshot> snapshots = new HashMap<>();

    public void capture(Player player) {
        snapshots.put(
                player.getUniqueId(),
                new Snapshot(
                        player.getInventory().getContents().clone(),
                        player.getInventory().getArmorContents().clone(),
                        player.getGameMode(),
                        player.getLocation().clone()));
    }

    public void restore(Player player) {
        Snapshot snap = snapshots.remove(player.getUniqueId());
        if (snap == null) return;
        player.getInventory().setContents(snap.contents());
        player.getInventory().setArmorContents(snap.armor());
        player.setGameMode(snap.mode());
        player.teleport(snap.location());
    }

    public boolean has(UUID uuid) {
        return snapshots.containsKey(uuid);
    }
}
