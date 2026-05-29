package dev.diegoh.sumo.arena;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class KnockbackConfigTest {

  @Test
  void acceptsValidValues() {
    KnockbackConfig kb = new KnockbackConfig(1.5, 0.4, 0.5);
    assertEquals(1.5, kb.strength());
    assertEquals(0.4, kb.verticalBoost());
    assertEquals(0.5, kb.friction());
  }

  @Test
  void rejectsNegativeStrength() {
    assertThrows(IllegalArgumentException.class, () -> new KnockbackConfig(-1.0, 0.4, 0.5));
  }

  @Test
  void rejectsFrictionOutOfRange() {
    assertThrows(IllegalArgumentException.class, () -> new KnockbackConfig(1.0, 0.4, -0.1));
    assertThrows(IllegalArgumentException.class, () -> new KnockbackConfig(1.0, 0.4, 1.1));
  }
}
