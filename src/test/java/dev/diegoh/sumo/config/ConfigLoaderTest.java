package dev.diegoh.sumo.config;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import dev.diegoh.sumo.SumoPlugin;
import dev.diegoh.sumo.stats.DatabaseDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigLoaderTest {
  private SumoPlugin plugin;

  @BeforeEach
  void setUp() {
    MockBukkit.mock();
    plugin = MockBukkit.load(SumoPlugin.class);
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void loadsBundledDefaults() {
    PluginConfig config = ConfigLoader.load(plugin.getConfig());
    assertEquals("en_US", config.defaultLocale());
    assertTrue(config.followPlayerLocale());
    assertEquals(DatabaseDriver.SQLITE, config.databaseDriver());
    assertEquals("stats.db", config.sqliteFile());
    assertEquals(2, config.defaultMinPlayers());
    assertEquals(8, config.defaultMaxPlayers());
    assertEquals(1.0, config.defaultKnockbackStrength());
  }
}
