/**
 * Player-facing feedback driven by game events.
 *
 * <p>{@link dev.diegoh.sumo.ui.SessionUiPresenter} subscribes to a {@link
 * dev.diegoh.sumo.game.GameSession}'s {@link dev.diegoh.sumo.game.SessionEvent} stream and turns
 * each event into the visible reaction — titles, action bars and scoreboard refreshes — keeping the
 * presentation concerns out of the game logic.
 */
package dev.diegoh.sumo.ui;
