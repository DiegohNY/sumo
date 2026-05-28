package dev.diegoh.sumo.queue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class MatchmakingQueue {
    private final String arenaId;
    private final LinkedHashSet<UUID> waiting = new LinkedHashSet<>();

    public MatchmakingQueue(String arenaId) {
        this.arenaId = arenaId;
    }

    public boolean add(UUID uuid) {
        return waiting.add(uuid);
    }

    public boolean remove(UUID uuid) {
        return waiting.remove(uuid);
    }

    public int position(UUID uuid) {
        int i = 0;
        for (UUID u : waiting) {
            i++;
            if (u.equals(uuid)) return i;
        }
        return -1;
    }

    public int size() {
        return waiting.size();
    }

    public List<UUID> drain(int max) {
        List<UUID> out = new ArrayList<>(Math.min(max, waiting.size()));
        Iterator<UUID> it = waiting.iterator();
        while (it.hasNext() && out.size() < max) {
            out.add(it.next());
            it.remove();
        }
        return out;
    }

    public String arenaId() {
        return arenaId;
    }
}
