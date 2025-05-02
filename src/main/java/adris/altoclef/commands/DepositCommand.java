package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.*;
import adris.altoclef.tasks.container.StoreInAnyContainerTask;
import adris.altoclef.tasks.container.StoreInContainerTask;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

public class DepositCommand extends Command {

    // TODO: Configuration
    private static final int NEARBY_RANGE = 20;

    private static final Block[] VALID_CONTAINERS = Stream.concat(Arrays.stream(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL}), Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.SHULKER_BOXES))).toArray(Block[]::new);

    public DepositCommand() throws CommandException {
        super("deposit", "Deposit our items to a nearby chest, making a chest if one doesn't exist. Pass no arguments to depisot ALL items. Examples: `deposit` deposits ALL items, `deposit diamond 2` deposits 2 diamonds.", new Arg(ItemList.class, "items (empty for ALL non gear items)", null, 0, false));
    }

    public static ItemTarget[] getAllNonEquippedOrToolItemsAsTarget(AltoClef mod) {
        return StorageHelper.getAllInventoryItemsAsTargets(slot -> {
            // Ignore armor
            if (ArrayUtils.contains(PlayerSlot.ARMOR_SLOTS, slot))
                return false;
            ItemStack stack = StorageHelper.getItemStackInSlot(slot);
            // Ignore tools
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                return !(item instanceof ToolItem);
            }
            return false;
        });
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        ItemList itemList = parser.get(ItemList.class);
        ItemTarget[] items;

        if (itemList != null) {
            // Validation that we have the items:
            Map<String, Integer> countsLeftover = new HashMap<>();

            // populate starting requirements
            for (ItemTarget itemTarget : itemList.items) {
                String name = itemTarget.getCatalogueName();
                countsLeftover.put(name, countsLeftover.getOrDefault(name, 0) + itemTarget.getTargetCount());
            }

            // subtract counts
            for (int i = 0; i < mod.getPlayer().getInventory().size(); ++i) {
                ItemStack stack = mod.getPlayer().getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    String name = ItemHelper.stripItemName(stack.getItem());
                    int count = stack.getCount();
                    if (countsLeftover.containsKey(name)) {
                        countsLeftover.put(name, countsLeftover.get(name) - count);
                        if (countsLeftover.get(name) <= 0) {
                            countsLeftover.remove(name);
                        }
                    }
                }
            }

            // invalid!
            if (countsLeftover.size() != 0) {
                String leftover = String.join(",", countsLeftover.entrySet().stream().map(e -> e.getKey() + " x " + e.getValue().toString()).toList());
                mod.log("Insuffucient items in inventory to deposit. We still need: " + leftover + ".");
                finish();
                return;
            }
        }

        if (itemList == null) {
            items = getAllNonEquippedOrToolItemsAsTarget(mod);
        } else {
            items = itemList.items;
        }

        // BlockScanner blockScanner = mod.getBlockScanner();
        // Optional<BlockPos> container = blockScanner.getNearestBlock(VALID_CONTAINERS);

        // if (!container.isPresent() || !container.get().isWithinDistance(mod.getPlayer().getPos(), NEARBY_RANGE)) {
        //     // Just use a random container
        //     mod.runUserTask(new StoreInAnyContainerTask(false, items), this::finish);
        //     return;
        //     // mod.log("No container (chest, barrel, shulker) found nearby. Move close to a container and try again.");
        //     // finish();
        //     // return;
        // }

        // // Store in the nearby container
        // mod.runUserTask(new StoreInContainerTask(container.get(), false, items), this::finish);

        mod.runUserTask(new StoreInAnyContainerTask(false, items), this::finish);
    }
}
