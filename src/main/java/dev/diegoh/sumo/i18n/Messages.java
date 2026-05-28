package dev.diegoh.sumo.i18n;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class Messages {
  private final Plugin plugin;
  private final MiniMessage mini = MiniMessage.miniMessage();
  private final Map<String, YamlConfiguration> byLocale = new HashMap<>();
  private final String defaultLocale;

  public Messages(Plugin plugin, String defaultLocale) {
    this.plugin = plugin;
    this.defaultLocale = defaultLocale;
    reload();
  }

  public void reload() {
    byLocale.clear();
    loadBundled("en_US");
    loadBundled("it_IT");
    loadOverrides();
  }

  private void loadBundled(String locale) {
    String resource = "lang/messages_" + locale + ".yml";
    InputStream stream = plugin.getResource(resource);
    if (stream == null) {
      plugin.getLogger().warning("Missing bundled language: " + resource);
      return;
    }
    try (Reader r = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      byLocale.put(locale, YamlConfiguration.loadConfiguration(r));
    } catch (Exception e) {
      plugin.getLogger().warning("Failed to load " + resource + ": " + e.getMessage());
    }
  }

  private void loadOverrides() {
    File langDir = new File(plugin.getDataFolder(), "lang");
    if (!langDir.exists() && !langDir.mkdirs()) return;
    File[] files = langDir.listFiles((d, n) -> n.startsWith("messages_") && n.endsWith(".yml"));
    if (files == null) return;
    for (File f : files) {
      String locale =
          f.getName().substring("messages_".length(), f.getName().length() - ".yml".length());
      YamlConfiguration override = YamlConfiguration.loadConfiguration(f);
      YamlConfiguration base = byLocale.computeIfAbsent(locale, k -> new YamlConfiguration());
      for (String key : override.getKeys(true)) {
        base.set(key, override.get(key));
      }
    }
  }

  public Component get(Locale locale, MessageKey key, TagResolver... resolvers) {
    String localeCode =
        locale == null ? defaultLocale : (locale.getLanguage() + "_" + locale.getCountry());
    YamlConfiguration cfg = byLocale.getOrDefault(localeCode, byLocale.get(defaultLocale));
    if (cfg == null) return Component.text("missing: " + key.path());
    String raw = cfg.getString(key.path());
    if (raw == null) {
      YamlConfiguration fallback = byLocale.get(defaultLocale);
      raw =
          fallback != null
              ? fallback.getString(key.path(), "missing: " + key.path())
              : "missing: " + key.path();
    }
    return mini.deserialize(raw, resolvers);
  }

  public Component get(Locale locale, MessageKey key, String name, String value) {
    return get(locale, key, Placeholder.parsed(name, value));
  }
}
