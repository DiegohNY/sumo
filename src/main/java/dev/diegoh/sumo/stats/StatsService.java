package dev.diegoh.sumo.stats;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The front door to player statistics, with a small in-memory cache.
 *
 * <p>Reads hit the cache first and only fall through to the {@link StatsRepository} (database) on a
 * miss. Writes are write-through: the cache is updated immediately and the change is persisted
 * asynchronously. All methods return a {@link java.util.concurrent.CompletableFuture} so callers
 * never block the server main thread on I/O.
 */
public final class StatsService {
  private final StatsRepository repository;
  private final ConcurrentHashMap<UUID, PlayerStats> cache = new ConcurrentHashMap<>();

  public StatsService(StatsRepository repository) {
    this.repository = repository;
  }

  public CompletableFuture<PlayerStats> get(UUID uuid) {
    PlayerStats cached = cache.get(uuid);
    if (cached != null) return CompletableFuture.completedFuture(cached);
    return repository
        .load(uuid)
        .thenApply(
            s -> {
              cache.put(uuid, s);
              return s;
            });
  }

  public Optional<PlayerStats> getCached(UUID uuid) {
    return Optional.ofNullable(cache.get(uuid));
  }

  public CompletableFuture<PlayerStats> recordWin(UUID uuid) {
    return update(uuid, PlayerStats::withWin);
  }

  public CompletableFuture<PlayerStats> recordLoss(UUID uuid) {
    return update(uuid, PlayerStats::withLoss);
  }

  /**
   * Loads the player into the cache, then applies the change atomically with {@link
   * java.util.concurrent.ConcurrentHashMap#compute} so two near-simultaneous records can't read the
   * same base value and clobber each other (no lost updates).
   */
  private CompletableFuture<PlayerStats> update(
      UUID uuid, java.util.function.UnaryOperator<PlayerStats> change) {
    return get(uuid)
        .thenCompose(
            loaded -> {
              PlayerStats updated =
                  cache.compute(
                      uuid, (k, current) -> change.apply(current == null ? loaded : current));
              return repository.save(updated).thenApply(v -> updated);
            });
  }

  /** Top players by wins. Reads straight from storage (not cached). */
  public CompletableFuture<java.util.List<PlayerStats>> top(int limit) {
    return repository.topByWins(limit);
  }

  public void invalidate(UUID uuid) {
    cache.remove(uuid);
  }

  public void shutdown() {
    cache.clear();
  }
}
