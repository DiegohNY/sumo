package dev.diegoh.sumo.stats;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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
        return get(uuid)
                .thenCompose(
                        s -> {
                            PlayerStats updated = s.withWin();
                            cache.put(uuid, updated);
                            return repository.save(updated).thenApply(v -> updated);
                        });
    }

    public CompletableFuture<PlayerStats> recordLoss(UUID uuid) {
        return get(uuid)
                .thenCompose(
                        s -> {
                            PlayerStats updated = s.withLoss();
                            cache.put(uuid, updated);
                            return repository.save(updated).thenApply(v -> updated);
                        });
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    public void shutdown() {
        cache.clear();
    }
}
