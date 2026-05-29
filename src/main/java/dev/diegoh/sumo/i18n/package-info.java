/**
 * Internationalization (i18n): all player-facing text.
 *
 * <p>{@link dev.diegoh.sumo.i18n.MessageKey} is a type-safe enum of every message (so a typo is a
 * compile error, not a runtime surprise). {@link dev.diegoh.sumo.i18n.Messages} loads the {@code
 * lang/messages_<locale>.yml} bundles, renders them with MiniMessage, and falls back to the default
 * locale for any missing line. {@link dev.diegoh.sumo.i18n.LocaleResolver} decides which language a
 * given player sees.
 */
package dev.diegoh.sumo.i18n;
