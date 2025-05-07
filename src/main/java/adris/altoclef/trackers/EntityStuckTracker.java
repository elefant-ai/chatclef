package adris.altoclef.trackers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;

public class EntityStuckTracker extends Tracker {

    final float MOB_RANGE = 25;

    private final Set<BlockPos> _blockedSpots = new HashSet<>();

    public EntityStuckTracker(TrackerManager manager) {
        super(manager);
    }


    public boolean isBlockedByEntity(BlockPos pos) {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return _blockedSpots.contains(pos);
        }
    }

    @Override
    protected synchronized void updateState() {
        // Go through every entity within a certain radius of the player
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {

            _blockedSpots.clear();

            Entity player = MinecraftClient.getInstance().player;
            // Loop through all entities and track 'em
            for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {

                if (entity == null || !entity.isAlive()) continue;

                // Don't be blocked by ourselves.
                if (entity.equals(player)) continue;

                if (!player.isInRange(entity, MOB_RANGE)) continue;
                Box b = entity.getBoundingBox();
                for (BlockPos p : WorldHelper.getBlocksTouchingBox(b)) {
                    _blockedSpots.add(p);
                }
            }
        }
    }

    @Override
    protected void reset() {
        _blockedSpots.clear();
    }
}
