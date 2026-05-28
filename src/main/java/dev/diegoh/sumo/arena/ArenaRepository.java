package dev.diegoh.sumo.arena;

import dev.diegoh.sumo.config.LocationCodec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ArenaRepository {
    private final Path directory;
    private final Server server;
    private final Map<String, Arena> cache = new HashMap<>();

    public ArenaRepository(Path directory, Server server) {
        this.directory = directory;
        this.server = server;
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create arenas directory: " + directory, e);
        }
    }

    public void loadAll() {
        cache.clear();
        File[] files = directory.toFile().listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return;
        for (File f : files) {
            readArena(f).ifPresent(a -> cache.put(a.id(), a));
        }
    }

    public void save(Arena arena) {
        cache.put(arena.id(), arena);
        File file = directory.resolve(arena.id() + ".yml").toFile();
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("id", arena.id());
        cfg.set("spawn-a", LocationCodec.encode(arena.spawnA()));
        cfg.set("spawn-b", LocationCodec.encode(arena.spawnB()));
        cfg.set("lobby", LocationCodec.encode(arena.lobby()));
        Location boundsCenter =
                new Location(
                        arena.bounds().world(),
                        arena.bounds().centerX(),
                        arena.lobby().getY(),
                        arena.bounds().centerZ());
        cfg.set("bounds.center", LocationCodec.encode(boundsCenter));
        cfg.set("bounds.radius", arena.bounds().radius());
        cfg.set("knockback.strength", arena.knockback().strength());
        cfg.set("knockback.vertical-boost", arena.knockback().verticalBoost());
        cfg.set("knockback.friction", arena.knockback().friction());
        cfg.set("min-players", arena.minPlayers());
        cfg.set("max-players", arena.maxPlayers());
        try {
            cfg.save(file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save arena " + arena.id(), e);
        }
    }

    public Optional<Arena> find(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    public Collection<Arena> all() {
        return Collections.unmodifiableCollection(cache.values());
    }

    public void delete(String id) {
        cache.remove(id);
        File file = directory.resolve(id + ".yml").toFile();
        if (file.exists() && !file.delete()) {
            throw new IllegalStateException("Failed to delete arena file " + file);
        }
    }

    private Optional<Arena> readArena(File f) {
        try {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            String id = cfg.getString("id");
            Optional<Location> sa = LocationCodec.decode(cfg.getString("spawn-a"), server);
            Optional<Location> sb = LocationCodec.decode(cfg.getString("spawn-b"), server);
            Optional<Location> lo = LocationCodec.decode(cfg.getString("lobby"), server);
            Optional<Location> bc = LocationCodec.decode(cfg.getString("bounds.center"), server);
            if (id == null || sa.isEmpty() || sb.isEmpty() || lo.isEmpty() || bc.isEmpty())
                return Optional.empty();
            double radius = cfg.getDouble("bounds.radius", 10.0);
            KnockbackConfig kb =
                    new KnockbackConfig(
                            cfg.getDouble("knockback.strength", 1.0),
                            cfg.getDouble("knockback.vertical-boost", 0.4),
                            cfg.getDouble("knockback.friction", 0.5));
            return Optional.of(
                    Arena.builder()
                            .id(id)
                            .spawnA(sa.get())
                            .spawnB(sb.get())
                            .lobby(lo.get())
                            .bounds(ArenaBounds.cylinder(bc.get(), radius))
                            .knockback(kb)
                            .minPlayers(cfg.getInt("min-players", 2))
                            .maxPlayers(cfg.getInt("max-players", 8))
                            .build());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
