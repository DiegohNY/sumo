package dev.diegoh.sumo.command.sub;

import dev.diegoh.sumo.command.SubCommand;
import dev.diegoh.sumo.gui.ArenaSelectorGui;
import dev.diegoh.sumo.i18n.LocaleResolver;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import dev.diegoh.sumo.util.AdventureUtil;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MenuSub implements SubCommand {
  private final ArenaSelectorGui gui;
  private final boolean enabled;
  private final Messages messages;
  private final LocaleResolver localeResolver;
  private final AdventureUtil adventure;

  public MenuSub(
      ArenaSelectorGui gui,
      boolean enabled,
      Messages messages,
      LocaleResolver localeResolver,
      AdventureUtil adventure) {
    this.gui = gui;
    this.enabled = enabled;
    this.messages = messages;
    this.localeResolver = localeResolver;
    this.adventure = adventure;
  }

  @Override
  public String name() {
    return "menu";
  }

  @Override
  public String permission() {
    return "sumo.play";
  }

  @Override
  public String usage() {
    return "/sumo menu";
  }

  @Override
  public String descriptionKey() {
    return "Open the arena selector GUI.";
  }

  @Override
  public boolean playerOnly() {
    return true;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    Player p = (Player) sender;
    if (!enabled) {
      adventure
          .audiences()
          .player(p)
          .sendMessage(messages.get(localeResolver.resolve(p), MessageKey.NO_PERMISSION));
      return;
    }
    gui.open(p);
  }

  @Override
  public List<String> complete(CommandSender sender, String[] args) {
    return List.of();
  }
}
