package adris.altoclef.player2api.status;

import adris.altoclef.AltoClef;
import net.minecraft.client.network.ClientPlayerEntity;

public record AgentStatus(float health, float food, float saturation, String inventoryStatusString,
        String taskStatusString) {
    @Override
    public String toString() {
        return String.format(
                "{\nhealth : %.2f/20,\nfood : %.2f/20,\nsaturation : %.2f/20,\ninventory : %s,\ntaskStatus : %s,\n}",
                health, food, saturation, inventoryStatusString, taskStatusString);


    public static AgentStatus fromMod(AltoClef mod) {
        ClientPlayerEntity player = mod.getPlayer();
        float food = player.getHungerManager().getFoodLevel();
        float saturation = player.getHungerManager().getSaturationLevel();
        float health = player.getHealth();
        String inventoryStatusString = StatusUtils.getInventoryString(mod);
        String taskStatusString = StatusUtils.getTaskStatus(mod);

        return new AgentStatus(health, food, saturation, inventoryStatusString, taskStatusString);
    }
}
