package dev.diegoh.sumo.queue;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class QueueServiceTest {
  @Test
  void joiningQueueAssignsPosition() {
    QueueService svc = new QueueService();
    UUID p1 = UUID.randomUUID();
    UUID p2 = UUID.randomUUID();
    assertEquals(1, svc.join("alpha", p1));
    assertEquals(2, svc.join("alpha", p2));
  }

  @Test
  void playerCanOnlyQueueOnce() {
    QueueService svc = new QueueService();
    UUID p1 = UUID.randomUUID();
    svc.join("alpha", p1);
    assertEquals(-1, svc.join("alpha", p1));
    assertEquals(-1, svc.join("bravo", p1));
  }

  @Test
  void leavingRemovesFromAnyQueue() {
    QueueService svc = new QueueService();
    UUID p1 = UUID.randomUUID();
    svc.join("alpha", p1);
    assertTrue(svc.leave(p1));
    assertEquals(0, svc.size("alpha"));
  }

  @Test
  void drainReturnsAndClearsCurrent() {
    QueueService svc = new QueueService();
    UUID p1 = UUID.randomUUID();
    UUID p2 = UUID.randomUUID();
    svc.join("alpha", p1);
    svc.join("alpha", p2);
    var drained = svc.drain("alpha", 2);
    assertEquals(2, drained.size());
    assertEquals(1, svc.join("alpha", p1));
  }
}
