package adris.altoclef.player2api.LLM.State;

import java.util.List;

import adris.altoclef.AltoClef;
import adris.altoclef.player2api.LLM.Event.Event;

public class WorldState extends AtomicState<WorldState> {
    private final String weather;
    private final String dimension;
    private final String spawnPos;
    private final String nearbyBlocks;
    private final String nearbyHostiles;
    private final String nearbyPlayers;
    private final String difficulty;
    private final String timeInfo;

    public WorldState(String weather, String dimension, String spawnPos,
                       String nearbyBlocks, String nearbyHostiles, String nearbyPlayers,
                       String difficulty, String timeInfo) {
        this.weather = weather;
        this.dimension = dimension;
        this.spawnPos = spawnPos;
        this.nearbyBlocks = nearbyBlocks;
        this.nearbyHostiles = nearbyHostiles;
        this.nearbyPlayers = nearbyPlayers;
        this.difficulty = difficulty;
        this.timeInfo = timeInfo;
    }

    public static WorldState fromMod(AltoClef mod) {
        return new WorldState(
                StateUtils.getWeatherString(mod),
                StateUtils.getDimensionString(mod),
                StateUtils.getSpawnPosString(mod),
                StateUtils.getNearbyBlocksString(mod),
                StateUtils.getNearbyHostileMobs(mod),
                StateUtils.getNearbyPlayers(mod),
                StateUtils.getDifficulty(mod),
                StateUtils.getTimeString(mod)
        );
    }

    @Override
    public String getSummary() {
        return String.format("""
            {
              weather: %s,
              dimension: "%s",
              spawnPos: "%s",
              nearbyBlocks: %s,
              nearbyHostiles: %s,
              nearbyPlayers: %s,
              difficulty: "%s",
              timeInfo: %s
            }
        """, weather, dimension, spawnPos, nearbyBlocks, nearbyHostiles, nearbyPlayers, difficulty, timeInfo);
    }
    @Override
    public List<Event> onChange(WorldState oldState, WorldState newState) {
        return List.of();
    }
}
