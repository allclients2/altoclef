package adris.altoclef.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayerInteractionManager.class)
public interface NetworkHandlerAccessor {
    @Accessor("networkHandler")
    ClientPlayNetworkHandler getNetworkHandler();
}
