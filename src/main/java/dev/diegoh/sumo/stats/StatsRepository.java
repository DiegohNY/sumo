package dev.diegoh.sumo.stats;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StatsRepository extends AutoCloseable {
  CompletableFuture<PlayerStats> load(UUID uuid);

  CompletableFuture<Void> save(PlayerStats stats);

  /** The top players ordered by wins (descending), at most {@code limit} rows. */
  CompletableFuture<List<PlayerStats>> topByWins(int limit);

  void init();

  @Override
  void close();
}
