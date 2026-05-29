package dev.diegoh.sumo.queue;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MatchmakingQueueTest {

  @Test
  void addAssignsFifoPositions() {
    MatchmakingQueue q = new MatchmakingQueue("main");
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    assertTrue(q.add(a));
    assertTrue(q.add(b));
    assertEquals(1, q.position(a));
    assertEquals(2, q.position(b));
    assertEquals(2, q.size());
  }

  @Test
  void duplicateAddIsRejected() {
    MatchmakingQueue q = new MatchmakingQueue("main");
    UUID a = UUID.randomUUID();
    assertTrue(q.add(a));
    assertFalse(q.add(a));
    assertEquals(1, q.size());
  }

  @Test
  void removeDropsEntry() {
    MatchmakingQueue q = new MatchmakingQueue("main");
    UUID a = UUID.randomUUID();
    q.add(a);
    assertTrue(q.remove(a));
    assertEquals(-1, q.position(a));
    assertEquals(0, q.size());
  }

  @Test
  void drainTakesUpToMaxInOrder() {
    MatchmakingQueue q = new MatchmakingQueue("main");
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    UUID c = UUID.randomUUID();
    q.add(a);
    q.add(b);
    q.add(c);
    List<UUID> first = q.drain(2);
    assertEquals(List.of(a, b), first);
    assertEquals(1, q.size());
    assertEquals(1, q.position(c));
  }
}
