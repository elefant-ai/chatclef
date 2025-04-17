package adris.altoclef.player2api.status;

import adris.altoclef.AltoClef;

public class WorldStatus extends ObjectStatus {

    public static WorldStatus fromMod(AltoClef mod) {
        return (WorldStatus) new WorldStatus()
                .add("weather", StatusUtils.getWeatherString(mod))
                .add("dimension", StatusUtils.getDimensionString(mod))
                .add("spawnPos", StatusUtils.getSpawnPosString(mod))
                .add("nearbyBlocks", StatusUtils.getNearbyBlocksString(mod))
                .add("nearbyHostiles", StatusUtils.getNearbyHostileMobs(mod))
                .add("nearbyPlayers", StatusUtils.getNearbyPlayers(mod))
                .add("difficulty", StatusUtils.getDifficulty(mod))
                .add("timeInfo", StatusUtils.getTimeString(mod));
    }
}
