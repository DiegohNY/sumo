package dev.diegoh.sumo.queue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class QueueService {
    private final ConcurrentHashMap<String, MatchmakingQueue> queues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> currentQueue = new ConcurrentHashMap<>();

    public int join(String arenaId, UUID uuid) {
        if (currentQueue.containsKey(uuid)) return -1;
        MatchmakingQueue q = queues.computeIfAbsent(arenaId, MatchmakingQueue::new);
        if (!q.add(uuid)) return -1;
        currentQueue.put(uuid, arenaId);
        return q.position(uuid);
    }

    public boolean leave(UUID uuid) {
        String arenaId = currentQueue.remove(uuid);
        if (arenaId == null) return false;
        MatchmakingQueue q = queues.get(arenaId);
        return q != null && q.remove(uuid);
    }

    public int size(String arenaId) {
        MatchmakingQueue q = queues.get(arenaId);
        return q == null ? 0 : q.size();
    }

    public List<UUID> drain(String arenaId, int max) {
        MatchmakingQueue q = queues.get(arenaId);
        if (q == null) return List.of();
        List<UUID> drained = q.drain(max);
        drained.forEach(currentQueue::remove);
        return drained;
    }
}
