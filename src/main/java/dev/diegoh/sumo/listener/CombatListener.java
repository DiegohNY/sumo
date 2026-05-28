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
    private final GameOrchestrator orchestrator;

    public CombatListener(GameOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        GameSession s = orchestrator.sessionOf(victim).orElse(null);
        if (s == null || s.state() != GameState.ACTIVE) {
            event.setCancelled(true);
            return;
        }
        if (orchestrator.sessionOf(attacker).orElse(null) != s) {
            event.setCancelled(true);
            return;
        }
        event.setDamage(0);
        Vector dir =
                victim.getLocation().toVector().subtract(attacker.getLocation().toVector());
        if (dir.lengthSquared() < 1e-6) dir = new Vector(0, 0, 1);
        KnockbackConfig kb = s.arena().knockback();
        Vector kbVector = dir.normalize().multiply(kb.strength()).setY(kb.verticalBoost());
        victim.setVelocity(victim.getVelocity().multiply(kb.friction()).add(kbVector));
    }
}
