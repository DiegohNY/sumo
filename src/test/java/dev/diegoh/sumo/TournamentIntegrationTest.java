package dev.diegoh.sumo;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.diegoh.sumo.arena.Arena;
import dev.diegoh.sumo.arena.ArenaBounds;
import dev.diegoh.sumo.arena.KnockbackConfig;
import dev.diegoh.sumo.game.GameOrchestrator;
import dev.diegoh.sumo.game.GameSession;
import dev.diegoh.sumo.game.GameState;
import dev.diegoh.sumo.player.InventoryStore;
import dev.diegoh.sumo.player.SessionRegistry;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TournamentIntegrationTest {
    private ServerMock server;
    private WorldMock world;
    private SumoPlugin plugin;
    private Arena arena;
    private GameOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        plugin = MockBukkit.load(SumoPlugin.class);
        arena =
                Arena.builder()
                        .id("main")
                        .spawnA(new Location(world, 0, 64, 5))
                        .spawnB(new Location(world, 0, 64, -5))
                        .lobby(new Location(world, 0, 80, 0))
                        .bounds(ArenaBounds.cylinder(new Location(world, 0, 64, 0), 15.0))
                        .knockback(new KnockbackConfig(1.0, 0.4, 0.5))
                        .minPlayers(2)
                        .maxPlayers(4)
                        .build();
        orchestrator =
                new GameOrchestrator(plugin, new InventoryStore(), new SessionRegistry());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void fourPlayerTournamentProducesSingleWinner() {
        PlayerMock[] players = {
            server.addPlayer(), server.addPlayer(), server.addPlayer(), server.addPlayer()
        };
        for (PlayerMock p : players) assertTrue(orchestrator.join(arena, p));

        GameSession session = orchestrator.sessionForArena("main").orElseThrow();
        assertTrue(session.startTournament());

        for (int round = 0; round < 3; round++) {
            session.skipCountdownForTesting();
            session.recordElimination(session.currentMatch().orElseThrow().playerB());
        }
        assertEquals(GameState.ENDING, session.state());
        assertTrue(session.winner().isPresent());
    }

    @Test
    void twoSimultaneousSessionsRunIndependently() {
        Arena alpha =
                Arena.builder()
                        .id("alpha")
                        .spawnA(new Location(world, 100, 64, 5))
                        .spawnB(new Location(world, 100, 64, -5))
                        .lobby(new Location(world, 100, 80, 0))
                        .bounds(ArenaBounds.cylinder(new Location(world, 100, 64, 0), 10.0))
                        .knockback(new KnockbackConfig(1.0, 0.4, 0.5))
                        .minPlayers(2)
                        .maxPlayers(2)
                        .build();
        Arena bravo =
                Arena.builder()
                        .id("bravo")
                        .spawnA(new Location(world, 200, 64, 5))
                        .spawnB(new Location(world, 200, 64, -5))
                        .lobby(new Location(world, 200, 80, 0))
                        .bounds(ArenaBounds.cylinder(new Location(world, 200, 64, 0), 10.0))
                        .knockback(new KnockbackConfig(1.0, 0.4, 0.5))
                        .minPlayers(2)
                        .maxPlayers(2)
                        .build();
        PlayerMock a1 = server.addPlayer();
        PlayerMock a2 = server.addPlayer();
        PlayerMock b1 = server.addPlayer();
        PlayerMock b2 = server.addPlayer();
        orchestrator.join(alpha, a1);
        orchestrator.join(alpha, a2);
        orchestrator.join(bravo, b1);
        orchestrator.join(bravo, b2);
        assertEquals(2, orchestrator.activeSessions().size());

        GameSession sa = orchestrator.sessionForArena("alpha").orElseThrow();
        GameSession sb = orchestrator.sessionForArena("bravo").orElseThrow();
        assertTrue(sa.startTournament());
        assertTrue(sb.startTournament());
        sa.skipCountdownForTesting();
        sa.recordElimination(sa.currentMatch().orElseThrow().playerB());
        assertEquals(GameState.ENDING, sa.state());
        // Other session unaffected.
        assertEquals(GameState.COUNTDOWN, sb.state());
    }
}
