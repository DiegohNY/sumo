package dev.diegoh.sumo;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class SumoPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerCommands();
    }

    private void registerCommands() {
        getCommand("sumo")
                .setExecutor(
                        (CommandExecutor)
                                (sender, cmd, label, args) -> {
                                    sender.sendMessage("Sumo loaded — commands coming soon.");
                                    return true;
                                });
    }
}
