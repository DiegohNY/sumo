package dev.diegoh.sumo.command.sub;

import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaService;
import dev.diegoh.sumo.command.SubCommand;
import dev.diegoh.sumo.game.GameOrchestrator;
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

public final class JoinSub implements SubCommand {
    private final ArenaService arenas;
    private final GameOrchestrator orchestrator;
    private final Messages messages;
    private final LocaleResolver localeResolver;
    private final AdventureUtil adventure;

    public JoinSub(
            ArenaService arenas,
            GameOrchestrator orchestrator,
            Messages messages,
            LocaleResolver localeResolver,
            AdventureUtil adventure) {
        this.arenas = arenas;
        this.orchestrator = orchestrator;
        this.messages = messages;
        this.localeResolver = localeResolver;
        this.adventure = adventure;
    }

    @Override
    public String name() {
        return "join";
    }

    @Override
    public String permission() {
        return "sumo.play";
    }

    @Override
    public String usage() {
        return "/sumo join <arena>";
    }

    @Override
    public String descriptionKey() {
        return "Join an arena.";
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
                            messages.get(
                                    localeResolver.resolve(p),
                                    MessageKey.INVALID_USAGE,
                                    "usage",
                                    usage()));
            return;
        }
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
        if (orchestrator.join(arena, p)) {
            adventure
                    .audiences()
                    .player(p)
                    .sendMessage(
                            messages.get(
                                    localeResolver.resolve(p),
                                    MessageKey.JOIN_SUCCESS,
                                    Placeholder.parsed("id", arena.id())));
        } else {
            adventure
                    .audiences()
                    .player(p)
                    .sendMessage(messages.get(localeResolver.resolve(p), MessageKey.JOIN_ALREADY_IN_GAME));
        }
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
