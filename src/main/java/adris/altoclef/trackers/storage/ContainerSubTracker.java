package adris.altoclef.trackers.storage;

import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockInteractEvent;
import adris.altoclef.eventbus.events.ScreenOpenEvent;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.trackers.Tracker;
import adris.altoclef.trackers.TrackerManager;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.function.Predicate;

/**
 * Keeps track of items in containers
 */
public class ContainerSubTracker extends Tracker {

    private final HashMap<Dimension, HashMap<BlockPos, ContainerCache>> containerCaches = new HashMap<>();
    private boolean containerOpen;
    private BlockPos lastBlockPosInteraction;
    private Block lastBlockInteraction;
    private ContainerCache enderChestCache;
    private boolean hasSentError;

    public ContainerSubTracker(TrackerManager manager) {
        super(manager);
        for (Dimension dimension : Dimension.values()) {
            containerCaches.put(dimension, new HashMap<>());
        }

        // Listen for when we interact with a block
        EventBus.subscribe(BlockInteractEvent.class, evt -> {
            BlockPos blockPos = evt.hitResult.getBlockPos();
            BlockState bs = mod.getWorld().getBlockState(blockPos);
            onBlockInteract(blockPos, bs.getBlock());
        });
        EventBus.subscribe(ScreenOpenEvent.class, evt -> {
            if (evt.preOpen) {
                onScreenOpenFirstTick(evt.screen);
            } else {
                if (evt.screen == null)
                    onScreenClose();
            }
        });
    }

    private void onBlockInteract(BlockPos pos, Block block) {
        if (block instanceof AbstractFurnaceBlock ||
                block instanceof ChestBlock ||
                block.equals(Blocks.ENDER_CHEST) ||
                block instanceof HopperBlock ||
                block instanceof ShulkerBoxBlock ||
                block instanceof DispenserBlock ||
                block instanceof BarrelBlock) {
            lastBlockPosInteraction = pos;
            lastBlockInteraction = block;
        }
    }

    private void onScreenOpenFirstTick(final Screen screen) {
        containerOpen = screen instanceof FurnaceScreen
                || screen instanceof GenericContainerScreen
                || screen instanceof SmokerScreen
                || screen instanceof BlastFurnaceScreen
                || screen instanceof HopperScreen
                || screen instanceof ShulkerBoxScreen;
    }

    private void onScreenClose() {
        containerOpen = false;
        lastBlockPosInteraction = null;
        lastBlockInteraction = null;
        hasSentError = false;
    }

