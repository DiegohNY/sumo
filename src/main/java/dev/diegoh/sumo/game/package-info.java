/**
 * The gameplay itself.
 *
 * <p>{@link dev.diegoh.sumo.game.GameSession} is a small state machine that runs one tournament in
 * one arena, walking through {@link dev.diegoh.sumo.game.GameState} ({@code WAITING → COUNTDOWN →
 * ACTIVE → ENDING}) and pairing players into a {@link dev.diegoh.sumo.game.Match}. {@link
 * dev.diegoh.sumo.game.GameOrchestrator} owns all the live sessions at once (one per arena) and is
 * the single place commands and listeners ask "is this player in a game?".
 */
package dev.diegoh.sumo.game;
