package dev.diegoh.sumo.arena;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArenaTest {
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
    void buildsArenaWithValidSpawns() {
        Location a = new Location(world, 0, 64, 5);
        Location b = new Location(world, 0, 64, -5);
        Location lobby = new Location(world, 0, 80, 0);
        Arena arena =
                Arena.builder()
                        .id("main")
                        .spawnA(a)
                        .spawnB(b)
                        .lobby(lobby)
                        .bounds(ArenaBounds.cylinder(lobby, 15.0))
                        .knockback(new KnockbackConfig(1.0, 0.4, 0.5))
                        .minPlayers(2)
                        .maxPlayers(8)
                        .build();
        assertEquals("main", arena.id());
        assertTrue(arena.bounds().contains(a));
    }

    @Test
    void rejectsCrossWorldSpawns() {
        Location a = new Location(world, 0, 64, 5);
        Location b = new Location(server.addSimpleWorld("nether"), 0, 64, -5);
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        Arena.builder()
                                .id("bad")
                                .spawnA(a)
                                .spawnB(b)
                                .lobby(a)
                                .bounds(ArenaBounds.cylinder(a, 10.0))
                                .knockback(new KnockbackConfig(1.0, 0.4, 0.5))
                                .minPlayers(2)
                                .maxPlayers(8)
                                .build());
    }
}
