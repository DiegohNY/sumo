package dev.diegoh.sumo.stats;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StatsRepository extends AutoCloseable {
    CompletableFuture<PlayerStats> load(UUID uuid);

    CompletableFuture<Void> save(PlayerStats stats);

    void init();

    @Override
    void close();
}
