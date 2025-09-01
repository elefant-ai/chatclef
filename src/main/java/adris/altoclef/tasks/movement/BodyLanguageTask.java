package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;

public class BodyLanguageTask extends Task {

    public enum Type { GREETING, NOD_HEAD, SHAKE_HEAD, VICTORY_DANCE, MY_DANCE }

    private final Type type;


    private final PrimitiveSequenceTask seqGreeting = makeGreeting();
    private final PrimitiveSequenceTask seqNodHead  = makeNodHead(2);
    private final PrimitiveSequenceTask seqShake    = makeShakeHead(2);
    private final PrimitiveSequenceTask seqVictory  = makeVictoryDance();

    private final PrimitiveSequenceTask seqMyDance =
            PrimitiveSequenceTask.builder()
                .hold(Input.SNEAK)
                .waitTicks(10)
                .release(Input.SNEAK)
                .jump() 
                .waitTicks(5)
                .lookRelative(new Rotation(40, 0), 5) 
                .build();

    private Task actuallyRunningTask;

    public BodyLanguageTask(String type) {
        this.type = parseType(type);
    }

    private static Type parseType(String t) {
        try { return Type.valueOf(t.toUpperCase()); }
        catch (IllegalArgumentException e) { return Type.GREETING; }
    }

    @Override
    protected void onStart() {
        switch (type) {
            case GREETING:
                actuallyRunningTask = seqGreeting;
                break;
            case NOD_HEAD:
                actuallyRunningTask = seqNodHead;
                break;
            case SHAKE_HEAD:
                actuallyRunningTask = seqShake;
                break;
            case VICTORY_DANCE:
                actuallyRunningTask = seqVictory;
                break;
            case MY_DANCE:
                actuallyRunningTask = seqMyDance;
                break;
        }
    }

    @Override
    protected Task onTick() {
        return actuallyRunningTask;
    }

    @Override
    public boolean isFinished() {
        return actuallyRunningTask != null && actuallyRunningTask.isFinished();
    }

    @Override
    protected boolean isEqual(Task other) {
        if (!(other instanceof BodyLanguageTask)) return false;
        BodyLanguageTask o = (BodyLanguageTask) other;
        return o.type == this.type;
    }

    @Override
    protected void onStop(Task next) {
        AltoClef mod = AltoClef.getInstance();
        mod.getInputControls().release(Input.SNEAK);
        mod.getInputControls().release(Input.JUMP);
        mod.getInputControls().release(Input.SPRINT);
        mod.getInputControls().release(Input.MOVE_FORWARD);
        mod.getInputControls().release(Input.MOVE_BACK);
        mod.getInputControls().release(Input.MOVE_LEFT);
        mod.getInputControls().release(Input.MOVE_RIGHT);
    }

    @Override
    protected String toDebugString() {
        return "BodyLanguage(" + type + ")";
    }


    private static PrimitiveSequenceTask makeGreeting() {
        PrimitiveSequenceTask.Sequence.Builder b = PrimitiveSequenceTask.builder();
        int bobCount = 3;
        int holdTicks = 6, pauseTicks = 6;
        for (int i = 0; i < bobCount; i++) {
            b.hold(Input.SNEAK).waitTicks(holdTicks).release(Input.SNEAK).waitTicks(pauseTicks);
        }
        return b.build();
    }

    private static PrimitiveSequenceTask makeNodHead(int nods) {
        PrimitiveSequenceTask.Sequence.Builder b = PrimitiveSequenceTask.builder();
        int ticks = 4;
        float amp = 15f;
        for (int i = 0; i < nods; i++) {
            b.lookRelative(new Rotation(0, +amp), ticks);
            b.lookRelative(new Rotation(0, -amp), ticks);
        }
        return b.build();
    }

    private static PrimitiveSequenceTask makeShakeHead(int shakes) {
        PrimitiveSequenceTask.Sequence.Builder b = PrimitiveSequenceTask.builder();
        int ticks = 4;
        float amp = 20f; 
        for (int i = 0; i < shakes; i++) {
            b.lookRelative(new Rotation(+amp, 0), ticks);
            b.lookRelative(new Rotation(-amp, 0), ticks);
        }
        return b.build();
    }

    private static PrimitiveSequenceTask makeVictoryDance() {
        return PrimitiveSequenceTask.builder()
                .jump()
                .waitTicks(4)
                .lookRelative(new Rotation(360f, 0), 20)   
                .hold(Input.SNEAK).waitTicks(6).release(Input.SNEAK)
                .jump()
                .build();
    }
}