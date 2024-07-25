package adris.altoclef.mixins;

import net.minecraft.item.AxeItem;
import org.spongepowered.asm.mixin.Mixin;

//#if MC <= 11605
//$$ import net.minecraft.block.Material;
//$$ import org.spongepowered.asm.mixin.gen.Accessor;
//$$ import java.util.Set;
//#endif

@Mixin(AxeItem.class)
public interface AxeItemAccessor {

    //#if MC <= 11605
    //$$ @Accessor("field_23139")
    //$$ Set<net.minecraft.block.Material> getEffectiveMaterials();
    //#endif

}
