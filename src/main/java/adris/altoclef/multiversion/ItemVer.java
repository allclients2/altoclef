package adris.altoclef.multiversion;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;

//#if MC <= 11605
//$$ import net.minecraft.tag.Tag;
//$$ import adris.altoclef.util.helpers.ItemHelper;
//$$ import net.minecraft.block.Block;
//$$ import java.util.ArrayList;
//$$ import java.util.Arrays;
//$$ import java.util.List;
//$$ import net.minecraft.tag.BlockTags;
//$$ import net.minecraft.block.Blocks;
//$$ import adris.altoclef.mixins.AxeItemAccessor;
//$$ import adris.altoclef.mixins.MiningToolItemAccessor;
//#endif

public class ItemVer {

    public static FoodComponentWrapper getFoodComponent(Item item) {
        //#if MC >=12005
        return FoodComponentWrapper.of(item.getComponents().get(net.minecraft.component.DataComponentTypes.FOOD));
        //#else
        //$$ return FoodComponentWrapper.of(item.getFoodComponent());
        //#endif
    }

    public static boolean isSuitableFor(Item item, BlockState state) {
        //#if MC <= 11605
        //$$ if (item instanceof PickaxeItem pickaxe) {
        //$$     return pickaxe.isSuitableFor(state);
        //$$ }
        //$$
        //$$ if (item instanceof MiningToolItem) {
        //$$     boolean isInEffectiveBlocks = ((MiningToolItemAccessor)item).getEffectiveBlocks().contains(state.getBlock());
        //$$
        //$$     if (item instanceof AxeItem) {
        //$$         return isInEffectiveBlocks || ((AxeItemAccessor)item).getEffectiveMaterials().contains(state.getMaterial());
        //$$     }
        //$$     return isInEffectiveBlocks;
        //$$ }
        //#endif

        //#if MC >= 12005
        return item.getDefaultStack().isSuitableFor(state);
        //#else
        //$$ return item.isSuitableFor(state);
        //#endif
    }

    public static int getMaxUseTime(ItemStack itemStack, PlayerEntity player) {
        //#if MC>=12100
        return itemStack.getMaxUseTime(player);
        //#else
        //$$ return itemStack.getMaxUseTime();
        //#endif
    }


    public static boolean isFood(ItemStack stack) {
        return isFood(stack.getItem());
    }

    public static boolean hasCustomName(ItemStack stack) {
        //#if MC >= 12005
        return stack.contains(net.minecraft.component.DataComponentTypes.CUSTOM_NAME);
        //#else
        //$$ return stack.hasCustomName();
        //#endif
    }

    public static boolean isFood(Item item) {
        //#if MC >=12005
        return item.getComponents().contains(net.minecraft.component.DataComponentTypes.FOOD);
        //#else
        //$$ return item.isFood();
        //#endif
    }


}
