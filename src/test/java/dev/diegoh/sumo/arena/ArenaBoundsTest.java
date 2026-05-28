package dev.diegoh.sumo.arena;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArenaBoundsTest {
    private ServerMock server;
    private WorldMock world;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void cylinderContainsCenter() {
        ArenaBounds bounds = ArenaBounds.cylinder(new Location(world, 0, 64, 0), 10.0);
        assertTrue(bounds.contains(new Location(world, 0, 64, 0)));
    }

    @Test
    void cylinderExcludesOutside() {
        ArenaBounds bounds = ArenaBounds.cylinder(new Location(world, 0, 64, 0), 5.0);
        assertFalse(bounds.contains(new Location(world, 10, 64, 0)));
    }

    @Test
    void cylinderRejectsOtherWorld() {
        ArenaBounds bounds = ArenaBounds.cylinder(new Location(world, 0, 64, 0), 5.0);
        WorldMock other = server.addSimpleWorld("nether");
        assertFalse(bounds.contains(new Location(other, 0, 64, 0)));
    }
}
