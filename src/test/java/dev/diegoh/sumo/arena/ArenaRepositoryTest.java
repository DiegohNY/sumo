package dev.diegoh.sumo.arena;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import java.nio.file.Path;
import java.util.Collection;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArenaRepositoryTest {
  private ServerMock server;
  private WorldMock world;
  @TempDir Path tmp;

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
  void saveThenLoadYieldsSameArena() {
    ArenaRepository repo = new ArenaRepository(tmp, server);
    Arena arena = sampleArena();
    repo.save(arena);
    ArenaRepository reloaded = new ArenaRepository(tmp, server);
    reloaded.loadAll();
    Arena loaded = reloaded.find("main").orElseThrow();
    assertEquals(arena.id(), loaded.id());
    assertEquals(arena.spawnA().getX(), loaded.spawnA().getX());
    assertEquals(arena.knockback(), loaded.knockback());
    assertEquals(arena.bounds().radius(), loaded.bounds().radius());
  }

  @Test
  void deleteRemovesFile() {
    ArenaRepository repo = new ArenaRepository(tmp, server);
    repo.save(sampleArena());
    repo.delete("main");
    Collection<Arena> all = repo.all();
    assertTrue(all.isEmpty());
  }

  private Arena sampleArena() {
    Location a = new Location(world, 1, 64, 5);
    Location b = new Location(world, 1, 64, -5);
    Location lobby = new Location(world, 1, 80, 0);
    return Arena.builder()
        .id("main")
        .spawnA(a)
        .spawnB(b)
        .lobby(lobby)
        .bounds(ArenaBounds.cylinder(lobby, 12.0))
        .knockback(new KnockbackConfig(1.2, 0.5, 0.6))
        .minPlayers(2)
        .maxPlayers(6)
        .build();
  }
}
