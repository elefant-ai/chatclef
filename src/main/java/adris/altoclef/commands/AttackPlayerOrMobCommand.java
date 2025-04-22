package adris.altoclef.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.EntityDeathEvent;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AttackPlayerOrMobCommand extends Command {

    public AttackPlayerOrMobCommand() throws CommandException {
        super("attack", "Attacks a specified player or mob. Example usages: @attack zombie 5 to attack and kill 5 zombies, @attack Player to attack a player with username=Player", new Arg<>(String.class, "name"), new Arg<>(Integer.class, "count", 1, 1));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String nameToAttack = parser.get(String.class);
        int countToAttack = parser.get(Integer.class);

        mod.runUserTask(new AttackAndGetDropsTask(nameToAttack, countToAttack), this::finish);
    }

    private static class AttackAndGetDropsTask extends ResourceTask {

        private final String _toKill;
    
        private final Task _killTask;

        private int _mobsKilledCount;

        private int _mobKillTargetCount;

        private TimerGame _forceCollectTimer = new TimerGame(2);

        private Subscription<EntityDeathEvent> _onMobDied;

        private Predicate<Entity> _shouldAttackPredicate;

        private Set<Entity> _trackedDeadEntities = new HashSet<>();

        private static ItemTarget[] drops = new ItemTarget[]{
            new ItemTarget("rotten_flesh", 9999),
            new ItemTarget("bone", 9999),
            new ItemTarget("string", 9999),
            new ItemTarget("spider_eye", 9999),
            new ItemTarget("gunpowder", 9999),
            new ItemTarget("slime_ball", 9999),
            new ItemTarget("ender_pearl", 9999),
            new ItemTarget("blaze_powder", 9999),
            new ItemTarget("ghast_tear", 9999),
            new ItemTarget("magma_cream", 9999),
            new ItemTarget("ender_eye", 9999),
            new ItemTarget("speckled_melon", 9999),
            new ItemTarget("gold_nugget", 9999),
            new ItemTarget("iron_nugget", 9999),
            new ItemTarget("porkchop", 9999),
            new ItemTarget("beef", 9999),
            new ItemTarget("chicken", 9999),
            new ItemTarget("mutton", 9999),
            new ItemTarget("rabbit", 9999),
    };

        public AttackAndGetDropsTask(String toKill, int killCount) {
            super(drops);
            _toKill = toKill;
            _mobKillTargetCount = killCount;

            _shouldAttackPredicate = (entity) -> {
                // Done, don't attack any mobs just collect
                if (_mobsKilledCount >= _mobKillTargetCount) {
                    return false;
                }

                // Attack players possibly
                if (entity instanceof PlayerEntity) {
                    String playerName = entity.getName().getString();
                    if (playerName != null && playerName.equalsIgnoreCase(_toKill)) {
                        return true;
                    }
                }

                // entity type match
                String name = entity.getType().getUntranslatedName();
                return name != null && name.equals(_toKill);
            };

            // Kill any entity matches our name, or if it's a player their username.
            _killTask = new KillEntitiesTask(_shouldAttackPredicate);// new KillEntitiesTask(shouldKill, _toKill);
        }

        @Override
        protected boolean shouldAvoidPickingUp(AltoClef mod) {
            return false;
        }

        @Override
        protected void onResourceStart(AltoClef mod) {
            _forceCollectTimer.reset();
            // TODO: Also consider if the target player entity is NOT alive, in the event we're dealing with players

            _onMobDied = EventBus.subscribe(EntityDeathEvent.class, evt -> {
                Entity diedEntity = evt.entity;

                // don't double count
                if (_trackedDeadEntities.contains(diedEntity)) {
                    return;
                }

                if (_shouldAttackPredicate.test(diedEntity)) {
                    markEntityDead(diedEntity);
                }
            });
        }

        private void markEntityDead(Entity entity) {
            // newly dead!
            _trackedDeadEntities.add(entity);
            _mobsKilledCount++;
        }

        @Override
        public boolean isFinished() {
            // We've killed enough of the mobs AND our timer has gone...
            return _mobsKilledCount >= _mobKillTargetCount && _forceCollectTimer.elapsed();
        }

        @Override
        protected Task onResourceTick(AltoClef mod) {

            // If our target is a player, consider dead players that match to add to our counter
            for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
                if (_trackedDeadEntities.contains(entity)) {
                    // don't double count
                    continue;
                }

                if (_shouldAttackPredicate.test(entity)) {
                    if (!entity.isAlive()) {
                        markEntityDead(entity);
                    }
                }
            }
            // Clear remaining not attack predicate entitites
            _trackedDeadEntities.removeIf(entity -> entity.isAlive());

            if (_mobsKilledCount < _mobKillTargetCount) {
                _forceCollectTimer.reset();
            }

            return _killTask;
        }

        @Override
        protected void onResourceStop(AltoClef mod, Task interruptTask) {
            EventBus.unsubscribe(_onMobDied);
            _trackedDeadEntities.clear();
        }
    
        @Override
        protected boolean isEqualResource(ResourceTask other) {
            if (other instanceof AttackAndGetDropsTask task) {
                return task._toKill.equals(_toKill) && task._mobKillTargetCount == _mobKillTargetCount;
            }
            return false;
        }
    
        @Override
        protected String toDebugStringName() {
            return "Attacking and collect items from " + _toKill + " x " + _mobKillTargetCount;
        }
    }
    
}