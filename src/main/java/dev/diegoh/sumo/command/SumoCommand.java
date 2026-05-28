package dev.diegoh.sumo.command;

import dev.diegoh.sumo.i18n.LocaleResolver;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import dev.diegoh.sumo.util.AdventureUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class SumoCommand implements CommandExecutor, TabCompleter {
  private final Map<String, SubCommand> subs = new LinkedHashMap<>();
  private final Messages messages;
  private final LocaleResolver localeResolver;
  private final AdventureUtil adventure;

  public SumoCommand(Messages messages, LocaleResolver localeResolver, AdventureUtil adventure) {
    this.messages = messages;
    this.localeResolver = localeResolver;
    this.adventure = adventure;
  }

  public SumoCommand register(SubCommand sub) {
    subs.put(sub.name().toLowerCase(), sub);
    return this;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length == 0) {
      renderHelp(sender);
      return true;
    }
    SubCommand sub = subs.get(args[0].toLowerCase());
    if (sub == null) {
      renderHelp(sender);
      return true;
    }
    if (!sub.permission().isEmpty() && !sender.hasPermission(sub.permission())) {
      adventure
          .audiences()
          .sender(sender)
          .sendMessage(messages.get(localeResolver.resolve(sender), MessageKey.NO_PERMISSION));
      return true;
    }
    if (sub.playerOnly() && !(sender instanceof Player)) {
      adventure
          .audiences()
          .sender(sender)
          .sendMessage(messages.get(localeResolver.resolve(sender), MessageKey.PLAYERS_ONLY));
      return true;
    }
    String[] subArgs = new String[args.length - 1];
    System.arraycopy(args, 1, subArgs, 0, subArgs.length);
    sub.execute(sender, subArgs);
    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String alias, String[] args) {
    if (args.length == 1) {
      List<String> names = new ArrayList<>();
      for (SubCommand s : subs.values()) {
        if (s.permission().isEmpty() || sender.hasPermission(s.permission())) names.add(s.name());
      }
      return StringUtil.copyPartialMatches(args[0], names, new ArrayList<>());
    }
    SubCommand sub = subs.get(args[0].toLowerCase());
    if (sub == null) return Collections.emptyList();
    String[] subArgs = new String[args.length - 1];
    System.arraycopy(args, 1, subArgs, 0, subArgs.length);
    return sub.complete(sender, subArgs);
  }

  private void renderHelp(CommandSender sender) {
    var audience = adventure.audiences().sender(sender);
    audience.sendMessage(messages.get(localeResolver.resolve(sender), MessageKey.HELP_HEADER));
    for (SubCommand s : subs.values()) {
      if (!s.permission().isEmpty() && !sender.hasPermission(s.permission())) continue;
      audience.sendMessage(
          messages.get(
              localeResolver.resolve(sender),
              MessageKey.HELP_LINE,
              Placeholder.parsed("usage", s.usage()),
              Placeholder.parsed("description", s.descriptionKey())));
    }
  }
}
