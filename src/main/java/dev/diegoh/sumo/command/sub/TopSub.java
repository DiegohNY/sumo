package dev.diegoh.sumo.command.sub;

import dev.diegoh.sumo.command.SubCommand;
import dev.diegoh.sumo.i18n.LocaleResolver;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import dev.diegoh.sumo.stats.PlayerStats;
import dev.diegoh.sumo.stats.StatsService;
import dev.diegoh.sumo.util.AdventureUtil;
import java.util.List;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public final class TopSub implements SubCommand {
  private static final int LIMIT = 10;

  private final StatsService stats;
  private final Messages messages;
  private final LocaleResolver localeResolver;
  private final AdventureUtil adventure;
  private final Plugin plugin;

  public TopSub(
      StatsService stats,
      Messages messages,
      LocaleResolver localeResolver,
      AdventureUtil adventure,
      Plugin plugin) {
    this.stats = stats;
    this.messages = messages;
    this.localeResolver = localeResolver;
    this.adventure = adventure;
    this.plugin = plugin;
  }

  @Override
  public String name() {
    return "top";
  }

  @Override
  public String permission() {
    return "sumo.play";
  }

  @Override
  public String usage() {
    return "/sumo top";
  }

  @Override
  public String descriptionKey() {
    return "Show the win leaderboard.";
  }

  @Override
  public boolean playerOnly() {
    return false;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    stats
        .top(LIMIT)
        .thenAccept(list -> onMain(() -> render(sender, list)))
        .exceptionally(
            ex -> {
              onMain(
                  () ->
                      adventure
                          .audiences()
                          .sender(sender)
                          .sendMessage(
                              messages.get(
                                  localeResolver.resolve(sender), MessageKey.STATS_TOP_EMPTY)));
              return null;
            });
  }

  private void render(CommandSender sender, List<PlayerStats> top) {
    var audience = adventure.audiences().sender(sender);
    audience.sendMessage(messages.get(localeResolver.resolve(sender), MessageKey.STATS_TOP_HEADER));
    if (top.isEmpty()) {
      audience.sendMessage(
          messages.get(localeResolver.resolve(sender), MessageKey.STATS_TOP_EMPTY));
      return;
    }
    int rank = 1;
    for (PlayerStats s : top) {
      OfflinePlayer op = Bukkit.getOfflinePlayer(s.uuid());
      String name = op.getName() != null ? op.getName() : s.uuid().toString().substring(0, 8);
      audience.sendMessage(
          messages.get(
              localeResolver.resolve(sender),
              MessageKey.STATS_TOP_ENTRY,
              Placeholder.parsed("rank", String.valueOf(rank)),
              Placeholder.parsed("player", name),
              Placeholder.parsed("wins", String.valueOf(s.wins())),
              Placeholder.parsed("losses", String.valueOf(s.losses())),
              Placeholder.parsed("best", String.valueOf(s.bestStreak()))));
      rank++;
    }
  }

  private void onMain(Runnable action) {
    if (Bukkit.isPrimaryThread()) {
      action.run();
    } else {
      Bukkit.getScheduler().runTask(plugin, action);
    }
  }

  @Override
  public List<String> complete(CommandSender sender, String[] args) {
    return List.of();
  }
}
