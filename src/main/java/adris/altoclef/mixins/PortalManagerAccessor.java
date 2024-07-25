package adris.altoclef.mixins;

import org.spongepowered.asm.mixin.Mixin;

//#if MC >= 12100
import net.minecraft.block.Portal;
import net.minecraft.world.dimension.PortalManager;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PortalManager.class)
public interface PortalManagerAccessor {
    @Accessor("portal")
    Portal accessPortal();
}
//#else
//$$ import adris.altoclef.util.DumbClass;
//$$ @Mixin(DumbClass.class)
//$$ public interface PortalManagerAccessor {}
//#endif