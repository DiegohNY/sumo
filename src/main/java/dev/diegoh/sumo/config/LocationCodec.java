package dev.diegoh.sumo.config;

import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

public final class LocationCodec {

  private LocationCodec() {}

  public static String encode(Location location) {
    return String.join(
        ",",
        location.getWorld().getName(),
        Double.toString(location.getX()),
        Double.toString(location.getY()),
        Double.toString(location.getZ()),
        Float.toString(location.getYaw()),
        Float.toString(location.getPitch()));
  }

  public static Optional<Location> decode(String encoded, Server server) {
    if (encoded == null) return Optional.empty();
    String[] parts = encoded.split(",");
    if (parts.length != 6) return Optional.empty();
    World world = server.getWorld(parts[0]);
    if (world == null) return Optional.empty();
    try {
      return Optional.of(
          new Location(
              world,
              Double.parseDouble(parts[1]),
              Double.parseDouble(parts[2]),
              Double.parseDouble(parts[3]),
              Float.parseFloat(parts[4]),
              Float.parseFloat(parts[5])));
    } catch (NumberFormatException ex) {
      return Optional.empty();
    }
  }
}
