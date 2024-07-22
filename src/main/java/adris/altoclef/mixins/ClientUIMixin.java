package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ClientRenderEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.util.math.MatrixStack;


//#if MC>=12001
@Mixin(InGameHud.class)
public final class ClientUIMixin {
//#else
//$$ import net.minecraft.client.gui.DrawableHelper;
//$$
//$$ @Mixin(InGameHud.class)
//$$ public final class ClientUIMixin extends DrawableHelper {
//#endif


    //#if MC>=12001
    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    //#if MC >= 12100
    private void clientRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        EventBus.publish(new ClientRenderEvent(DrawContextWrapper.of(context), tickCounter.getTickDelta(true)));
    }
    //#else
    //#if MC >= 12001
    //$$ private void clientRender(DrawContext obj, float tickDelta, CallbackInfo ci) {
    //#else
    //$$    @Inject(
    //$$            method = "render",
    //$$            at = @At("TAIL")
    //$$    )
    //$$    private void clientRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
    //$$        EventBus.publish(new ClientRenderEvent(this, matrices, tickDelta));
    //$$    }
    //#endif
    //$$    EventBus.publish(new ClientRenderEvent(DrawContextWrapper.of(obj), tickDelta));
    //$$ }
    //#endif


}
