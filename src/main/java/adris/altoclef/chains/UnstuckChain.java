package adris.altoclef.chains;

import java.util.LinkedList;
import java.util.Optional;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.multiversion.entity.PlayerVer;
import adris.altoclef.multiversion.versionedfields.Blocks;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.GetOutOfWaterTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.SafeRandomShimmyTask;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StlHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.Baritone;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class UnstuckChain extends SingleTaskChain {

    private final LinkedList<Vec3d> posHistory = new LinkedList<>();
    private boolean isProbablyStuck = false;

    private int eatingTicks = 0;
    private boolean interruptedEating = false;

    private TimerGame shimmyTaskTimer = new TimerGame(5);
    private boolean startedShimmying = false;

    private TimerGame placeBlockTimeout = new TimerGame(2);

    private TimerGame placeBlockGoToBlockTimeout = new TimerGame(5);
    private BlockPos placeBlockGoToBlock = null;

    public UnstuckChain(TaskRunner runner) {
        super(runner);
    }


    private void checkStuckInWater() {
        if (posHistory.size() < 100) return;

        ClientWorld world = AltoClef.getInstance().getWorld();
        ClientPlayerEntity player = AltoClef.getInstance().getPlayer();

        // is not in water
        if (!world.getBlockState(player.getSteppingPos()).getBlock().equals(Blocks.WATER)
                && !world.getBlockState(player.getSteppingPos().down()).getBlock().equals(Blocks.WATER))
            return;

        // everything should be fine
        if (player.isOnGround()) {
            posHistory.clear();
            return;
        }

        // do NOT do anything if underwater
        if (player.getAir() < player.getMaxAir()) {
            return;
        }

        Vec3d pos1 = posHistory.get(0);
        for (int i = 1; i < 100; i++) {
            Vec3d pos2 = posHistory.get(i);
            if (Math.abs(pos1.getX() - pos2.getX()) > 0.75 || Math.abs(pos1.getZ() - pos2.getZ()) > 0.75) {
                return;
            }
        }

        posHistory.clear();
        setTask(new GetOutOfWaterTask());
    }

    private void checkStuckInPowderedSnow() {
        AltoClef mod = AltoClef.getInstance();

        PlayerEntity player = mod.getPlayer();
        ClientWorld world = mod.getWorld();

        if (PlayerVer.inPowderedSnow(player)) {
            isProbablyStuck = true;
            BlockPos destroyPos = null;

            Optional<BlockPos> nearest = mod.getBlockScanner().getNearestBlock(Blocks.POWDER_SNOW);
            if (nearest.isPresent()) {
                destroyPos = nearest.get();
            }

            BlockPos headPos = WorldHelper.toBlockPos(player.getEyePos()).down();
            if (world.getBlockState(headPos).getBlock() == Blocks.POWDER_SNOW) {
                destroyPos = headPos;
            } else if (world.getBlockState(player.getBlockPos()).getBlock() == Blocks.POWDER_SNOW) {
                destroyPos = player.getBlockPos();
            }

            if (destroyPos != null) {
                setTask(new DestroyBlockTask(destroyPos));
            }
        }
    }

    private void checkStuckOnEndPortalFrame(AltoClef mod) {
        BlockState state = mod.getWorld().getBlockState(mod.getPlayer().getSteppingPos());

        // if we are standing on an end portal frame that is NOT filled, get off otherwise we will get stuck
        if (state.getBlock() == Blocks.END_PORTAL_FRAME && !state.get(EndPortalFrameBlock.EYE)) {
            if (!mod.getFoodChain().isTryingToEat()) {
                isProbablyStuck = true;

                // for now let's just hope the other mechanisms will take care of cases where moving forward will get us in danger
                mod.getInputControls().tryPress(Input.MOVE_FORWARD);
            }
        }
    }

    private void checkEatingGlitch() {
        FoodChain foodChain = AltoClef.getInstance().getFoodChain();

        if (interruptedEating) {
            foodChain.shouldStop(false);
            interruptedEating = false;
        }

        if (foodChain.isTryingToEat()) {
            eatingTicks++;
        } else {
            eatingTicks = 0;
        }

        if (eatingTicks > 7*20) {
            Debug.logMessage("the bot is probably stuck trying to eat... resetting action");
            foodChain.shouldStop(true);

            eatingTicks = 0;
            interruptedEating = true;
            isProbablyStuck = true;
        }
    }

    // Baritone might try to place a block THROUGH an entity. Detect this here
    private void checkEntityBlocksPlacement() {

        Baritone b = AltoClef.getInstance().getClientBaritone();

        // must be pathing and trying to right click
        if (!b.getPathingBehavior().isPathing()) {
            placeBlockTimeout.reset();
            return;
        }
        if (!b.getInputOverrideHandler().isInputForcedDown(Input.CLICK_RIGHT)) {
            placeBlockTimeout.reset();
            return;
        }
        // must be LOOKING at an entity

        HitResult result = MinecraftClient.getInstance().crosshairTarget;

        if (result == null || result.getType() != HitResult.Type.ENTITY) {
            placeBlockTimeout.reset();
            return;            
        }
        if (result instanceof EntityHitResult) {
            if (placeBlockTimeout.elapsed()) {
                if (b.getPathingBehavior().getCurrent() != null) {
                    // We tried placing too long through this entity, get out of here!
                    var toPlace = b.getPathingBehavior().getCurrent().toPlace();
                    if (toPlace.size() != 0) {
                        Vec3d camPos = LookHelper.getCameraPos(AltoClef.getInstance());
                        Optional<BlockPos> closestPlace = toPlace.stream().min(StlHelper.compareValues(bpos -> camPos.distanceTo(bpos.toCenterPos())));
                        if (closestPlace.isPresent()) {
                            BlockPos f = closestPlace.get();
                            placeBlockGoToBlock = f;
                            placeBlockGoToBlockTimeout.reset();    
                        }
                    }
                }
                placeBlockTimeout.reset();
            }
            return;
        }

        placeBlockTimeout.reset();
    }

    @Override
    public float getPriority() {
        if (mainTask instanceof GetOutOfWaterTask && mainTask.isActive()) {
            return 55;
        }

        isProbablyStuck = false;

        AltoClef mod = AltoClef.getInstance();

        if (!AltoClef.inGame() || MinecraftClient.getInstance().isPaused() || !mod.getUserTaskChain().isActive())
            return Float.NEGATIVE_INFINITY;

        if (StorageHelper.isBlastFurnaceOpen() || StorageHelper.isSmokerOpen() || StorageHelper.isChestOpen() || StorageHelper.isBigCraftingOpen()) {
            return Float.NEGATIVE_INFINITY;
        }

        PlayerEntity player = mod.getPlayer();
        posHistory.addFirst(player.getPos());
        if (posHistory.size() > 500) {
            posHistory.removeLast();
        }

        checkStuckInWater();
        checkStuckInPowderedSnow();
        checkEatingGlitch();
        checkStuckOnEndPortalFrame(mod);
        checkEntityBlocksPlacement();

        if (isProbablyStuck) {
            return 55;
        }

        // Shimmy out of the way
        if (startedShimmying && !shimmyTaskTimer.elapsed()) {
            setTask(new SafeRandomShimmyTask());
            return 55;
        }
        startedShimmying = false;

        // We might place blocks but an entity blocks us
        if (placeBlockGoToBlockTimeout.elapsed()) {
            placeBlockGoToBlock = null;
        }
        if (placeBlockGoToBlock != null) {
            setTask(new GetToBlockTask(placeBlockGoToBlock, false));
            return 55;
        }

        return Float.NEGATIVE_INFINITY;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {

    }

    @Override
    public String getName() {
        return "Unstuck Chain";
    }
}
