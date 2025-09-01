package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;

import java.util.ArrayDeque;
import java.util.Deque;

public class PrimitiveSequenceTask extends Task {

    interface Step {
        void start(AltoClef mod);

        // true when done
        boolean tick(AltoClef mod);

        default void stop(AltoClef mod) {
        }
    }

    static final class TickCountdown {
        private int remaining;

        TickCountdown(int ticks) {
            this.remaining = Math.max(0, ticks);
        }

        boolean isDone() {
            return remaining <= 0;
        }

        void dec() {
            if (remaining > 0)
                remaining--;
        }
    }

    // immediate
    static final class HoldInputStep implements Step {
        private final Input input;

        HoldInputStep(Input input) {
            this.input = input;
        }

        @Override
        public void start(AltoClef mod) {
            mod.getInputControls().hold(input);
        }

        @Override
        public boolean tick(AltoClef mod) {
            return true;
        }
    }

    // also immediate
    static final class ReleaseInputStep implements Step {
        private final Input input;

        ReleaseInputStep(Input input) {
            this.input = input;
        }

        @Override
        public void start(AltoClef mod) {
            mod.getInputControls().release(input);
        }

        @Override
        public boolean tick(AltoClef mod) {
            return true;
        }
    }

    static final class WaitTicksStep implements Step {
        private final TickCountdown timer;

        WaitTicksStep(int ticks) {
            this.timer = new TickCountdown(ticks);
        }

        @Override
        public void start(AltoClef mod) {
        }

        @Override
        public boolean tick(AltoClef mod) {
            timer.dec();
            return timer.isDone();
        }
    }

    static final class JumpStep implements Step {
        private static int holdTicks = 3;
        private int ticksHeld;

        @Override
        public void start(AltoClef mod) {
            ticksHeld = 0;
            mod.getInputControls().hold(Input.JUMP);
        }

        @Override
        public boolean tick(AltoClef mod) {
            ticksHeld++;
            if (ticksHeld >= holdTicks) {
                mod.getInputControls().release(Input.JUMP);
                return true;
            }
            return false;
        }

        @Override
        public void stop(AltoClef mod) {
            mod.getInputControls().release(Input.JUMP);
        }
    }

    // looks not immediate but over time
    static final class LookOverTimeStep implements Step {
        private final int totalTicks;
        private int elapsed;
        private Rotation start;
        private Rotation target;
        private final Rotation delta; // iff relative
        private final boolean relative;

        LookOverTimeStep(Rotation targetAbs, int ticks) {
            this.totalTicks = Math.max(1, ticks);
            this.relative = false;
            this.delta = null;
            this.target = targetAbs;
        }

        LookOverTimeStep(Rotation dRot, int ticks, boolean relative) {
            this.totalTicks = Math.max(1, ticks);
            this.relative = relative;
            this.delta = dRot;
        }

        @Override
        public void start(AltoClef mod) {
            elapsed = 0;
            start = LookHelper.getLookRotation();

            if (relative) {
                Rotation raw = start.add(delta);
                target = new Rotation(Rotation.normalizeYaw(raw.getYaw()), Rotation.clampPitch(raw.getPitch()));
            } else {
                target = new Rotation(Rotation.normalizeYaw(target.getYaw()), Rotation.clampPitch(target.getPitch()));
            }

            if (totalTicks == 1 || totalTicks == 0)
                // 1/0 ticks => instantly snap to target
                LookHelper.lookAt(target);
        }

        @Override
        public boolean tick(AltoClef mod) {
            elapsed++;
            float alpha = Math.min(1f, (float) elapsed / (float) totalTicks);
            Rotation desired = interpolate(start, target, alpha);
            LookHelper.lookAt(desired);
            return elapsed >= totalTicks;
        }

        private static Rotation interpolate(Rotation a, Rotation b, float t) {
            float aYaw = Rotation.normalizeYaw(a.getYaw());
            float bYaw = Rotation.normalizeYaw(b.getYaw());
            float yawDelta = shortestYawDelta(aYaw, bYaw);
            float yaw = Rotation.normalizeYaw(aYaw + yawDelta * t);

            float pitchDelta = b.getPitch() - a.getPitch();
            float pitch = Rotation.clampPitch(a.getPitch() + pitchDelta * t);

            return new Rotation(yaw, pitch);
        }

        private static float shortestYawDelta(float from, float to) {
            float diff = Rotation.normalizeYaw(to) - Rotation.normalizeYaw(from);
            if (diff > 180f)
                diff -= 360f;
            if (diff < -180f)
                diff += 360f;
            return diff;
        }
    }


    public static final class Sequence {
        private final Deque<Step> steps = new ArrayDeque<>();

        private Sequence() {
        }

        public static Builder builder() {
            return new Builder();
        }

        Deque<Step> buildSteps() {
            return steps;
        }

        public static final class Builder {
            private final Sequence seq = new Sequence();

            public Builder hold(Input input) {
                seq.steps.add(new HoldInputStep(input));
                return this;
            }

            public Builder release(Input input) {
                seq.steps.add(new ReleaseInputStep(input));
                return this;
            }

            public Builder waitTicks(int ticks) {
                seq.steps.add(new WaitTicksStep(ticks));
                return this;
            }

            public Builder jump() {
                seq.steps.add(new JumpStep());
                return this;
            }

            public Builder lookAbs(Rotation target, int ticks) {
                seq.steps.add(new LookOverTimeStep(target, ticks));
                return this;
            }

            public Builder lookRelative(Rotation delta, int ticks) {
                seq.steps.add(new LookOverTimeStep(delta, ticks, true));
                return this;
            }

            public PrimitiveSequenceTask build() {
                return new PrimitiveSequenceTask(seq);
            }
        }
    }


    private final Deque<Step> steps = new ArrayDeque<>();
    private Step current;
    private boolean started = false;

    private PrimitiveSequenceTask(Sequence seq) {
        this.steps.addAll(seq.buildSteps());
    }

    public static Sequence.Builder builder() {
        return Sequence.builder();
    }

    @Override
    protected void onStart() {
        started = false;
        current = null;
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        if (!started) {
            started = true;
            current = steps.pollFirst();
            if (current != null)
                current.start(mod);
        }

        if (current == null)
            return null;

        if (current.tick(mod)) {
            current.stop(mod);
            current = steps.pollFirst();
            if (current != null)
                current.start(mod);
        }
        return null;
    }

    @Override
    public boolean isFinished() {
        return started && current == null && steps.isEmpty();
    }

    @Override
    protected boolean isEqual(Task other) {
        if(!(other instanceof PrimitiveSequenceTask)){
            return false;
        }
        PrimitiveSequenceTask RHS = (PrimitiveSequenceTask) other;
        // TODO: fix if needed
        return this.steps == RHS.steps && this.started == RHS.started;
    }

    @Override
    protected void onStop(Task next) {
        AltoClef mod = AltoClef.getInstance();
        // releases all inputs in case 
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
        return "PrimitiveSequenceTask";
    }
}
