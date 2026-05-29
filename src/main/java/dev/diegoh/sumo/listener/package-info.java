/**
 * Bukkit event handlers, one class per concern.
 *
 * <p>Each listener asks {@link dev.diegoh.sumo.game.GameOrchestrator} whether the player involved
 * is in a game and, if so, applies the rule: {@link dev.diegoh.sumo.listener.CombatListener} for
 * custom knockback, {@link dev.diegoh.sumo.listener.BoundsListener} for ring-out and water
 * elimination, {@link dev.diegoh.sumo.listener.ProtectionListener} for blocking
 * fall/void/hunger/etc. damage, and {@link dev.diegoh.sumo.listener.ConnectionListener} for
 * clean-up when a player quits.
 */
package dev.diegoh.sumo.listener;
