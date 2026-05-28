package dev.diegoh.sumo.command;

import java.util.List;
import org.bukkit.command.CommandSender;

public interface SubCommand {
  String name();

  String permission();

  String usage();

  String descriptionKey();

  boolean playerOnly();

  void execute(CommandSender sender, String[] args);

  List<String> complete(CommandSender sender, String[] args);
}