    public void onServerTick() {
        if (MinecraftClient.getInstance().player == null)
            return;
        // If we haven't registered interacting with a block, try the currently "looking at" block
        if (containerOpen && lastBlockPosInteraction == null && lastBlockInteraction == null) {
            if (MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult bhit) {
                Debug.logWarning("Screen open but no block interaction detected, using the block we're currently looking at.");
                lastBlockPosInteraction = bhit.getBlockPos();
                lastBlockInteraction = mod.getWorld().getBlockState(lastBlockPosInteraction).getBlock();
            }
        }
        if (containerOpen && lastBlockPosInteraction != null && lastBlockInteraction != null) {
            BlockPos containerPos = lastBlockPosInteraction;
            ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;
            if (handler == null)
                return;

            HashMap<BlockPos, ContainerCache> dimCache = containerCaches.get(WorldHelper.getCurrentDimension());

            // Container Type Mismatch, reset.
            if (dimCache.containsKey(containerPos)) {
                ContainerType currentType = dimCache.get(containerPos).getContainerType();
                if (!ContainerType.screenHandlerMatches(currentType, handler)) {
                    if (!hasSentError) {
                        Debug.logMessage("Mismatched container screen at " + containerPos.toShortString() + ", will overwrite container data: " + handler.getType() + " ?=> " + currentType);
                        hasSentError = true;
                    }
                    dimCache.remove(containerPos);
                }
            }

            // New container found
            if (!dimCache.containsKey(containerPos)) {
                Block containerBlock = lastBlockInteraction;
                ContainerType interactType = ContainerType.getFromBlock(containerBlock);
                ContainerCache newCache = new ContainerCache(WorldHelper.getCurrentDimension(), containerPos, interactType);
                dimCache.put(containerPos, newCache);
                // Special ender chest cache
                if (interactType == ContainerType.ENDER_CHEST) {
                    enderChestCache = newCache;
                }
            }

            ContainerCache toUpdate = dimCache.get(containerPos);
            toUpdate.update(handler, stack -> {

            });
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isContainerCacheValid(Dimension dimension, ContainerCache cache) {
        BlockPos pos = cache.getBlockPos();
        if (WorldHelper.getCurrentDimension() == dimension && mod.getChunkTracker().isChunkLoaded(pos)) {
            ContainerType actualType = ContainerType.getFromBlock(mod.getWorld().getBlockState(pos).getBlock());
            if (actualType == ContainerType.EMPTY) {
                return false;
            }
            return actualType == cache.getContainerType();
        }
        return true;
    }

    public Optional<ContainerCache> getContainerAtPosition(Dimension dimension, BlockPos pos) {
        Optional<ContainerCache> cache = Optional.ofNullable(containerCaches.get(dimension).getOrDefault(pos, null));
        if (cache.isPresent() && !isContainerCacheValid(dimension, cache.get())) {
            containerCaches.get(dimension).remove(pos);
            return Optional.empty();
        }
        return cache;
    }

    public Optional<ContainerCache> getContainerAtPosition(BlockPos pos) {
        return getContainerAtPosition(WorldHelper.getCurrentDimension(), pos);
    }

    public Optional<ContainerCache> getEnderChestStorage() {
        return Optional.ofNullable(enderChestCache);
    }

    public List<ContainerCache> getCachedContainers(Predicate<ContainerCache> accept) {
        List<ContainerCache> result = new ArrayList<>();
        List<Pair<Dimension, BlockPos>> toRemove = new ArrayList<>();
        for (Dimension dim : containerCaches.keySet()) {
            HashMap<BlockPos, ContainerCache> map = containerCaches.get(dim);
            for (ContainerCache cache : map.values()) {
                if (!isContainerCacheValid(dim, cache)) {
                    toRemove.add(new Pair<>(dim, cache.getBlockPos()));
                    continue;
                }
                if (accept.test(cache))
                    result.add(cache);
            }
        }
        for (Pair<Dimension, BlockPos> remove : toRemove) {
            containerCaches.get(remove.getLeft()).remove(remove.getRight());
        }
        return result;
    }

    public List<ContainerCache> getCachedContainers(ContainerType... types) {
        Set<ContainerType> typeSet = new HashSet<>(Arrays.asList(types));
        return getCachedContainers(cache -> typeSet.contains(cache.getContainerType()));
    }

    public Optional<ContainerCache> getClosestTo(Vec3d pos, Predicate<ContainerCache> accept) {
        double bestDist = Double.POSITIVE_INFINITY;
        Dimension dim = WorldHelper.getCurrentDimension();

        List<BlockPos> toRemove = new ArrayList<>();

        ContainerCache bestCache = null;
        for (ContainerCache cache : containerCaches.get(dim).values()) {
            if (!isContainerCacheValid(dim, cache)) {
                toRemove.add(cache.getBlockPos());
                continue;
            }
            double dist = BlockPosVer.getSquaredDistance(cache.getBlockPos(),pos);
            if (dist < bestDist) {
                if (accept.test(cache)) {
                    bestDist = dist;
                    bestCache = cache;
                }
            }
        }
        // Clear anything invalid
        for (BlockPos remove : toRemove) {
            containerCaches.get(dim).remove(remove);
        }
        return Optional.ofNullable(bestCache);
    }

    public Optional<ContainerCache> getClosestTo(Vec3d pos, ContainerType... types) {
        Set<ContainerType> typeSet = new HashSet<>(Arrays.asList(types));
        return getClosestTo(pos, cache -> typeSet.contains(cache.getContainerType()));
    }

    public List<ContainerCache> getContainersWithItem(Item... items) {
        return getCachedContainers(cache -> cache.hasItem(items));
    }

    public Optional<ContainerCache> getClosestWithItem(Vec3d pos, Item... items) {
        return getClosestTo(pos, cache -> cache.hasItem(items));
    }

    public boolean hasItem(Predicate<ContainerCache> accept, Item... items) {
        for (HashMap<BlockPos, ContainerCache> map : containerCaches.values()) {
            for (ContainerCache cache : map.values()) {
                if (cache.hasItem(items) && accept.test(cache))
                    return true;
            }
        }
        return false;
    }

    public boolean hasItem(Item... items) {
        return hasItem(cache -> true, items);
    }

    public BlockPos getLastBlockPosInteraction() {
        return lastBlockPosInteraction;
    }

    @Override
    protected void updateState() {
        // umm lol
    }

    @Override
    protected void reset() {
        for (Dimension key : containerCaches.keySet()) {
            containerCaches.get(key).clear();
        }
    }

}
