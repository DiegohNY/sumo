package dev.diegoh.sumo.command.sub;

import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaService;
import dev.diegoh.sumo.command.SubCommand;
import dev.diegoh.sumo.game.GameOrchestrator;
import dev.diegoh.sumo.game.GameState;
import dev.diegoh.sumo.i18n.LocaleResolver;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import dev.diegoh.sumo.util.AdventureUtil;
import java.util.List;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

public final class ListSub implements SubCommand {
    private final ArenaService arenas;
    private final GameOrchestrator orchestrator;
    private final Messages messages;
    private final LocaleResolver localeResolver;
    private final AdventureUtil adventure;

    public ListSub(
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
        return "list";
    }

    @Override
    public String permission() {
        return "sumo.play";
    }

    @Override
    public String usage() {
        return "/sumo list";
    }

    @Override
    public String descriptionKey() {
        return "List arenas.";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        var audience = adventure.audiences().sender(sender);
        audience.sendMessage(messages.get(localeResolver.resolve(sender), MessageKey.ARENA_LIST_HEADER));
        for (Arena arena : arenas.all()) {
            GameState state =
                    orchestrator
                            .sessionForArena(arena.id())
                            .map(s -> s.state())
                            .orElse(GameState.IDLE);
            audience.sendMessage(
                    messages.get(
                            localeResolver.resolve(sender),
                            MessageKey.ARENA_LIST_ENTRY,
                            Placeholder.parsed("id", arena.id()),
                            Placeholder.parsed("state", state.name())));
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
