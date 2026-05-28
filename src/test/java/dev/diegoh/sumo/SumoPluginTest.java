package dev.diegoh.sumo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SumoPluginTest {
    private ServerMock server;
    private SumoPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(SumoPlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void pluginEnablesSuccessfully() {
        assertNotNull(plugin);
        assertTrue(plugin.isEnabled());
    }

    @Test
    void rootCommandRegistered() {
        assertNotNull(server.getPluginCommand("sumo"));
    }
}
