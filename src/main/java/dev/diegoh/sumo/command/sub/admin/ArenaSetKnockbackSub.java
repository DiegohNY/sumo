package dev.diegoh.sumo.command.sub.admin;

import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaService;
import dev.diegoh.sumo.arena.KnockbackConfig;
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

public final class ArenaSetKnockbackSub implements SubCommand {
    private final ArenaService arenas;
    private final Messages messages;
    private final LocaleResolver localeResolver;
    private final AdventureUtil adventure;

    public ArenaSetKnockbackSub(
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
        return "setkb";
    }

    @Override
    public String permission() {
        return "sumo.admin";
    }

    @Override
    public String usage() {
        return "/sumo setkb <id> <strength> <vertical> <friction>";
    }

    @Override
    public String descriptionKey() {
        return "Set per-arena knockback parameters.";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 4) return;
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
        double strength;
        double vertical;
        double friction;
        try {
            strength = Double.parseDouble(args[1]);
            vertical = Double.parseDouble(args[2]);
            friction = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            return;
        }
        arenas.update(
                arena.toBuilder().knockback(new KnockbackConfig(strength, vertical, friction)).build());
        adventure
                .audiences()
                .sender(sender)
                .sendMessage(
                        messages.get(
                                localeResolver.resolve(sender),
                                MessageKey.ARENA_KB_SET,
                                Placeholder.parsed("id", arena.id()),
                                Placeholder.parsed("strength", args[1]),
                                Placeholder.parsed("vertical", args[2]),
                                Placeholder.parsed("friction", args[3])));
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
