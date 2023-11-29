package dev.tr7zw.itemswapper.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.itemswapper.ItemSwapperSharedMod;
import dev.tr7zw.itemswapper.packets.DisableModPayload;
import dev.tr7zw.itemswapper.packets.RefillSupportPayload;
import dev.tr7zw.itemswapper.packets.ShulkerSupportPayload;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    public void handleCustomPayload(CustomPacketPayload customPacketPayload, CallbackInfo ci) {
        try {
            if (customPacketPayload instanceof ShulkerSupportPayload payload) {
                ItemSwapperSharedMod.instance.setEnableShulkers(payload.enabled());
            }
            if (customPacketPayload instanceof DisableModPayload payload) {
                ItemSwapperSharedMod.instance.setModDisabled(payload.enabled());
            }
            if (customPacketPayload instanceof RefillSupportPayload payload) {
                ItemSwapperSharedMod.instance.setEnableRefill(payload.enabled());
            }
        } catch (Throwable th) {
            ItemSwapperSharedMod.LOGGER.error("Error while processing packet!", th);
        }
    }

}
