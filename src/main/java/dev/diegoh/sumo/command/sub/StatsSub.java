package dev.diegoh.sumo.command.sub;

import dev.diegoh.sumo.command.SubCommand;
import dev.diegoh.sumo.i18n.LocaleResolver;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import dev.diegoh.sumo.stats.PlayerStats;
import dev.diegoh.sumo.stats.StatsService;
import dev.diegoh.sumo.util.AdventureUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class StatsSub implements SubCommand {
    private final StatsService stats;
    private final Messages messages;
    private final LocaleResolver localeResolver;
    private final AdventureUtil adventure;

    public StatsSub(
            StatsService stats,
            Messages messages,
            LocaleResolver localeResolver,
            AdventureUtil adventure) {
        this.stats = stats;
        this.messages = messages;
        this.localeResolver = localeResolver;
        this.adventure = adventure;
    }

    @Override
    public String name() {
        return "stats";
    }

    @Override
    public String permission() {
        return "sumo.play";
    }

    @Override
    public String usage() {
        return "/sumo stats [player]";
    }

    @Override
    public String descriptionKey() {
        return "View stats.";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String target;
        UUID uuid;
        if (args.length >= 1) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
            target = op.getName() == null ? args[0] : op.getName();
            uuid = op.getUniqueId();
        } else if (sender instanceof Player p) {
            target = p.getName();
            uuid = p.getUniqueId();
        } else {
            adventure
                    .audiences()
                    .sender(sender)
                    .sendMessage(messages.get(localeResolver.resolve(sender), MessageKey.PLAYERS_ONLY));
            return;
        }
        stats.get(uuid)
                .thenAccept(
                        s -> render(sender, target, s))
                .exceptionally(
                        ex -> {
                            adventure
                                    .audiences()
                                    .sender(sender)
                                    .sendMessage(
                                            messages.get(
                                                    localeResolver.resolve(sender),
                                                    MessageKey.STATS_LINE,
                                                    Placeholder.parsed("wins", "0"),
                                                    Placeholder.parsed("losses", "0"),
                                                    Placeholder.parsed("streak", "0")));
                            return null;
                        });
    }

    private void render(CommandSender sender, String target, PlayerStats s) {
        var audience = adventure.audiences().sender(sender);
        audience.sendMessage(
                messages.get(
                        localeResolver.resolve(sender),
                        MessageKey.STATS_HEADER,
                        Placeholder.parsed("player", target)));
        audience.sendMessage(
                messages.get(
                        localeResolver.resolve(sender),
                        MessageKey.STATS_LINE,
                        Placeholder.parsed("wins", String.valueOf(s.wins())),
                        Placeholder.parsed("losses", String.valueOf(s.losses())),
                        Placeholder.parsed("streak", String.valueOf(s.currentStreak()))));
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
            return StringUtil.copyPartialMatches(args[0], names, new ArrayList<>());
        }
        return List.of();
    }
}
