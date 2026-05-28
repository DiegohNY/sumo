package dev.diegoh.sumo.i18n;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import dev.diegoh.sumo.SumoPlugin;
import java.util.Locale;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessagesTest {
  private SumoPlugin plugin;
  private Messages messages;

  @BeforeEach
  void setUp() {
    MockBukkit.mock();
    plugin = MockBukkit.load(SumoPlugin.class);
    messages = new Messages(plugin, "en_US");
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void englishMessageRenders() {
    String out =
        PlainTextComponentSerializer.plainText()
            .serialize(messages.get(Locale.US, MessageKey.PLUGIN_ENABLED));
    assertEquals("Sumo enabled.", out);
  }

  @Test
  void italianMessageRenders() {
    String out =
        PlainTextComponentSerializer.plainText()
            .serialize(messages.get(Locale.ITALY, MessageKey.PLUGIN_ENABLED));
    assertEquals("Sumo attivato.", out);
  }

  @Test
  void unsupportedLocaleFallsBackToDefault() {
    String out =
        PlainTextComponentSerializer.plainText()
            .serialize(messages.get(Locale.JAPAN, MessageKey.PLUGIN_ENABLED));
    assertEquals("Sumo enabled.", out);
  }

  @Test
  void placeholderResolves() {
    String out =
        PlainTextComponentSerializer.plainText()
            .serialize(messages.get(Locale.US, MessageKey.ARENA_CREATED, "id", "main"));
    assertEquals("Arena main created.", out);
  }
}
