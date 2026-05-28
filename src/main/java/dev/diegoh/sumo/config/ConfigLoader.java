package dev.diegoh.sumo.config;

import dev.diegoh.sumo.stats.DatabaseDriver;
import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigLoader {
  private ConfigLoader() {}

  public static PluginConfig load(FileConfiguration cfg) {
    return new PluginConfig(
        cfg.getString("locale.default", "en_US"),
        cfg.getBoolean("locale.follow-player-locale", true),
        DatabaseDriver.valueOf(cfg.getString("storage.driver", "sqlite").toUpperCase()),
        cfg.getString("storage.sqlite-file", "stats.db"),
        cfg.getString("storage.mysql.host", "localhost"),
        cfg.getInt("storage.mysql.port", 3306),
        cfg.getString("storage.mysql.database", "sumo"),
        cfg.getString("storage.mysql.username", "sumo"),
        cfg.getString("storage.mysql.password", ""),
        cfg.getBoolean("storage.mysql.use-ssl", false),
        cfg.getInt("storage.pool.maximum-pool-size", 8),
        cfg.getInt("storage.pool.minimum-idle", 2),
        cfg.getLong("storage.pool.connection-timeout-ms", 5000L),
        cfg.getInt("defaults.min-players", 2),
        cfg.getInt("defaults.max-players", 8),
        cfg.getDouble("defaults.knockback.strength", 1.0),
        cfg.getDouble("defaults.knockback.vertical-boost", 0.4),
        cfg.getDouble("defaults.knockback.friction", 0.5),
        cfg.getBoolean("scoreboard.enabled", true),
        cfg.getBoolean("gui.enabled", true));
  }
}
