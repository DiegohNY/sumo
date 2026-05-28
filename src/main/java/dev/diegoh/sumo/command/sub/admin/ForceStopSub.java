package dev.diegoh.sumo.command.sub.admin;

import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaService;
import dev.diegoh.sumo.command.SubCommand;
import dev.diegoh.sumo.game.GameOrchestrator;
import dev.diegoh.sumo.game.GameSession;
import dev.diegoh.sumo.i18n.LocaleResolver;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import dev.diegoh.sumo.util.AdventureUtil;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class ForceStopSub implements SubCommand {
    private final GameOrchestrator orchestrator;
    private final ArenaService arenas;
    private final Messages messages;
    private final LocaleResolver localeResolver;
    private final AdventureUtil adventure;

    public ForceStopSub(
            GameOrchestrator orchestrator,
            ArenaService arenas,
            Messages messages,
            LocaleResolver localeResolver,
            AdventureUtil adventure) {
        this.orchestrator = orchestrator;
        this.arenas = arenas;
        this.messages = messages;
        this.localeResolver = localeResolver;
        this.adventure = adventure;
    }

    @Override
    public String name() {
        return "forcestop";
    }

    @Override
    public String permission() {
        return "sumo.admin";
    }

    @Override
    public String usage() {
        return "/sumo forcestop <id>";
    }

    @Override
    public String descriptionKey() {
        return "Force stop the tournament in an arena.";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) return;
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
        GameSession session = orchestrator.sessionForArena(arena.id()).orElse(null);
        if (session != null) {
            for (var uuid : session.participants()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) orchestrator.leave(p);
            }
        }
        adventure
                .audiences()
                .sender(sender)
                .sendMessage(
                        messages.get(
                                localeResolver.resolve(sender),
                                MessageKey.FORCE_STOP_OK,
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
