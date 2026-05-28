package dev.diegoh.sumo.game;

import java.util.UUID;

public sealed interface SessionEvent {
  record PlayerJoined(UUID player) implements SessionEvent {}

  record PlayerLeft(UUID player) implements SessionEvent {}

  record StateChanged(GameState previous, GameState next) implements SessionEvent {}

  record MatchStarted(UUID playerA, UUID playerB) implements SessionEvent {}

  record PlayerEliminated(UUID player, UUID matchWinner) implements SessionEvent {}

  record TournamentEnded(UUID winner) implements SessionEvent {}
}
