package dev.diegoh.sumo.player;

import dev.diegoh.sumo.game.GameSession;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionRegistry {
  private final ConcurrentHashMap<UUID, GameSession> bySession = new ConcurrentHashMap<>();

  public void bind(UUID playerUuid, GameSession session) {
    bySession.put(playerUuid, session);
  }

  public void unbind(UUID playerUuid) {
    bySession.remove(playerUuid);
  }

  public Optional<GameSession> find(UUID playerUuid) {
    return Optional.ofNullable(bySession.get(playerUuid));
  }
}
