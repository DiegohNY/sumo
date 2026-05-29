/**
 * Configuration and (de)serialization helpers.
 *
 * <p>{@link dev.diegoh.sumo.config.ConfigLoader} reads {@code config.yml} into an immutable {@link
 * dev.diegoh.sumo.config.PluginConfig} snapshot, so the rest of the code depends on a typed object
 * instead of raw YAML lookups. {@link dev.diegoh.sumo.config.LocationCodec} converts a Bukkit
 * {@code Location} to and from a string, returning {@link java.util.Optional} instead of {@code
 * null} on bad input.
 */
package dev.diegoh.sumo.config;
