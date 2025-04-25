package adris.altoclef.eventbus.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;

public class PlayerDamageEvent {
    public DamageSource source;
    public float damage;

    public PlayerDamageEvent(DamageSource source, float damage) {
        this.source = source;
        this.damage = damage;
    }
}
