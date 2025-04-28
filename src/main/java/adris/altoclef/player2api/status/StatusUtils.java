package adris.altoclef.player2api.status;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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

        ObjectStatus status = new ObjectStatus();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            status.add(entry.getKey(), entry.getValue().toString());
        }

        return status.toString();
    }

    public static String getDimensionString(AltoClef mod) {
        return mod.getWorld().getRegistryKey().getValue().toString().replace("minecraft:", "");
    }

    public static String getWeatherString(AltoClef mod) {
        boolean isRaining = mod.getWorld().isRaining();
        boolean isThundering = mod.getWorld().isThundering();

        ObjectStatus status = new ObjectStatus()
                .add("isRaining", String.valueOf(isRaining))
                .add("isThundering", String.valueOf(isThundering));

        return status.toString();
    }

    public static String getSpawnPosString(AltoClef mod) {
        BlockPos spawnPos = mod.getWorld().getSpawnPos();
        return String.format("(%d, %d, %d)", spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
    }

    public static String getTaskStatusString(AltoClef mod) {
        List<Task> tasks = mod.getUserTaskChain().getTasks();
        if (tasks.isEmpty()) {
            return "No tasks currently running.";
        } else {
            return tasks.get(0).toString();
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
                    String blockName = mod.getWorld().getBlockState(pos).getBlock().getTranslationKey()
                            .replace("block.minecraft.", "");
                    if (!blockName.equals("air")) {
                        blockCounts.put(blockName, blockCounts.getOrDefault(blockName, 0) + 1);
                    }
                }
            }
        }

        ObjectStatus status = new ObjectStatus();
        for (Map.Entry<String, Integer> entry : blockCounts.entrySet()) {
            status.add(entry.getKey(), entry.getValue().toString());
        }

        return status.toString();
    }

    public static String getOxygenString(AltoClef mod) {
        return String.format("%s/300", Integer.toString(mod.getPlayer().getAir()));
    }

    public static String getNearbyHostileMobs(AltoClef mod) {
        final int radius = 32;
        List<String> descriptions = new ArrayList<>();

        for (Entity entity : mod.getEntityTracker().getCloseEntities()) {
            if (entity instanceof HostileEntity && entity.distanceTo(mod.getPlayer()) < radius) {
                String type = entity.getType().getTranslationKey();
                String niceName = type.replace("entity.minecraft.", "");
                String position = entity.getPos().floorAlongAxes(EnumSet.allOf(Direction.Axis.class)).toString();
                descriptions.add(niceName + " at " + position);
            }
        }

        if (descriptions.isEmpty()) {
            return String.format("no nearby hostile mobs within %d", radius);
        } else {
            return "[" + String.join(",", descriptions.stream()
                    .map(s -> "\"" + s + "\"")
                    .toArray(String[]::new)) + "]";
        }
    }

    public static String getEquippedArmorStatusString(AltoClef mod) {
        ClientPlayerEntity player = mod.getPlayer();
        ObjectStatus status = new ObjectStatus();

        ItemStack head = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack legs = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack feet = player.getEquippedStack(EquipmentSlot.FEET);
        ItemStack offhand = player.getEquippedStack(EquipmentSlot.OFFHAND);

        status.add("helmet", (head.isEmpty() || !(head.getItem() instanceof ArmorItem)) ? "none"
                : head.getItem().getTranslationKey().replace("item.minecraft.", ""));
        status.add("chestplate", (chest.isEmpty() || !(chest.getItem() instanceof ArmorItem)) ? "none"
                : chest.getItem().getTranslationKey().replace("item.minecraft.", ""));
        status.add("leggings", (legs.isEmpty() || !(legs.getItem() instanceof ArmorItem)) ? "none"
                : legs.getItem().getTranslationKey().replace("item.minecraft.", ""));
        status.add("boots", (feet.isEmpty() || !(feet.getItem() instanceof ArmorItem)) ? "none"
                : feet.getItem().getTranslationKey().replace("item.minecraft.", ""));
        status.add("offhand_shield", (offhand.isEmpty() || !(offhand.getItem() instanceof ShieldItem)) ? "none"
                : offhand.getItem().getTranslationKey().replace("item.minecraft.", ""));

        return status.toString();
    }

    public static String getNearbyPlayers(AltoClef mod) {
        final int radius = 32;
        List<String> descriptions = new ArrayList<>();

        for (Entity entity : mod.getEntityTracker().getCloseEntities()) {
            if (entity instanceof PlayerEntity player && entity.distanceTo(mod.getPlayer()) < radius) {
                String username = player.getName().getString();
                if (username != mod.getPlayer().getName().getString()) {
                    String position = entity.getPos().floorAlongAxes(EnumSet.allOf(Direction.Axis.class)).toString();
                    descriptions.add(username + " at " + position);
                }
            }
        }

        if (descriptions.isEmpty()) {
            return String.format("no nearby users within %d", radius);
        } else {
            return "[" + String.join(",", descriptions.stream()
                    .map(s -> "\"" + s + "\"")
                    .toArray(String[]::new)) + "]";
        }
    }

    public static float getUserNameDistance(AltoClef mod, String targetUsername) {
        for (PlayerEntity player : mod.getWorld().getPlayers()) {
            String username = player.getName().getString();
            if (username.equals(targetUsername)) {
                return player.distanceTo(mod.getPlayer());
            }
        }
        return Float.MAX_VALUE;
    }

    public static String getDifficulty(AltoClef mod) {
        return mod.getWorld().getDifficulty().toString();
    }

    public static String getTimeString(AltoClef mod) {
        ObjectStatus status = new ObjectStatus();

        status.add("isDay", Boolean.toString(mod.getWorld().isDay()));
        status.add("timeOfDay", String.format("%d/24,000", mod.getWorld().getTimeOfDay() % 24000));

        return status.toString();

    }

    public static String getGamemodeString(AltoClef mod) {
        return mod.getPlayer().isCreative() ? "creative" : "survival";
    }
}