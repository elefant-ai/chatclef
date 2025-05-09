package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.slot.MoveItemToSlotFromInventoryTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerCache;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Moves items from your inventory to a storage container.
 */
public class StoreInContainerTask extends AbstractDoToStorageContainerTask {

    public static final Block[] CONTAINER_BLOCKS = Stream.concat(Arrays.stream(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL}), Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.SHULKER_BOXES))).toArray(Block[]::new);

    private final BlockPos targetContainer;
    private final boolean getIfNotPresent;
    private final ItemTarget[] toStore;

    private ContainerStoredTracker storedItems;

    public StoreInContainerTask(BlockPos targetContainer, boolean getIfNotPresent, ItemTarget... toStore) {
        this.targetContainer = targetContainer;
        this.getIfNotPresent = getIfNotPresent;
        this.toStore = toStore;
    }

    @Override
    protected Optional<BlockPos> getContainerTarget() {
        return Optional.of(targetContainer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (storedItems == null) {
            // Only consider transfers to the container we wish
            storedItems = new ContainerStoredTracker(slot -> {
                Optional<BlockPos> openContainer = AltoClef.getInstance().getItemStorage().getLastBlockPosInteraction();
                return openContainer.isPresent() && openContainer.get().equals(targetContainer);
            });
        }
        storedItems.startTracking();
    }

    @Override
    protected Task onTick() {
        // Get more if we don't have & "get if not present" is true.
        if (getIfNotPresent) {
            for (ItemTarget target : toStore) {
                int inventoryNeed = target.getTargetCount() - storedItems.getStoredCount(target.getMatches());
                if (inventoryNeed > AltoClef.getInstance().getItemStorage().getItemCount(target)) {
                    return TaskCatalogue.getItemTask(new ItemTarget(target, inventoryNeed));
                }
            }
        }
        return super.onTick();
    }

    @Override
    protected void onStop(Task interruptTask) {
        super.onStop(interruptTask);
        storedItems.stopTracking();
    }

    @Override
    protected Task onContainerOpenSubtask(AltoClef mod, ContainerCache containerCache) {
        // Move all items that aren't in the container
        for (ItemTarget target : storedItems.getUnstoredItemTargetsYouCanStore(mod, toStore)) {
            setDebugState("Dumping " + target);
            // Grab the item from the current chest that most closely matches our requirements
            List<Slot> potentials = mod.getItemStorage().getSlotsWithItemPlayerInventory(false, target.getMatches());

            // Pick the best slot to grab from.
            Optional<Slot> bestPotential = PickupFromContainerTask.getBestSlotToTransfer(
                    mod,
                    target,
                    mod.getItemStorage().getItemCountContainer(target.getMatches()),
                    potentials,
                    stack -> mod.getItemStorage().getSlotThatCanFitInOpenContainer(stack, false).isPresent());
            if (bestPotential.isPresent()) {
                ItemStack stackIn = StorageHelper.getItemStackInSlot(bestPotential.get());
                Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInOpenContainer(stackIn, false);
                if (toMoveTo.isEmpty()) {
                    setDebugState("CONTAINER FULL!");
                    return null;
                }
                setDebugState("Moving to slot...");
                return new MoveItemToSlotFromInventoryTask(target, toMoveTo.get());
            }
            setDebugState("SHOULD NOT HAPPEN! No valid items detected.");
        }
        setDebugState("SHOULD NOT HAPPEN! All items stored but we're still trying.");
        return null;
    }

    @Override
    public boolean isFinished() {
        // We've stored all items
        return storedItems != null && storedItems.getUnstoredItemTargetsYouCanStore(AltoClef.getInstance(), toStore).length == 0;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof StoreInContainerTask task) {
            return task.targetContainer.equals(targetContainer) && task.getIfNotPresent == getIfNotPresent && Arrays.equals(task.toStore, toStore);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Storing in container[" + targetContainer.toShortString() + "] " + Arrays.toString(toStore);
    }
}
