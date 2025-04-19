package adris.altoclef.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.EntityDeathEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

@Mixin(LivingEntity.class)
public class MobDeathMixin {

    @Inject(at = @At("HEAD"), method = "onDeath")
    private void onDie(DamageSource damageSource, CallbackInfo info) {
        Entity died = (Entity) ((Object) this);
        EventBus.publish(new EntityDeathEvent(died, damageSource));
    }
}
