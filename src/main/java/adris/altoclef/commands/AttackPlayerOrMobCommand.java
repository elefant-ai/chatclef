package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.resources.CollectMeatTask;

public class AttackPlayerOrMobCommand extends Command {

    public AttackPlayerOrMobCommand() throws CommandException {
        super("attack", "Attacks a specified player or mob. Example usages: @attack zombie 5 to attack and kill 5 zombies, @attack Player to attack a player with username=Player", new Arg<>(String.class, "name"), new Arg<>(Integer.class, "count"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String nameToAttack = parser.get(String.class);

        // TODO: Figure out when a mob is defeated and increment our count accordingly
        
        // mod.runUserTask(new CollectMeatTask(parser.get(Integer.class)), this::finish);
    }

    static class AttackAndGetDropsTask extends ResourceTask {

        private final String _toKill;
    
        private final Task _killTask;

        private int _mobsKilledCount;

        private int _mobKillTargetCount;

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
    };



        public KillAndLootTask(String toKill, int killCount) {
            super(drops);
            _toKill = toKill;
            _mobKillTargetCount = killCount;

            // TODO: If mob, kill, otherwise player

            _killTask = null;// new KillEntitiesTask(shouldKill, _toKill);
        }
        
        @Override
        protected boolean shouldAvoidPickingUp(AltoClef mod) {
            return false;
        }
    
        @Override
        protected void onResourceStart(AltoClef mod) {
            // _mobsKilledCount = 0;
            // TODO: Eventbus subscribe to mob killed event
        }

        @Override
        public boolean isFinished() {
            // We've killed enough of the mobs
            return _mobsKilledCount > _mobKillTargetCount;
        }
    
        @Override
        protected Task onResourceTick(AltoClef mod) {
            if (!mod.getEntityTracker().entityFound(_toKill)) {
                if (isInWrongDimension(mod)) {
                    setDebugState("Going to correct dimension.");
                    return getToCorrectDimensionTask(mod);
                }
                setDebugState("Searching for mob...");
                return new TimeoutWanderTask();
            }
            // We found the mob!
            return _killTask;
        }
    
        @Override
        protected void onResourceStop(AltoClef mod, Task interruptTask) {
                // TODO: Eventbus unsubscribe to mob killed event
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
            return "Collect items from " + _toKill.toGenericString();
        }
    }
    
}