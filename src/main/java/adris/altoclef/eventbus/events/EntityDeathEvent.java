package adris.altoclef.eventbus.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;

public class EntityDeathEvent {
    public Entity entity;
    public DamageSource damageSource;

    public EntityDeathEvent(Entity entity, DamageSource damageSource) {
        this.entity = entity;
        this.damageSource = damageSource;
    }
}
