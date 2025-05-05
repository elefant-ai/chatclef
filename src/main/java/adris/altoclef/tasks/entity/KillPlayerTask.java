package adris.altoclef.tasks.entity;

import java.util.Objects;
import java.util.Optional;

import adris.altoclef.AltoClef;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class KillPlayerTask extends AbstractKillEntityTask {

    private String playerName;

    public KillPlayerTask(String playerName) {
        this.playerName = playerName;
    }

    public KillPlayerTask(String playerName, double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
        this.playerName = playerName;
    }

    @Override
    protected Optional<Entity> getEntityTarget(AltoClef mod) {

        for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
            if (!entity.isAlive()) continue;
            if (entity instanceof PlayerEntity) {
                String playerName = entity.getName().getString().toLowerCase();
                // System.out.println("GOT PLAYER ENTITY: " + playerName);
                if (playerName != null && playerName.equals(this.playerName.toLowerCase())) {
                    // System.out.println("YAH: " + playerName + " == " + this.playerName.toLowerCase());
                    return Optional.of(entity);
                }
                // System.out.println("nah: " + playerName);
            }
        }

        return Optional.empty();
    }

    @Override
    protected boolean isSubEqual(AbstractDoToEntityTask other) {
        if (other instanceof KillPlayerTask task) {
            return Objects.equals(task.playerName, playerName);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Killing Player " + playerName;
    }
}
