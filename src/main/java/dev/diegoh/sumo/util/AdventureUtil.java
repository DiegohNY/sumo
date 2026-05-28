package dev.diegoh.sumo.util;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.Plugin;

public final class AdventureUtil implements AutoCloseable {
    private final BukkitAudiences audiences;

    public AdventureUtil(Plugin plugin) {
        this.audiences = BukkitAudiences.create(plugin);
    }

    public BukkitAudiences audiences() {
        return audiences;
    }

    @Override
    public void close() {
        audiences.close();
    }
}
