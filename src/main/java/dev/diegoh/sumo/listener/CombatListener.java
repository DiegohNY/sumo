package dev.diegoh.sumo.listener;

import dev.diegoh.sumo.arena.KnockbackConfig;
import dev.diegoh.sumo.game.GameOrchestrator;
import dev.diegoh.sumo.game.GameSession;
import dev.diegoh.sumo.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public final class CombatListener implements Listener {
  private static final double EPSILON = 1.0e-4;

  private final GameOrchestrator orchestrator;

  public CombatListener(GameOrchestrator orchestrator) {
    this.orchestrator = orchestrator;
  }

  @EventHandler
  public void onHit(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player victim)) return;
    GameSession s = orchestrator.sessionOf(victim).orElse(null);
    if (s == null) return; // victim isn't in a game — leave normal combat alone.

    // The victim IS in a game: the ONLY damage allowed is a hit from their current opponent while
    // the round is live. Everything else (mobs, projectiles, players in another arena, hits during
    // countdown/lobby) is cancelled so nothing can hurt a fighter outside the push mechanic.
    boolean validSumoHit =
        s.state() == GameState.ACTIVE
            && event.getDamager() instanceof Player attacker
            && orchestrator.sessionOf(attacker).orElse(null) == s;
    if (!validSumoHit) {
      event.setCancelled(true);
      return;
    }

    Player attacker = (Player) event.getDamager();
    event.setDamage(0);
    applyKnockback(victim, attacker, s.arena().knockback());
  }

  /** Crisp arcade-style push: horizontal direction away from the attacker, plus a vertical pop. */
  private void applyKnockback(Player victim, Player attacker, KnockbackConfig kb) {
    Vector dir =
        victim.getLocation().toVector().subtract(attacker.getLocation().toVector()).setY(0);
    if (dir.lengthSquared() < EPSILON) {
      // Players overlap exactly: push along the attacker's facing instead.
      dir = attacker.getLocation().getDirection().setY(0);
      if (dir.lengthSquared() < EPSILON) {
        dir = new Vector(0, 0, 1);
      }
    }
    dir.normalize().multiply(kb.strength());
    dir.setY(kb.verticalBoost());
    victim.setVelocity(dir);
  }
}
