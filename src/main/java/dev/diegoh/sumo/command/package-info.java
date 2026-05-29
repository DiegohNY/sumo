/**
 * The {@code /sumo} command.
 *
 * <p>{@link dev.diegoh.sumo.command.SumoCommand} is the single entry point Bukkit calls; it just
 * routes the first argument to the matching {@link dev.diegoh.sumo.command.SubCommand}. Each
 * subcommand is its own small class (see the {@code sub} and {@code sub.admin} subpackages) that
 * validates input and calls a service. Adding a command = write one class and register it in {@link
 * dev.diegoh.sumo.SumoPlugin}.
 */
package dev.diegoh.sumo.command;
