/**
 * Arenas: what a ring is and how it is stored and managed.
 *
 * <p>An {@link dev.diegoh.sumo.arena.Arena} is an immutable value object describing one ring (its
 * spawns, lobby, {@link dev.diegoh.sumo.arena.ArenaBounds bounds} and {@link
 * dev.diegoh.sumo.arena.KnockbackConfig knockback}). {@link dev.diegoh.sumo.arena.ArenaRepository}
 * persists arenas as YAML files, and {@link dev.diegoh.sumo.arena.ArenaService} is the in-memory
 * API the rest of the plugin calls to create, update, find and delete them.
 */
package dev.diegoh.sumo.arena;
