package adris.altoclef.multiversion;

import net.minecraft.block.BlockState;
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
//#endif

public class ItemVer {

    public static FoodComponentWrapper getFoodComponent(Item item) {
        //#if MC >=12005
        return FoodComponentWrapper.of(item.getComponents().get(net.minecraft.component.DataComponentTypes.FOOD));
        //#else
        //$$ return FoodComponentWrapper.of(item.getFoodComponent());
        //#endif
    }

    //#if MC <= 11605
    //$$ private static final List<Tag.Identified<Block>> woodRelatedTags = List.of(
    //$$          BlockTags.PLANKS,
    //$$          BlockTags.LOGS,
    //$$          BlockTags.SIGNS,
    //$$          BlockTags.WOODEN_DOORS,
    //$$          BlockTags.WOODEN_BUTTONS,
    //$$          BlockTags.WOODEN_STAIRS,
    //$$          BlockTags.WOODEN_SLABS,
    //$$          BlockTags.WOODEN_FENCES,
    //$$          BlockTags.FENCE_GATES,
    //$$          BlockTags.WOODEN_PRESSURE_PLATES,
    //$$          BlockTags.WOODEN_TRAPDOORS,
    //$$          BlockTags.CAMPFIRES
    //$$  );
    //$$
    //$$ private static final List<Block> shovelObliterates = new ArrayList<>();
    //$$  static {
    //$$      shovelObliterates.addAll(Arrays.stream(ItemHelper.DIRTS).map(Block::getBlockFromItem).toList()); // Dirts
    //$$      shovelObliterates.addAll(ItemHelper.colorMap.values().stream().map(colorItems -> Block.getBlockFromItem(colorItems.concretePowder)).toList()); // Concrete Powder
    //$$      shovelObliterates.addAll(List.of( // Other Blocks
    //$$              Blocks.SNOW,
    //$$              Blocks.SNOW_BLOCK,
    //$$              Blocks.GRAVEL,
    //$$              Blocks.SAND
    //$$      ));
    //$$ }
    //#endif
    public static boolean isSuitableFor(Item itemUse, BlockState targetState) {
        //#if MC <= 11605
        //$$ final Block targetBlock = targetState.getBlock();
        //$$ if (itemUse instanceof AxeItem) {
        //$$     for (Tag.Identified<Block> blockTag : woodRelatedTags) {
        //$$         if (targetBlock.isIn(blockTag)) {
        //$$             return true;
        //$$         }
        //$$     }
        //$$     return targetBlock == Blocks.CRAFTING_TABLE;
        //$$ } else if (itemUse instanceof ShovelItem) {
        //$$     if (shovelObliterates.stream().anyMatch(block -> block == targetBlock))
        //$$         return true;
        //$$ } else if (itemUse instanceof HoeItem) {
        //$$     if (targetBlock == Blocks.HAY_BLOCK)
        //$$         return true;
        //$$ }
        //$$ return itemUse.isSuitableFor(targetState);
        //#elseif MC >= 12005
        return itemUse.getDefaultStack().isSuitableFor(targetState);
        //#else
        //$$ return itemUse.isSuitableFor(targetState);
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
