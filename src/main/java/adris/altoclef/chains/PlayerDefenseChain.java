package adris.altoclef.chains;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.PlayerDamageEvent;
import adris.altoclef.eventbus.events.EntitySwungEvent;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.time.TimerGame;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.util.helpers.LookHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class PlayerDefenseChain extends SingleTaskChain {

    private Map<String, DamageTarget> _damageTargets = new HashMap<>();

    // TODO: These happen out of order from damage. consider them after the fact.
    // entities that swung recently mapped by their entityId
    private Map<Integer, TimerGame> _recentlySwung = new HashMap<>();
    private TimerGame _recentlyDamagedUnknown = new TimerGame(0.3);

    private String _currentlyAttackingPlayer = null;

    // TODO: CONFIG
    private static int HITS_BEFORE_RETALIATION = 2;
    private static int HITS_BEFORE_RETALIATION_LOW_HEALTH = 1;
    private static int LOW_HEALTH_THRESHOLD = 14; // 70%

    private static double SWING_TIMEOUT = 0.4;

    public PlayerDefenseChain(TaskRunner runner) {
        super(runner);
        EventBus.subscribe(PlayerDamageEvent.class, evt -> onPlayerDamage(evt.source.getAttacker()));
        EventBus.subscribe(EntitySwungEvent.class, evt -> onEntitySwung(evt.entity));
    }

    private void processMaybeDamaged() {
        if (_recentlyDamagedUnknown == null || _recentlyDamagedUnknown.elapsed()) {
            _recentlyDamagedUnknown = null;
            return;
        }
        // Process
        _recentlyDamagedUnknown = null;

        // Try INFERRING based on looking players that recently swung their hands
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        // Integer[] swungEntities = _damageTargets.keySet().toArray(Integer[]::new);
        for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
            // Entity entity = MinecraftClient.getInstance().world.getEntityById(swungEntityId);
            // System.out.println("checking: " + entity);
            if (entity == null || (_recentlySwung.containsKey(entity.getId()) && _recentlySwung.get(entity.getId()).elapsed())) {
                _recentlySwung.remove(entity.getId());
                continue;
            }
            if (entity == null) {
                continue;
            }
            if (entity.distanceTo(player) > 5) {
                continue;
            }
            Vec3d playerCenter = player.getPos().add(new Vec3d(0, player.getStandingEyeHeight(), 0));
            if (entity.isAlive() && LookHelper.isLookingAt(entity, playerCenter, 60)) {
                // Consider this entity
                _recentlySwung.remove(entity.getId());
                onPlayerDamage(entity);
                return;
            }
        }
        // clear where the timeouts are too big
    }

    // Keep track of swinging entities
    private void onEntitySwung(Entity entity) {
        int id = entity.getId();
        // System.out.println("SWUNG" + id);
        TimerGame timeout = new TimerGame(SWING_TIMEOUT);
        timeout.reset();
        _recentlySwung.put(id, timeout);

        processMaybeDamaged();
    }

    private void onPlayerDamage(Entity damagedBy) {
        if (damagedBy == null) {
            // System.out.println("recently damaged");
            // unknown. Perform inferrence.
            if (_recentlyDamagedUnknown == null || _recentlyDamagedUnknown.elapsed()) {
                _recentlyDamagedUnknown = new TimerGame(0.3);
                _recentlyDamagedUnknown.reset();                    
            }
            processMaybeDamaged();
            return;
        }

        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;

        // don't do the inferrence, just accept
        _recentlyDamagedUnknown = null;
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
                int hitsBeforeRetaliation = clientPlayer.getHealth() < LOW_HEALTH_THRESHOLD ? HITS_BEFORE_RETALIATION_LOW_HEALTH : HITS_BEFORE_RETALIATION;
                System.out.println("Another player hit us " + target.timesHit + "times: " + offendingName + ", attacking if they hit us " + (hitsBeforeRetaliation - target.timesHit) + " more time(s).");
                if (target.timesHit >= hitsBeforeRetaliation) {
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
                if (potentialAttacker.equals(_currentlyAttackingPlayer)) {
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
