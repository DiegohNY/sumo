package dev.diegoh.sumo.ui;

import dev.diegoh.sumo.game.GameSession;
import dev.diegoh.sumo.game.SessionEvent;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import dev.diegoh.sumo.scoreboard.ArenaScoreboard;
import dev.diegoh.sumo.util.AdventureUtil;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Subscribes to a session and pushes Adventure feedback (titles, action bars, scoreboard) to its
 * participants. One instance per active session.
 */
public final class SessionUiPresenter {
  private final GameSession session;
  private final Messages messages;
  private final Locale locale;
  private final AdventureUtil adventure;
  private final ArenaScoreboard scoreboard;
  private final boolean scoreboardEnabled;

  public SessionUiPresenter(
      GameSession session,
      Messages messages,
      Locale locale,
      AdventureUtil adventure,
      boolean scoreboardEnabled) {
    this.session = session;
    this.messages = messages;
    this.locale = locale;
    this.adventure = adventure;
    this.scoreboardEnabled = scoreboardEnabled;
    this.scoreboard = scoreboardEnabled ? new ArenaScoreboard(messages, locale) : null;
  }

  public void attach() {
    session.subscribe(this::handle);
  }

  private void handle(SessionEvent event) {
    if (event instanceof SessionEvent.PlayerJoined j) {
      onJoin(j.player());
      refreshScoreboard();
    } else if (event instanceof SessionEvent.PlayerLeft l) {
      onLeave(l.player());
      refreshScoreboard();
    } else if (event instanceof SessionEvent.MatchStarted m) {
      onMatchStarted(m.playerA(), m.playerB());
    } else if (event instanceof SessionEvent.CountdownTick c) {
      onCountdownTick(c.secondsLeft());
    } else if (event instanceof SessionEvent.StateChanged s) {
      if (s.next() == dev.diegoh.sumo.game.GameState.ACTIVE) onFight();
      refreshScoreboard();
    } else if (event instanceof SessionEvent.PlayerEliminated e) {
      onEliminated(e.player(), e.matchWinner());
      refreshScoreboard();
    } else if (event instanceof SessionEvent.TournamentEnded t) {
      onTournamentEnded(t.winner());
    }
  }

  private void onJoin(UUID uuid) {
    Player p = Bukkit.getPlayer(uuid);
    if (p == null) return;
    if (scoreboard != null) scoreboard.apply(p);
    audience(p)
        .sendActionBar(
            messages.get(
                locale, MessageKey.JOIN_SUCCESS, Placeholder.parsed("id", session.arena().id())));
  }

  private void onLeave(UUID uuid) {
    if (scoreboard != null) scoreboard.clear(uuid);
  }

  private void onMatchStarted(UUID a, UUID b) {
    Player pa = Bukkit.getPlayer(a);
    Player pb = Bukkit.getPlayer(b);
    String nameA = pa != null ? pa.getName() : a.toString();
    String nameB = pb != null ? pb.getName() : b.toString();
    Component msg =
        messages.get(
            locale,
            MessageKey.MATCH_STARTING,
            Placeholder.parsed("player_a", nameA),
            Placeholder.parsed("player_b", nameB));
    for (Player p : participantsPlayers()) {
      audience(p).sendMessage(msg);
    }
  }

  private void onCountdownTick(int secondsLeft) {
    Component num =
        messages.get(
            locale,
            MessageKey.MATCH_COUNTDOWN,
            Placeholder.parsed("seconds", String.valueOf(secondsLeft)));
    // Stay just under one second so each number clears before the next and the screen is readable.
    Title.Times times =
        Title.Times.times(Duration.ZERO, Duration.ofMillis(700), Duration.ofMillis(150));
    Title title = Title.title(Component.empty(), num, times);
    for (Player p : participantsPlayers()) {
      audience(p).showTitle(title);
    }
  }

  private void onFight() {
    Component fight = messages.get(locale, MessageKey.MATCH_FIGHT);
    // Quick flash so it doesn't block the view once the round is live.
    Title.Times times =
        Title.Times.times(Duration.ZERO, Duration.ofMillis(400), Duration.ofMillis(200));
    Title fightTitle = Title.title(fight, Component.empty(), times);
    for (Player p : participantsPlayers()) {
      audience(p).showTitle(fightTitle);
    }
  }

  private void onEliminated(UUID loser, UUID winnerId) {
    Player loserPlayer = Bukkit.getPlayer(loser);
    Player winnerPlayer = Bukkit.getPlayer(winnerId);
    String loserName = loserPlayer != null ? loserPlayer.getName() : loser.toString();
    String winnerName = winnerPlayer != null ? winnerPlayer.getName() : winnerId.toString();
    Component msg =
        messages.get(
            locale,
            MessageKey.MATCH_WINNER,
            Placeholder.parsed("winner", winnerName),
            Placeholder.parsed("loser", loserName));
    for (Player p : Bukkit.getOnlinePlayers()) {
      audience(p).sendMessage(msg);
    }
    if (loserPlayer != null) {
      audience(loserPlayer)
          .sendActionBar(
              messages.get(
                  locale, MessageKey.PLAYER_ELIMINATED, Placeholder.parsed("player", loserName)));
    }
  }

  private void onTournamentEnded(UUID winnerId) {
    Player winner = Bukkit.getPlayer(winnerId);
    String name = winner != null ? winner.getName() : winnerId.toString();
    Component msg =
        messages.get(locale, MessageKey.TOURNAMENT_WINNER, Placeholder.parsed("player", name));
    for (Player p : Bukkit.getOnlinePlayers()) {
      audience(p).sendMessage(msg);
    }
    Title times =
        Title.title(
            msg,
            Component.empty(),
            Title.Times.times(
                Duration.ofMillis(200), Duration.ofSeconds(3), Duration.ofMillis(500)));
    if (winner != null) audience(winner).showTitle(times);
  }

  private void refreshScoreboard() {
    if (!scoreboardEnabled || scoreboard == null) return;
    scoreboard.render(session, messages, locale);
    for (Player p : participantsPlayers()) scoreboard.apply(p);
  }

  private Iterable<Player> participantsPlayers() {
    return session.participants().stream().map(Bukkit::getPlayer).filter(p -> p != null).toList();
  }

  private Audience audience(Player p) {
    return adventure.audiences().player(p);
  }
}
