/**
 * Per-player helpers used by the game layer.
 *
 * <p>{@link dev.diegoh.sumo.player.InventoryStore} snapshots a player's inventory, armor, game mode
 * and location when they join and restores it when they leave, are eliminated, or disconnect.
 * {@link dev.diegoh.sumo.player.SessionRegistry} is a fast UUID → {@link
 * dev.diegoh.sumo.game.GameSession} lookup so listeners can answer "which game is this player in?"
 * in O(1).
 */
package dev.diegoh.sumo.player;
