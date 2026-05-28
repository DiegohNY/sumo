package dev.diegoh.sumo.i18n;

public enum MessageKey {
  PLUGIN_ENABLED("plugin.enabled"),
  PLUGIN_DISABLED("plugin.disabled"),
  NO_PERMISSION("error.no-permission"),
  PLAYERS_ONLY("error.players-only"),
  ARENA_NOT_FOUND("arena.not-found"),
  ARENA_CREATED("arena.created"),
  ARENA_DELETED("arena.deleted"),
  ARENA_LIST_HEADER("arena.list-header"),
  ARENA_LIST_ENTRY("arena.list-entry"),
  ARENA_SPAWN_SET("arena.spawn-set"),
  ARENA_LOBBY_SET("arena.lobby-set"),
  ARENA_BOUNDS_SET("arena.bounds-set"),
  ARENA_KB_SET("arena.kb-set"),
  JOIN_SUCCESS("join.success"),
  JOIN_ALREADY_IN_GAME("join.already-in-game"),
  JOIN_FULL("join.full"),
  LEAVE_SUCCESS("leave.success"),
  LEAVE_NOT_IN_GAME("leave.not-in-game"),
  QUEUE_JOINED("queue.joined"),
  QUEUE_LEFT("queue.left"),
  MATCH_STARTING("match.starting"),
  MATCH_COUNTDOWN("match.countdown"),
  MATCH_FIGHT("match.fight"),
  MATCH_WINNER("match.winner"),
  TOURNAMENT_WINNER("tournament.winner"),
  TOURNAMENT_END("tournament.end"),
  PLAYER_ELIMINATED("player.eliminated"),
  STATS_HEADER("stats.header"),
  STATS_LINE("stats.line"),
  SCOREBOARD_TITLE("scoreboard.title"),
  SCOREBOARD_ARENA("scoreboard.arena"),
  SCOREBOARD_PLAYERS("scoreboard.players"),
  HELP_HEADER("help.header"),
  HELP_LINE("help.line"),
  FORCE_START_OK("force.start-ok"),
  FORCE_START_FAIL("force.start-fail"),
  FORCE_STOP_OK("force.stop-ok"),
  RELOADED("plugin.reloaded"),
  INVALID_USAGE("error.invalid-usage");

  private final String path;

  MessageKey(String path) {
    this.path = path;
  }

  public String path() {
    return path;
  }
}
