package  adris.altoclef.player2api.LLM.State;

import adris.altoclef.AltoClef;
import net.minecraft.client.network.ClientPlayerEntity;

public class AgentState {
    private final float health;
    private final float food;
    private final float saturation;
    private final String inventory;
    private final String taskStatus;
    private final String oxygenLevel;
    private final String armor;
    private final String gamemode;

    public AgentState(float health, float food, float saturation,
                       String inventory, String taskStatus, String oxygenLevel,
                       String armor, String gamemode) {
        this.health = health;
        this.food = food;
        this.saturation = saturation;
        this.inventory = inventory;
        this.taskStatus = taskStatus;
        this.oxygenLevel = oxygenLevel;
        this.armor = armor;
        this.gamemode = gamemode;
    }

    public static AgentState fromMod(AltoClef mod) {
        ClientPlayerEntity player = mod.getPlayer();
        return new AgentState(
                player.getHealth(),
                player.getHungerManager().getFoodLevel(),
                player.getHungerManager().getSaturationLevel(),
                StateUtils.getInventoryString(mod),
                StateUtils.getTaskStatusString(mod),
                StateUtils.getOxygenString(mod),
                StateUtils.getEquippedArmorStatusString(mod),
                StateUtils.getGamemodeString(mod)
        );
    }

    @Override
    public String toString() {
        return String.format("""
            {
              health: %.2f,
              food: %.2f,
              saturation: %.2f,
              inventory: %s,
              taskStatus: "%s",
              oxygenLevel: "%s",
              armor: %s,
              gamemode: "%s"
            }
        """, health, food, saturation, inventory, taskStatus, oxygenLevel, armor, gamemode);
    }
}
