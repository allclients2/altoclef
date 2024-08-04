package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.DimensionChangedEvent;
import adris.altoclef.util.publicenums.Dimension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class WorldLoadMixin {

    @Inject(
            method = "setWorld",
            at = @At("HEAD")
    )
    private void setWorld(@Nullable ClientWorld world, CallbackInfo ci) {
        if (world != null) {
            final Dimension dimension = Dimension.dimensionFromWorldKey(world.getRegistryKey());
            if (dimension != null)
                EventBus.publish(
                    new DimensionChangedEvent(Dimension.dimensionFromWorldKey(world.getRegistryKey()), world)
                );
        }
    }
}

