package dev.diegoh.sumo.command.sub.admin;

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

public final class ArenaDeleteSub implements SubCommand {
    private final ArenaService arenas;
    private final Messages messages;
    private final LocaleResolver localeResolver;
    private final AdventureUtil adventure;

    public ArenaDeleteSub(
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
        return "delete";
    }

    @Override
    public String permission() {
        return "sumo.admin";
    }

    @Override
    public String usage() {
        return "/sumo delete <id>";
    }

    @Override
    public String descriptionKey() {
        return "Delete an arena.";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) return;
        boolean ok = arenas.delete(args[0]);
        var key = ok ? MessageKey.ARENA_DELETED : MessageKey.ARENA_NOT_FOUND;
        adventure
                .audiences()
                .sender(sender)
                .sendMessage(
                        messages.get(
                                localeResolver.resolve(sender), key, Placeholder.parsed("id", args[0])));
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
