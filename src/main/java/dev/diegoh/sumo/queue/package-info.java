/**
 * Optional per-arena matchmaking queue.
 *
 * <p>{@link dev.diegoh.sumo.queue.MatchmakingQueue} is a simple FIFO waiting line for one arena.
 * {@link dev.diegoh.sumo.queue.QueueService} manages one queue per arena and guarantees a player is
 * only ever queued in one place at a time.
 */
package dev.diegoh.sumo.queue;
