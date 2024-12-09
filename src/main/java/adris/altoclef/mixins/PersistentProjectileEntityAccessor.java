package adris.altoclef.mixins;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

//#if MC>=12103
@Mixin(PersistentProjectileEntity.class)
public interface PersistentProjectileEntityAccessor {
    @Invoker("isInGround")
    boolean isInGround();
}
//#else
//$$ @Mixin(PersistentProjectileEntity.class)
//$$ public interface PersistentProjectileEntityAccessor {
//$$     @Accessor("inGround")
//$$     boolean isInGround();
//$$ }
//#endif