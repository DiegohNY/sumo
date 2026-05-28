package dev.diegoh.sumo.config;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocationCodecTest {
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
  void roundTripPreservesLocation() {
    Location loc = new Location(world, 12.5, 64.0, -33.0, 90.0f, 45.0f);
    String encoded = LocationCodec.encode(loc);
    Location decoded = LocationCodec.decode(encoded, server).orElseThrow();
    assertEquals(loc.getWorld(), decoded.getWorld());
    assertEquals(loc.getX(), decoded.getX());
    assertEquals(loc.getY(), decoded.getY());
    assertEquals(loc.getZ(), decoded.getZ());
    assertEquals(loc.getYaw(), decoded.getYaw());
    assertEquals(loc.getPitch(), decoded.getPitch());
  }

  @Test
  void decodeReturnsEmptyOnMalformedInput() {
    assertTrue(LocationCodec.decode("garbage", server).isEmpty());
    assertTrue(LocationCodec.decode("world,1,2", server).isEmpty());
    assertTrue(LocationCodec.decode(null, server).isEmpty());
  }

  @Test
  void decodeReturnsEmptyWhenWorldMissing() {
    assertTrue(LocationCodec.decode("missing_world,0,64,0,0,0", server).isEmpty());
  }
}
