package dev.diegoh.sumo.command.sub.admin;

import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaService;
import dev.diegoh.sumo.command.SubCommand;
import dev.diegoh.sumo.i18n.LocaleResolver;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import dev.diegoh.sumo.util.AdventureUtil;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

public final class ArenaSetPlayersSub implements SubCommand {
  private final ArenaService arenas;
  private final Messages messages;
  private final LocaleResolver localeResolver;
  private final AdventureUtil adventure;

  public ArenaSetPlayersSub(
      ArenaService arenas,
      Messages messages,
      LocaleResolver localeResolver,
      AdventureUtil adventure) {
    this.arenas = arenas;
    this.messages = messages;
    this.localeResolver = localeResolver;
    this.adventure = adventure;
  }

  @Override
  public String name() {
    return "setplayers";
  }

  @Override
  public String permission() {
    return "sumo.admin";
  }

  @Override
  public String usage() {
    return "/sumo setplayers <id> <min> <max>";
  }

  @Override
  public String descriptionKey() {
    return "Set per-arena min and max players.";
  }

  @Override
  public boolean playerOnly() {
    return false;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    if (args.length < 3) {
      sendUsage(sender);
      return;
    }
    Arena arena = arenas.find(args[0]).orElse(null);
    if (arena == null) {
      adventure
          .audiences()
          .sender(sender)
          .sendMessage(
              messages.get(
                  localeResolver.resolve(sender),
                  MessageKey.ARENA_NOT_FOUND,
                  Placeholder.parsed("id", args[0])));
      return;
    }
    int min;
    int max;
    try {
      min = Integer.parseInt(args[1]);
      max = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {
      sendUsage(sender);
      return;
    }
    try {
      arenas.update(arena.toBuilder().minPlayers(min).maxPlayers(max).build());
    } catch (IllegalArgumentException e) {
      // Arena enforces min >= 2 and max >= min.
      sendUsage(sender);
      return;
    }
    adventure
        .audiences()
        .sender(sender)
        .sendMessage(
            messages.get(
                localeResolver.resolve(sender),
                MessageKey.ARENA_PLAYERS_SET,
                Placeholder.parsed("id", arena.id()),
                Placeholder.parsed("min", String.valueOf(min)),
                Placeholder.parsed("max", String.valueOf(max))));
  }

  private void sendUsage(CommandSender sender) {
    adventure
        .audiences()
        .sender(sender)
        .sendMessage(
            messages.get(
                localeResolver.resolve(sender), MessageKey.INVALID_USAGE, "usage", usage()));
  }

  @Override
  public List<String> complete(CommandSender sender, String[] args) {
    if (args.length == 1) {
      List<String> ids = new ArrayList<>();
      arenas.all().forEach(a -> ids.add(a.id()));
      return StringUtil.copyPartialMatches(args[0], ids, new ArrayList<>());
    }
    return List.of();
  }
}
