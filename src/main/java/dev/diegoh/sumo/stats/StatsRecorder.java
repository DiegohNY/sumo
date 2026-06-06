package dev.diegoh.sumo.stats;

import dev.diegoh.sumo.game.GameSession;
import dev.diegoh.sumo.game.SessionEvent;
import java.util.logging.Logger;

/**
 * Records a win and a loss for every finished match by listening to a session's {@link
 * SessionEvent} stream. One instance is attached per session; failures are logged and never
 * interrupt gameplay.
 */
public final class StatsRecorder {
  private final StatsService stats;
  private final Logger logger;

  public StatsRecorder(StatsService stats, Logger logger) {
    this.stats = stats;
    this.logger = logger;
  }

  public void attach(GameSession session) {
    session.subscribe(this::onEvent);
  }

  private void onEvent(SessionEvent event) {
    if (!(event instanceof SessionEvent.PlayerEliminated e)) return;
    stats
        .recordLoss(e.player())
        .exceptionally(
            ex -> {
              logger.warning("Failed to record loss for " + e.player() + ": " + ex.getMessage());
              return null;
            });
    stats
        .recordWin(e.matchWinner())
        .exceptionally(
            ex -> {
              logger.warning(
                  "Failed to record win for " + e.matchWinner() + ": " + ex.getMessage());
              return null;
            });
  }
}
