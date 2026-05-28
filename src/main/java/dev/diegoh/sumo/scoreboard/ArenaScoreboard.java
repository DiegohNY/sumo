package dev.diegoh.sumo.scoreboard;

import dev.diegoh.sumo.game.GameSession;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public final class ArenaScoreboard {
    private final Scoreboard scoreboard;
    private final Objective objective;

    public ArenaScoreboard(Messages messages, Locale locale) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Component title = messages.get(locale, MessageKey.SCOREBOARD_TITLE);
        this.objective =
                scoreboard.registerNewObjective(
                        "sumo",
                        "dummy",
                        LegacyComponentSerializer.legacySection().serialize(title));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void render(GameSession session, Messages messages, Locale locale) {
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        String arenaLine =
                LegacyComponentSerializer.legacySection()
                        .serialize(
                                messages.get(
                                        locale,
                                        MessageKey.SCOREBOARD_ARENA,
                                        "id",
                                        session.arena().id()));
        String playersLine =
                LegacyComponentSerializer.legacySection()
                        .serialize(
                                messages.get(
                                        locale,
                                        MessageKey.SCOREBOARD_PLAYERS,
                                        "count",
                                        String.valueOf(session.participantCount())));
        objective.getScore(arenaLine).setScore(2);
        objective.getScore(playersLine).setScore(1);
    }

    public void apply(Player player) {
        player.setScoreboard(scoreboard);
    }

    public void clear(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
