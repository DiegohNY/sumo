package dev.diegoh.sumo.command;

import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * One {@code /sumo <name> ...} subcommand.
 *
 * <p>Implement this for each command. {@link SumoCommand} handles the boilerplate for you —
 * permission check, player-only check, and slicing off the subcommand name — before calling {@link
 * #execute}. To add a command: write a class implementing this interface and register it in {@link
 * dev.diegoh.sumo.SumoPlugin}.
 */
public interface SubCommand {
  String name();

  String permission();

  String usage();

  String descriptionKey();

  boolean playerOnly();

  void execute(CommandSender sender, String[] args);

  List<String> complete(CommandSender sender, String[] args);
}
