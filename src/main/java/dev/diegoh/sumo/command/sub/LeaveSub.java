package dev.diegoh.sumo.command.sub;

import dev.diegoh.sumo.command.SubCommand;
import dev.diegoh.sumo.game.GameOrchestrator;
import dev.diegoh.sumo.i18n.LocaleResolver;
import dev.diegoh.sumo.i18n.MessageKey;
import dev.diegoh.sumo.i18n.Messages;
import dev.diegoh.sumo.util.AdventureUtil;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LeaveSub implements SubCommand {
    private final GameOrchestrator orchestrator;
    private final Messages messages;
    private final LocaleResolver localeResolver;
    private final AdventureUtil adventure;

    public LeaveSub(
            GameOrchestrator orchestrator,
            Messages messages,
            LocaleResolver localeResolver,
            AdventureUtil adventure) {
        this.orchestrator = orchestrator;
        this.messages = messages;
        this.localeResolver = localeResolver;
        this.adventure = adventure;
    }

    @Override
    public String name() {
        return "leave";
    }

    @Override
    public String permission() {
        return "sumo.play";
    }

    @Override
    public String usage() {
        return "/sumo leave";
    }

    @Override
    public String descriptionKey() {
        return "Leave your current game.";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        var key =
                orchestrator.leave(p) ? MessageKey.LEAVE_SUCCESS : MessageKey.LEAVE_NOT_IN_GAME;
        adventure.audiences().player(p).sendMessage(messages.get(localeResolver.resolve(p), key));
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
