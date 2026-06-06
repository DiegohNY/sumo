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

  // Note: the "follow player locale" path can't be unit-tested here because MockBukkit 3.9.0 does
  // not implement Player#getLocale(); it's covered manually in-game.
}
