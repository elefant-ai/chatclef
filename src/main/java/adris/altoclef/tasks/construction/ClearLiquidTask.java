package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;

/**
 * Removes a liquid source block at a position.
 */
public class ClearLiquidTask extends Task {

    private final BlockPos _liquidPos;

    public ClearLiquidTask(BlockPos liquidPos) {
        this._liquidPos = liquidPos;
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected Task onTick() {
        if (AltoClef.getInstance().getItemStorage().hasItem(Items.BUCKET)) {
            AltoClef.getInstance().getBehaviour().setRayTracingFluidHandling(RaycastContext.FluidHandling.SOURCE_ONLY);
            return new InteractWithBlockTask(new ItemTarget(Items.BUCKET, 1), _liquidPos, false);
        }

        return new PlaceStructureBlockTask(_liquidPos);
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    @Override
    public boolean isFinished() {
        if (AltoClef.getInstance().getChunkTracker().isChunkLoaded(_liquidPos)) {
            return AltoClef.getInstance().getWorld().getBlockState(_liquidPos).getFluidState().isEmpty();
        }
        return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof ClearLiquidTask task) {
            return task._liquidPos.equals(_liquidPos);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Clear liquid at " + _liquidPos;
    }
}
