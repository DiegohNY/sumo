package dev.diegoh.sumo.arena;

public record KnockbackConfig(double strength, double verticalBoost, double friction) {
  public KnockbackConfig {
    if (strength < 0) throw new IllegalArgumentException("strength must be >= 0");
    if (friction < 0 || friction > 1)
      throw new IllegalArgumentException("friction must be in [0,1]");
  }
}
