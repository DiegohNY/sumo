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
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class ArenaSetLobbySub implements SubCommand {
    private final ArenaService arenas;
    private final Messages messages;
    private final LocaleResolver localeResolver;
    private final AdventureUtil adventure;

    public ArenaSetLobbySub(
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
        return "setlobby";
    }

    @Override
    public String permission() {
        return "sumo.admin";
    }

    @Override
    public String usage() {
        return "/sumo setlobby <id>";
    }

    @Override
    public String descriptionKey() {
        return "Set arena lobby spawn.";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        if (args.length < 1) return;
        Arena arena = arenas.find(args[0]).orElse(null);
        if (arena == null) {
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
        arenas.update(arena.toBuilder().lobby(p.getLocation()).build());
        adventure
                .audiences()
                .player(p)
                .sendMessage(
                        messages.get(
                                localeResolver.resolve(p),
                                MessageKey.ARENA_LOBBY_SET,
                                Placeholder.parsed("id", arena.id())));
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
