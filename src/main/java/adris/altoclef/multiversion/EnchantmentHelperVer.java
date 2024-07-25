package adris.altoclef.multiversion;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

import java.util.Objects;


public class EnchantmentHelperVer {

    @Pattern
    public static boolean hasBindingCurse(ItemStack stack) {
        //#if MC >= 12100
        return EnchantmentHelper.hasAnyEnchantmentsWith(stack, net.minecraft.component.EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE);
        //#else
        //$$ return EnchantmentHelper.hasBindingCurse(stack);
        //#endif
    }

    public static int getProtectionAmount(PlayerEntity player, DamageSource source) {
        //#if MC>=12100
        ServerWorld serverWorld = Objects.requireNonNull(player.getServer()).getWorld(player.getWorld().getRegistryKey());
        return (int) EnchantmentHelper.getProtectionAmount(serverWorld, player, source);
        //#else
        //$$ return (int) EnchantmentHelper.getProtectionAmount(player.getArmorItems(), source);
        //#endif
    }

}
