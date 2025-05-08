package adris.altoclef.trackers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Streams;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class UserBlockRangeTracker extends Tracker {

    // TODO: Config

    final int AVOID_BREAKING_RANGE = 16;

    final Block[] USER_INDICATOR_BLOCKS = Streams.concat(
        Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.BED))
        // maybe add these in later, no need
        // Arrays.asList(Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.FLETCHING_TABLE, Blocks.ANVIL).stream()
    ).toArray(Block[]::new);

    final Block[] USER_BLOCKS_TO_AVOID_BREAKING = Streams.concat(
        Arrays.asList(Blocks.COBBLESTONE).stream(),
        Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.LOG))
    ).toArray(Block[]::new);

    private final Set<BlockPos> _dontBreakBlocks = new HashSet<>();

    public UserBlockRangeTracker(TrackerManager manager) {
        super(manager);
    }

    public boolean isNearUserTrackedBlock(BlockPos pos) {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return _dontBreakBlocks.contains(pos);
        }
    }

    @Override
    protected void updateState() {
        _dontBreakBlocks.clear();
        List<BlockPos> userBlocks = AltoClef.getInstance().getBlockScanner().getKnownLocationsIncludeUnreachable(USER_INDICATOR_BLOCKS);

        Set<Block> userIndicatorBlocks = new HashSet<>(Arrays.asList(USER_INDICATOR_BLOCKS));
        Set<Block> userBlocksToAvoidMining = new HashSet<>(Arrays.asList(USER_BLOCKS_TO_AVOID_BREAKING));

        // filter out user blocks
        // TODO: for some reason we haven't been validating in the world for block tracking... so we do it manually.
        //      would "fixing" it cause problems?
        userBlocks.removeIf(bpos -> {
            Block b = AltoClef.getInstance().getWorld().getBlockState(bpos).getBlock();
            return !userIndicatorBlocks.contains(b);
        });


        for (BlockPos userBlockPos : userBlocks) {

            BlockPos min = userBlockPos.add(-AVOID_BREAKING_RANGE, -AVOID_BREAKING_RANGE, -AVOID_BREAKING_RANGE);
            BlockPos max = userBlockPos.add(AVOID_BREAKING_RANGE, AVOID_BREAKING_RANGE, AVOID_BREAKING_RANGE);

            // Range
            for (BlockPos possible : adris.altoclef.util.helpers.WorldHelper.scanRegion(min, max)) {
                Block b = AltoClef.getInstance().getWorld().getBlockState(possible).getBlock();
                if (userBlocksToAvoidMining.contains(b)) {
                    _dontBreakBlocks.add(possible);
                }
            }
        }
    }

    @Override
    protected void reset() {
        _dontBreakBlocks.clear();
    }
    
}
