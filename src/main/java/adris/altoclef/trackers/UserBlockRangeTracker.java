package adris.altoclef.trackers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class UserBlockRangeTracker extends Tracker {

    // TODO: Config
    final int AVOID_BREAKING_VOXEL_SIZE = 8;
    final Block[] USER_BLOCKS = ItemHelper.itemsToBlocks(ItemHelper.BED);

    private final Set<Vec3i> _dontBreakVoxels = new HashSet<>();

    public UserBlockRangeTracker(TrackerManager manager) {
        super(manager);
    }

    private Vec3i blockPosToVoxel(BlockPos pos) {
        return new Vec3i(pos.getX() / AVOID_BREAKING_VOXEL_SIZE, pos.getY() / AVOID_BREAKING_VOXEL_SIZE, pos.getZ() / AVOID_BREAKING_VOXEL_SIZE);
    }

    public boolean isNearUserTrackedBlock(BlockPos pos) {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return _dontBreakVoxels.contains(blockPosToVoxel(pos));
        }
    }

    @Override
    protected void updateState() {
        _dontBreakVoxels.clear();
        List<BlockPos> userBlocks = AltoClef.getInstance().getBlockScanner().getKnownLocationsIncludeUnreachable(USER_BLOCKS);
        // filter out user blocks
        // TODO: is this a bad idea for the tracker...
        userBlocks.removeIf(bpos -> {
            Block b = AltoClef.getInstance().getWorld().getBlockState(bpos).getBlock();
            for (Block check : USER_BLOCKS) {
                if (check == b) {
                    return false;
                }
            }
            return true;
        });

        for (BlockPos userBlockPos : userBlocks) {
            Vec3i v = blockPosToVoxel(userBlockPos);
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dz = -1; dz <= 1; ++dz) {
                    for (int dy = -1; dy <= 1; ++dy) {
                        Vec3i vToAvoid = new Vec3i(v.getX() + dx, v.getY() + dy, v.getZ() + dz);
                        _dontBreakVoxels.add(vToAvoid);
                    }
                }
            }
        }
    }

    @Override
    protected void reset() {
        _dontBreakVoxels.clear();
    }
    
}
