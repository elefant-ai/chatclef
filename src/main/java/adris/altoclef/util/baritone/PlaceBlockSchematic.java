package adris.altoclef.util.baritone;

import baritone.api.schematic.AbstractSchematic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.List;

//@Deprecated
public class PlaceBlockSchematic extends AbstractSchematic {

    private static final int RANGE = 1;
    private final Block[] blockToPlace;
    private final boolean skipIfAlreadyThere;
    private final boolean done;
    private BlockState targetPlace;

    public PlaceBlockSchematic(Block[] blocksToPlace, boolean skipIfAlreadyThere) {
        super(RANGE, RANGE, RANGE);
        blockToPlace = blocksToPlace;
        done = false;
        targetPlace = null;
        this.skipIfAlreadyThere = skipIfAlreadyThere;
    }

    public PlaceBlockSchematic(Block[] blocksToPlace) {
        this(blocksToPlace, true);
    }

    public PlaceBlockSchematic(Block blockToPlace) {
        this(new Block[]{blockToPlace});
    }


    public boolean foundSpot() {
        return targetPlace != null;
    }

    // No restrictions.
    //@Override
    //public boolean inSchematic(int x, int y, int z, BlockState currentState) {
    //    return true;
    //}

    @Override
    public BlockState desiredState(int x, int y, int z, BlockState blockState, List<BlockState> list) {
        // Only place at the origin.
        if (x != 0 || y != 0 || z != 0) {
            return blockState;
        }
        // If a block already exists there, place it.
        if (skipIfAlreadyThere && blockIsTarget(blockState.getBlock())) {
            //System.out.println("PlaceBlockNearbySchematic (already exists)");
            targetPlace = blockState;
        }
        boolean isDone = (targetPlace != null);
        if (isDone) {
            return targetPlace;
        }
        //System.out.print("oof: [");
        if (!list.isEmpty()) {
            for (BlockState possible : list) {
                if (possible == null) {
                /*
                if (ToolSet.areShearsEffective(blockState.getBlock()) || BlockTags.FLOWERS.contains(blockState.getBlock())) {
                    // Sheering items/flowers results in this issue, but it works fine!
                } else {
                    Debug.logWarning("Weird issue, given possible state is null. Will ignore.");
                }
                 */
                    continue;
                }
                //System.out.print(possible.getBlock().getTranslationKey() + " ");
                if (blockIsTarget(possible.getBlock())) {
                    //System.out.print("PlaceBlockNearbySchematic  ( FOUND! )");
                    targetPlace = possible;
                    return possible;
                }
            }
        }
        //System.out.println("] ( :(((((( )");
        return blockState;
    }


    private boolean blockIsTarget(Block block) {
        if (blockToPlace != null) {
            for (Block check : blockToPlace) {
                if (check == block) return true;
            }
        }
        return false;
    }
}
