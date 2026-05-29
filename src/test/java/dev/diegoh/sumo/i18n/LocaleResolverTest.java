package dev.diegoh.sumo.i18n;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocaleResolverTest {
  private ServerMock server;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void usesDefaultWhenNotFollowingPlayer() {
    LocaleResolver resolver = new LocaleResolver("it_IT", false);
    PlayerMock p = server.addPlayer();
    Locale locale = resolver.resolve(p);
    assertEquals("it", locale.getLanguage());
    assertEquals("IT", locale.getCountry());
  }

  @Test
  void usesDefaultForConsoleSender() {
    LocaleResolver resolver = new LocaleResolver("en_US", true);
    Locale locale = resolver.resolve(server.getConsoleSender());
    assertEquals("en", locale.getLanguage());
    assertEquals("US", locale.getCountry());
  }

  @Test
  void followsPlayerLocaleWhenEnabled() {
    LocaleResolver resolver = new LocaleResolver("en_US", true);
    PlayerMock p = server.addPlayer();
    // With follow enabled, the resolved locale is derived from the player's own client locale.
    Locale locale = resolver.resolve(p);
    assertEquals(p.getLocale().split("_")[0], locale.getLanguage());
  }
}
