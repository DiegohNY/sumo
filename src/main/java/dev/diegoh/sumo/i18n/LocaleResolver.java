package dev.diegoh.sumo.i18n;

import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LocaleResolver {
  private final String defaultLocale;
  private final boolean followPlayer;

  public LocaleResolver(String defaultLocale, boolean followPlayer) {
    this.defaultLocale = defaultLocale;
    this.followPlayer = followPlayer;
  }

  public Locale resolve(CommandSender sender) {
    if (!followPlayer || !(sender instanceof Player p)) return parse(defaultLocale);
    String code = p.getLocale();
    return code == null || code.isEmpty() ? parse(defaultLocale) : parse(code);
  }

  private Locale parse(String code) {
    String[] parts = code.split("_");
    return parts.length >= 2 ? new Locale(parts[0], parts[1]) : new Locale(parts[0]);
  }
}
