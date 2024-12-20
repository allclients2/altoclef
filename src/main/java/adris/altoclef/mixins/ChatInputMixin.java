package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.SendChatEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.network.ClientPlayerEntity;

//#if MC>=11904
@Mixin(ClientPlayNetworkHandler.class)
//#else
//$$ @Mixin(ClientPlayerEntity.class)
//#endif
public final class ChatInputMixin {

    @Inject(
            method = "sendChatMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sendChatMessage(String content, CallbackInfo ci) {
        SendChatEvent event = new SendChatEvent(content);
        EventBus.publish(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}
