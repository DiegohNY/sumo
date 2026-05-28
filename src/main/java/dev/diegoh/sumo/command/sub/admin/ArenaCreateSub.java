package dev.diegoh.sumo.command.sub.admin;

import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaBounds;
import dev.diegoh.sumo.arena.ArenaService;
import dev.diegoh.sumo.arena.KnockbackConfig;
import dev.diegoh.sumo.command.SubCommand;
import dev.diegoh.sumo.config.PluginConfig;
import dev.diegoh.sumo.i18n.LocaleResolver;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import dev.diegoh.sumo.util.AdventureUtil;
import java.util.List;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ArenaCreateSub implements SubCommand {
  private final ArenaService arenas;
  private final PluginConfig config;
  private final Messages messages;
  private final LocaleResolver localeResolver;
  private final AdventureUtil adventure;

  public ArenaCreateSub(
      ArenaService arenas,
      PluginConfig config,
      Messages messages,
      LocaleResolver localeResolver,
      AdventureUtil adventure) {
    this.arenas = arenas;
    this.config = config;
    this.messages = messages;
    this.localeResolver = localeResolver;
    this.adventure = adventure;
  }

  @Override
  public String name() {
    return "create";
  }

  @Override
  public String permission() {
    return "sumo.admin";
  }

  @Override
  public String usage() {
    return "/sumo create <id>";
  }

  @Override
  public String descriptionKey() {
    return "Create an arena (uses your current location).";
  }

  @Override
  public boolean playerOnly() {
    return true;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    Player p = (Player) sender;
    if (args.length < 1) {
      adventure
          .audiences()
          .player(p)
          .sendMessage(
              messages.get(localeResolver.resolve(p), MessageKey.INVALID_USAGE, "usage", usage()));
      return;
    }
    var loc = p.getLocation();
    Arena.Builder builder =
        Arena.builder()
            .spawnA(loc)
            .spawnB(loc)
            .lobby(loc)
            .bounds(ArenaBounds.cylinder(loc, 15.0))
            .knockback(
                new KnockbackConfig(
                    config.defaultKnockbackStrength(),
                    config.defaultKnockbackVertical(),
                    config.defaultKnockbackFriction()))
            .minPlayers(config.defaultMinPlayers())
            .maxPlayers(config.defaultMaxPlayers());
    var created = arenas.create(args[0], builder);
    if (created.isEmpty()) {
      adventure
          .audiences()
          .player(p)
          .sendMessage(
              messages.get(
                  localeResolver.resolve(p),
                  MessageKey.ARENA_NOT_FOUND,
                  Placeholder.parsed("id", args[0])));
      return;
    }
    adventure
        .audiences()
        .player(p)
        .sendMessage(
            messages.get(
                localeResolver.resolve(p),
                MessageKey.ARENA_CREATED,
                Placeholder.parsed("id", args[0])));
  }

  @Override
  public List<String> complete(CommandSender sender, String[] args) {
    return List.of();
  }
}
