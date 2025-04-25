package adris.altoclef.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.EntitySwungEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class EntityAnimationSwungMixin {

    @Inject(method = "onEntityAnimation", at = @At("HEAD"))
    private void onEntityAnimation(EntityAnimationS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity entity = client.world.getEntityById(packet.getEntityId());

        if (entity == null) {
            return;
        }

        int id = packet.getAnimationId();
        if (id == EntityAnimationS2CPacket.SWING_MAIN_HAND || id == EntityAnimationS2CPacket.SWING_OFF_HAND) {
            // System.out.println("[Mixin] " + entity.getName().getString() + " swung main hand.");
            EventBus.publish(new EntitySwungEvent(entity));
        }
    }
}