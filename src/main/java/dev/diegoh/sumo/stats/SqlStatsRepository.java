package dev.diegoh.sumo.stats;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SqlStatsRepository implements StatsRepository {
    private final HikariDataSource ds;
    private final DatabaseDriver driver;
    private final ExecutorService io;

    private SqlStatsRepository(HikariDataSource ds, DatabaseDriver driver) {
        this.ds = ds;
        this.driver = driver;
        this.io =
                Executors.newFixedThreadPool(
                        Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
                        r -> {
                            Thread t = new Thread(r, "sumo-stats-io");
                            t.setDaemon(true);
                            return t;
                        });
    }

    public static SqlStatsRepository sqlite(String jdbcUrl) {
        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName(DatabaseDriver.SQLITE.driverClass());
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setMaximumPoolSize(1);
        cfg.setPoolName("sumo-sqlite");
        return new SqlStatsRepository(new HikariDataSource(cfg), DatabaseDriver.SQLITE);
    }

    public static SqlStatsRepository mysql(
            String jdbcUrl,
            String username,
            String password,
            int maxPool,
            int minIdle,
            long timeoutMs) {
        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName(DatabaseDriver.MYSQL.driverClass());
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setUsername(username);
        cfg.setPassword(password);
        cfg.setMaximumPoolSize(maxPool);
        cfg.setMinimumIdle(minIdle);
        cfg.setConnectionTimeout(timeoutMs);
        cfg.setPoolName("sumo-mysql");
        return new SqlStatsRepository(new HikariDataSource(cfg), DatabaseDriver.MYSQL);
    }

    @Override
    public void init() {
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(schema())) {
            ps.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to init stats schema", e);
        }
    }

    private String schema() {
        return switch (driver) {
            case SQLITE -> """
                    CREATE TABLE IF NOT EXISTS player_stats (
                      uuid TEXT PRIMARY KEY,
                      wins INTEGER NOT NULL DEFAULT 0,
                      losses INTEGER NOT NULL DEFAULT 0,
                      current_streak INTEGER NOT NULL DEFAULT 0,
                      best_streak INTEGER NOT NULL DEFAULT 0,
                      total_games INTEGER NOT NULL DEFAULT 0,
                      last_played_ms INTEGER NOT NULL DEFAULT 0
                    );
                    """;
            case MYSQL -> """
                    CREATE TABLE IF NOT EXISTS player_stats (
                      uuid CHAR(36) PRIMARY KEY,
                      wins INT NOT NULL DEFAULT 0,
                      losses INT NOT NULL DEFAULT 0,
                      current_streak INT NOT NULL DEFAULT 0,
                      best_streak INT NOT NULL DEFAULT 0,
                      total_games INT NOT NULL DEFAULT 0,
                      last_played_ms BIGINT NOT NULL DEFAULT 0,
                      INDEX idx_wins (wins DESC),
                      INDEX idx_best_streak (best_streak DESC)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                    """;
        };
    }

    @Override
    public CompletableFuture<PlayerStats> load(UUID uuid) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try (Connection c = ds.getConnection();
                            PreparedStatement ps =
                                    c.prepareStatement(
                                            "SELECT wins,losses,current_streak,best_streak,total_games,last_played_ms"
                                                    + " FROM player_stats WHERE uuid=?")) {
                        ps.setString(1, uuid.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) return PlayerStats.empty(uuid);
                            return new PlayerStats(
                                    uuid,
                                    rs.getInt(1),
                                    rs.getInt(2),
                                    rs.getInt(3),
                                    rs.getInt(4),
                                    rs.getInt(5),
                                    rs.getLong(6));
                        }
                    } catch (SQLException e) {
                        throw new IllegalStateException("Failed to load stats for " + uuid, e);
                    }
                },
                io);
    }

    @Override
    public CompletableFuture<Void> save(PlayerStats stats) {
        return CompletableFuture.runAsync(
                () -> {
                    String sql =
                            switch (driver) {
                                case SQLITE -> """
                                INSERT INTO player_stats(uuid,wins,losses,current_streak,best_streak,total_games,last_played_ms)
                                VALUES(?,?,?,?,?,?,?)
                                ON CONFLICT(uuid) DO UPDATE SET
                                  wins=excluded.wins, losses=excluded.losses,
                                  current_streak=excluded.current_streak, best_streak=excluded.best_streak,
                                  total_games=excluded.total_games, last_played_ms=excluded.last_played_ms;
                                """;
                                case MYSQL -> """
                                INSERT INTO player_stats(uuid,wins,losses,current_streak,best_streak,total_games,last_played_ms)
                                VALUES(?,?,?,?,?,?,?)
                                ON DUPLICATE KEY UPDATE
                                  wins=VALUES(wins), losses=VALUES(losses),
                                  current_streak=VALUES(current_streak), best_streak=VALUES(best_streak),
                                  total_games=VALUES(total_games), last_played_ms=VALUES(last_played_ms);
                                """;
                            };
                    try (Connection c = ds.getConnection();
                            PreparedStatement ps = c.prepareStatement(sql)) {
                        ps.setString(1, stats.uuid().toString());
                        ps.setInt(2, stats.wins());
                        ps.setInt(3, stats.losses());
                        ps.setInt(4, stats.currentStreak());
                        ps.setInt(5, stats.bestStreak());
                        ps.setInt(6, stats.totalGames());
                        ps.setLong(7, stats.lastPlayedEpochMillis());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        throw new IllegalStateException(
                                "Failed to save stats for " + stats.uuid(), e);
                    }
                },
                io);
    }

    @Override
    public void close() {
        io.shutdown();
        ds.close();
    }
}
