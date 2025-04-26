package adris.altoclef.player2api.status;

import adris.altoclef.AltoClef;

public class WorldStatus extends ObjectStatus {

    public static WorldStatus fromMod(AltoClef mod) {
        return (WorldStatus) new WorldStatus()
                .add("weather", StatusUtils.getWeatherString(mod))
                .add("dimension", StatusUtils.getDimensionString(mod))
                .add("spawn position", StatusUtils.getSpawnPosString(mod))
                .add("nearby blocks", StatusUtils.getNearbyBlocksString(mod))
                .add("nearby hostiles", StatusUtils.getNearbyHostileMobs(mod))
                .add("nearby other players", StatusUtils.getNearbyPlayers(mod))
                .add("difficulty", StatusUtils.getDifficulty(mod))
                .add("timeInfo", StatusUtils.getTimeString(mod));
    }
}
