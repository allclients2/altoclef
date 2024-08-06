package adris.altoclef.mixins;

import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;

//#if MC <= 11904
//$$ import net.minecraft.client.util.math.MatrixStack;
//$$ import org.spongepowered.asm.mixin.gen.Invoker;
//#endif


@Mixin(DrawContext.class)
public interface DrawableHelperInvoker {

    //#if MC <= 11904
    //$$ @Invoker("drawHorizontalLine")
    //$$ void invokeDrawHorizontalLine(MatrixStack matrices, int x1, int x2, int y, int color);
    //$$
    //$$ @Invoker("drawVerticalLine")
    //$$ void invokeDrawVerticalLine(MatrixStack matrices, int x, int y1, int y2, int color);
    //#endif
}
