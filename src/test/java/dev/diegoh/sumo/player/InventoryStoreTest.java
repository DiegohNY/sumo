package dev.diegoh.sumo.player;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InventoryStoreTest {
  private ServerMock server;
  private InventoryStore store;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
    store = new InventoryStore();
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void capturedInventoryRestoresContents() {
    PlayerMock p = server.addPlayer();
    p.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
    p.setGameMode(GameMode.SURVIVAL);

    store.capture(p);
    p.getInventory().clear();
    p.setGameMode(GameMode.SPECTATOR);

    store.restore(p);
    assertEquals(Material.DIAMOND_SWORD, p.getInventory().getItem(0).getType());
    assertEquals(GameMode.SURVIVAL, p.getGameMode());
  }

  @Test
  void restoreWithoutCaptureIsNoop() {
    PlayerMock p = server.addPlayer();
    p.getInventory().setItem(0, new ItemStack(Material.STICK));
    store.restore(p);
    assertEquals(Material.STICK, p.getInventory().getItem(0).getType());
  }
}
