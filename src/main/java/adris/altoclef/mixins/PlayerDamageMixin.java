package adris.altoclef.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.PlayerDamageEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class PlayerDamageMixin {
    
    @Inject(
        method = "damage",
        at = @At("HEAD")
    )
    public void applyDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
        // System.out.println("DAMAGED: " + source.getAttacker() + " " + source.getName());
        // PlayerEntity p = (PlayerEntity) ((Object)this);
        // if ( !(p.getName().equals(MinecraftClient.getInstance().player.getName())) ) {
        //     // must be the client player
        //     return;
        // }
        EventBus.publish(new PlayerDamageEvent(source, amount));
    }
}
