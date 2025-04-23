package adris.altoclef.chains;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.PlayerDamageEvent;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import adris.altoclef.tasks.entity.KillPlayerTask;

// TODO: Get attack events, if another player then consider attacking them for a period of time
public class PlayerDefenseChain extends SingleTaskChain {

    private Map<String, DamageTarget> _damageTargets = new HashMap<>();

    private String _currentlyAttackingPlayer = null;

    // TODO: CONFIG
    private static int HITS_BEFORE_RETALIATION = 3;

    public PlayerDefenseChain(TaskRunner runner) {
        super(runner);
        EventBus.subscribe(PlayerDamageEvent.class, evt -> onPlayerDamage(evt.source));
    }

    private void onPlayerDamage(DamageSource source) {
        // TODO: Process and target player
        Entity damagedBy = source.getAttacker();
        if (damagedBy == null) {
            return;
        }
        if (damagedBy instanceof PlayerEntity player) {
            String offendingName = player.getName().getString();

            if (!_damageTargets.containsKey(offendingName)) {
                _damageTargets.put(offendingName, new DamageTarget());
            }
            DamageTarget target = _damageTargets.get(offendingName);
            // Check if timed out
            if (target.forgetInstigationTimer.elapsed()) {
                target.timesHit = 0;
            }
            if (target.forgetAttackTimer.elapsed()) {
                target.attacking = false;
            }

            target.forgetInstigationTimer.reset();
            if (!target.attacking) {
                target.timesHit++;
                System.out.println("Another player hit us " + target.timesHit + "times: " + offendingName + ", attacking if they hit us " + (HITS_BEFORE_RETALIATION - target.timesHit) + " more time(s).");
                if (target.timesHit >= HITS_BEFORE_RETALIATION) {
                    System.out.println("Too many attacks from another player! Retaliating attacks against offending player: " + offendingName);
                    target.attacking = true;
                    target.forgetAttackTimer.reset();
                    target.timesHit = 0;
                    // Always attack the most recently attacked entity, to keep things simple
                    _currentlyAttackingPlayer = offendingName;
                }
            } else {
                // we're already attacking, reset the chase timer since we got hit again
                target.forgetAttackTimer.reset();
            }
        }
    }

    @Override
    public float getPriority() {
        if (_currentlyAttackingPlayer != null) {
            Optional<PlayerEntity> currentPlayerEntity = AltoClef.getInstance().getEntityTracker().getPlayerEntity(_currentlyAttackingPlayer);
            // dead trigger: we're done
            if (!currentPlayerEntity.isPresent() || !currentPlayerEntity.get().isAlive()) {
                _currentlyAttackingPlayer = null;
            }
        }
        // dead OR forgot triggers for other entities: done
        String[] playerNames = _damageTargets.keySet().toArray(String[]::new);
        for (String potentialAttacker : playerNames) {
            if (potentialAttacker == null) {
                _damageTargets.remove(potentialAttacker);
                continue;
            }

            PlayerEntity potentialPlayer = AltoClef.getInstance().getEntityTracker().getPlayerEntity(potentialAttacker).orElse(null);

            if (potentialPlayer == null || (!potentialPlayer.isAlive() || _damageTargets.get(potentialAttacker).forgetAttackTimer.elapsed())) {
                System.out.println("Either forgot or killed player: " + potentialAttacker + " (no longer attacking)");
                _damageTargets.remove(potentialAttacker);
                if (potentialAttacker == _currentlyAttackingPlayer) {
                    _currentlyAttackingPlayer = null;
                }
            }
        }

        if (_currentlyAttackingPlayer != null) {
            setTask(new KillPlayerTask(_currentlyAttackingPlayer));
            return 55;
        }
        return 0;
    }

    @Override
    public boolean isActive() {
        // We're always checking for player attacks
        return true;
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        // Task is done, so I guess we move on?
    }

    @Override
    public String getName() {
        return "Player Defense";
    }

    static class DamageTarget {
        public TimerGame forgetInstigationTimer = new TimerGame(6);
        public TimerGame forgetAttackTimer = new TimerGame(30);
        public int timesHit = 0;
        public boolean attacking = false;

        public DamageTarget() {
            // init timers
            forgetInstigationTimer.reset();
            forgetAttackTimer.reset();
        }
    }
}
