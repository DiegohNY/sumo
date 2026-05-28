package dev.diegoh.sumo.arena;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import java.nio.file.Path;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArenaServiceTest {
  private ServerMock server;
  private WorldMock world;
  @TempDir Path tmp;
  private ArenaService service;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
    world = server.addSimpleWorld("world");
    service = new ArenaService(new ArenaRepository(tmp, server));
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void createPersistsArena() {
    Arena created = service.create("main", sample()).orElseThrow();
    assertEquals("main", created.id());
    assertTrue(service.find("main").isPresent());
  }

  @Test
  void createFailsOnDuplicateId() {
    service.create("main", sample()).orElseThrow();
    assertTrue(service.create("main", sample()).isEmpty());
  }

  @Test
  void deleteRemoves() {
    service.create("main", sample()).orElseThrow();
    assertTrue(service.delete("main"));
    assertTrue(service.find("main").isEmpty());
  }

  @Test
  void rejectsInvalidId() {
    assertTrue(service.create("bad id!", sample()).isEmpty());
  }

  private Arena.Builder sample() {
    Location a = new Location(world, 0, 64, 5);
    Location b = new Location(world, 0, 64, -5);
    Location lobby = new Location(world, 0, 80, 0);
    return Arena.builder()
        .spawnA(a)
        .spawnB(b)
        .lobby(lobby)
        .bounds(ArenaBounds.cylinder(lobby, 10.0))
        .knockback(new KnockbackConfig(1.0, 0.4, 0.5))
        .minPlayers(2)
        .maxPlayers(8);
  }
}
