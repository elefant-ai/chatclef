package adris.altoclef.player2api.status;

import java.util.HashMap;
import java.util.Map;

import adris.altoclef.AltoClef;
import net.minecraft.client.network.ClientPlayerEntity;

import adris.altoclef.AltoClef;
import net.minecraft.client.network.ClientPlayerEntity;

public class AgentStatus extends ObjectStatus {
    public static AgentStatus fromMod(AltoClef mod) {
        ClientPlayerEntity player = mod.getPlayer();
        return (AgentStatus) new AgentStatus()
                .add("health", String.format("%.2f/20", player.getHealth()))
                .add("food", String.format("%.2f/20", (float) player.getHungerManager().getFoodLevel()))
                .add("saturation", String.format("%.2f/20", player.getHungerManager().getSaturationLevel()))
                .add("inventory", StatusUtils.getInventoryString(mod))
                .add("taskStatus", StatusUtils.getTaskStatusString(mod))
                .add("oxygenLevel", StatusUtils.getOxygenString(mod))
                .add("armor", StatusUtils.getEquippedArmorStatusString(mod))
                .add("gamemode", StatusUtils.getGamemodeString(mod));
    }
}
