package adris.altoclef.mixins;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.BlockVer;
import adris.altoclef.multiversion.PlayerVer;
import adris.altoclef.util.helpers.StorageHelper;
import baritone.Baritone;
import baritone.api.Settings;
import baritone.utils.ToolSet;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Mixin(ToolSet.class)
public class ToolSetMixin {

    //TODO: Fix this...

    @Shadow @Final private ClientPlayerEntity player;

    @Inject(method = "getBestSlot(Lnet/minecraft/block/Block;ZZ)I", at = @At("HEAD"), cancellable = true)
    public void inject(Block block, boolean preferSilkTouch, boolean pathingCalculation, CallbackInfoReturnable<Integer> cir) {
        if (BlockVer.getHardness(block) == 0) cir.setReturnValue(PlayerVer.getInventory(this.player).selectedSlot);
    }

    //#if MC >= 11800
    @Redirect(method = "getBestSlot(Lnet/minecraft/block/Block;ZZ)I",at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getDamage()I"))
    public int redirected(ItemStack stack,Block block) {
        if (StorageHelper.shouldSaveStack(AltoClef.INSTANCE,block,stack)) {
            return 100_000;
        }

        return stack.getDamage();
    }
    //#else
    //$$  // I see this solution as partial...
    //$$  @Redirect(method = "getBestSlot(Lnet/minecraft/block/Block;ZZ)I",at = @At(value = "INVOKE", target = "Lbaritone/utils/ToolSet;calculateSpeedVsBlock(Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/BlockState;)D"))
    //$$  public double redirected(ItemStack effLevel, BlockState item) {
    //$$      if (StorageHelper.shouldSaveStack(AltoClef.INSTANCE, item.getBlock(), effLevel)) {
    //$$          return 100_000;
    //$$      }
    //$$
    //$$      return ToolSet.calculateSpeedVsBlock(effLevel, item);
    //$$  }
    //#endif

}
