package dev.diegoh.sumo.command.sub;

import dev.diegoh.sumo.arena.ArenaRepository;
import dev.diegoh.sumo.command.SubCommand;
import dev.diegoh.sumo.i18n.LocaleResolver;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import dev.diegoh.sumo.util.AdventureUtil;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public final class ReloadSub implements SubCommand {
  private final Messages messages;
  private final ArenaRepository arenaRepository;
  private final Plugin plugin;
  private final LocaleResolver localeResolver;
  private final AdventureUtil adventure;

  public ReloadSub(
      Messages messages,
      ArenaRepository arenaRepository,
      Plugin plugin,
      LocaleResolver localeResolver,
      AdventureUtil adventure) {
    this.messages = messages;
    this.arenaRepository = arenaRepository;
    this.plugin = plugin;
    this.localeResolver = localeResolver;
    this.adventure = adventure;
  }

  @Override
  public String name() {
    return "reload";
  }

  @Override
  public String permission() {
    return "sumo.admin";
  }

  @Override
  public String usage() {
    return "/sumo reload";
  }

  @Override
  public String descriptionKey() {
    return "Reload config and language files.";
  }

  @Override
  public boolean playerOnly() {
    return false;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    plugin.reloadConfig();
    messages.reload();
    arenaRepository.loadAll();
    adventure
        .audiences()
        .sender(sender)
        .sendMessage(messages.get(localeResolver.resolve(sender), MessageKey.RELOADED));
  }

  @Override
  public List<String> complete(CommandSender sender, String[] args) {
    return List.of();
  }
}
