package dev.diegoh.sumo.arena;

import java.util.Objects;
import org.bukkit.Location;

public final class Arena {
  private final String id;
  private final Location spawnA;
  private final Location spawnB;
  private final Location lobby;
  private final ArenaBounds bounds;
  private final KnockbackConfig knockback;
  private final int minPlayers;
  private final int maxPlayers;

  private Arena(Builder b) {
    this.id = Objects.requireNonNull(b.id, "id");
    this.spawnA = Objects.requireNonNull(b.spawnA, "spawnA");
    this.spawnB = Objects.requireNonNull(b.spawnB, "spawnB");
    this.lobby = Objects.requireNonNull(b.lobby, "lobby");
    this.bounds = Objects.requireNonNull(b.bounds, "bounds");
    this.knockback = Objects.requireNonNull(b.knockback, "knockback");
    if (b.minPlayers < 2) throw new IllegalArgumentException("minPlayers must be >= 2");
    if (b.maxPlayers < b.minPlayers)
      throw new IllegalArgumentException("maxPlayers must be >= minPlayers");
    if (!spawnA.getWorld().equals(spawnB.getWorld())
        || !spawnA.getWorld().equals(lobby.getWorld())) {
      throw new IllegalArgumentException("arena locations must share the same world");
    }
    this.minPlayers = b.minPlayers;
    this.maxPlayers = b.maxPlayers;
  }

  public String id() {
    return id;
  }

  public Location spawnA() {
    return spawnA.clone();
  }

  public Location spawnB() {
    return spawnB.clone();
  }

  public Location lobby() {
    return lobby.clone();
  }

  public ArenaBounds bounds() {
    return bounds;
  }

  public KnockbackConfig knockback() {
    return knockback;
  }

  public int minPlayers() {
    return minPlayers;
  }

  public int maxPlayers() {
    return maxPlayers;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder()
        .id(id)
        .spawnA(spawnA)
        .spawnB(spawnB)
        .lobby(lobby)
        .bounds(bounds)
        .knockback(knockback)
        .minPlayers(minPlayers)
        .maxPlayers(maxPlayers);
  }

  public static final class Builder {
    private String id;
    private Location spawnA;
    private Location spawnB;
    private Location lobby;
    private ArenaBounds bounds;
    private KnockbackConfig knockback;
    private int minPlayers = 2;
    private int maxPlayers = 8;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder spawnA(Location l) {
      this.spawnA = l;
      return this;
    }

    public Builder spawnB(Location l) {
      this.spawnB = l;
      return this;
    }

    public Builder lobby(Location l) {
      this.lobby = l;
      return this;
    }

    public Builder bounds(ArenaBounds b) {
      this.bounds = b;
      return this;
    }

    public Builder knockback(KnockbackConfig k) {
      this.knockback = k;
      return this;
    }

    public Builder minPlayers(int n) {
      this.minPlayers = n;
      return this;
    }

    public Builder maxPlayers(int n) {
      this.maxPlayers = n;
      return this;
    }

    public Arena build() {
      return new Arena(this);
    }
  }
}
