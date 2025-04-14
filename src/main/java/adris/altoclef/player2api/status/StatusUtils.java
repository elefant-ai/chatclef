package adris.altoclef.player2api.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public class StatusUtils {

    public static String getInventoryString(AltoClef mod) {
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < mod.getPlayer().getInventory().size(); ++i) {
            ItemStack stack = mod.getPlayer().getInventory().getStack(i);
            if (!stack.isEmpty()) {
                String name = ItemHelper.stripItemName(stack.getItem());
                counts.put(name, counts.getOrDefault(name, 0) + stack.getCount());
            }
        }

        StringBuilder output = new StringBuilder("{\n");
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            output.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        output.append("}");
        return output.toString();
    }

    public static String getDimensionString(AltoClef mod) {
        DimensionType dim = mod.getPlayer().getWorld().getDimension();
        return "Dimension: " + dim.toString(); // Or use `dim.effects()` or `dim.getRegistryKey()` depending on desired output
    }

    public static String getWeatherString(AltoClef mod) {
        boolean isRaining = mod.getWorld().isRaining();
        boolean isThundering = mod.getWorld().isThundering();
        return String.format("Weather: { isRaining: %s, isThundering: %s }", isRaining, isThundering);
    }

    public static String getSpawnPosString(AltoClef mod) {
        BlockPos spawnPos = mod.getWorld().getSpawnPos();
        return String.format("Spawn Position: (%d, %d, %d)", spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
    }

    public static String getTaskStatusString(AltoClef mod) {
        List<Task> tasks = mod.getUserTaskChain().getTasks();
        if (tasks.isEmpty()) {
            return "Task Status: No tasks currently running.";
        } else {
            return "Task Status: " + tasks.get(0).toString();
        }
    }

    public static String getNearbyBlocksString(AltoClef mod) {
        final int radius = 12;
        BlockPos center = mod.getPlayer().getBlockPos();
        Map<String, Integer> blockCounts = new HashMap<>();
    
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    String blockName = mod.getWorld().getBlockState(pos).getBlock().getTranslationKey();
    
                    if (!blockName.equals("block.minecraft.air")) {
                        blockCounts.put(blockName, blockCounts.getOrDefault(blockName, 0) + 1);
                    }
                }
            }
        }
    
        StringBuilder result = new StringBuilder("Nearby Blocks:\n{\n");
        for (Map.Entry<String, Integer> entry : blockCounts.entrySet()) {
            result.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        result.append("}");
        return result.toString();
    }
}