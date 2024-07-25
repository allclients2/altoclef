package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ClientRenderEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>=12001
@Mixin(InGameHud.class)
public final class ClientUIMixin {
//#else
//$$ import net.minecraft.client.gui.DrawableHelper;
//$$ import net.minecraft.client.util.math.MatrixStack;
//$$
//$$ @Mixin(InGameHud.class)
//$$ public final class ClientUIMixin extends DrawableHelper {
//#endif

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    //#if MC>=12100
    private void clientRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        EventBus.publish(new ClientRenderEvent(context, tickCounter.getTickDelta(true)));
    }
    //#elseif MC>=12001
    //$$ private void clientRender(DrawContext context, float tickDelta, CallbackInfo ci) {
    //$$    EventBus.publish(new ClientRenderEvent(context, tickDelta));
    //$$ }
    //#else
    //$$    private void clientRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
    //$$        EventBus.publish(new ClientRenderEvent(this, matrices, tickDelta));
    //$$    }
    //#endif
}