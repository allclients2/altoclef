package adris.altoclef.tasks.speedrun.beatgame;

import adris.altoclef.tasks.speedrun.BeatMinecraftConfig;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class UselessItems {

    //dont even pick up these items
    public final Item[] uselessItems;

    public UselessItems(BeatMinecraftConfig config)  {
        List<Item> uselessItemList = new ArrayList<>(List.of(
                // random jung, might add more things in the future
                Items.FEATHER,
                Items.EGG,
                Items.BONE,
                Items.LEATHER,
                Items.WARPED_ROOTS,
                Items.GUNPOWDER,
                Items.MOSSY_COBBLESTONE,
                Items.SPRUCE_TRAPDOOR,
                Items.SANDSTONE_STAIRS,
                Items.STONE_BRICKS,
                Items.COARSE_DIRT,
                Items.SMOOTH_STONE,
                Items.FLOWER_POT,
                Items.POPPY,
                Items.SPIDER_EYE,
                Items.PINK_TULIP,

                //#if MC>=11800
                Items.RAW_COPPER,
                //#endif

                //#if MC>=11900
                Items.PINK_PETALS,
                Items.MANGROVE_ROOTS,
                Items.MUDDY_MANGROVE_ROOTS,
                //#endif

                Items.SPRUCE_STAIRS,
                Items.OAK_STAIRS,

                Items.LAPIS_LAZULI,
                Items.SUNFLOWER,
                Items.REDSTONE,
                Items.CRIMSON_ROOTS,
                Items.OAK_DOOR,
                Items.STRING,
                Items.WHITE_TERRACOTTA,
                Items.RED_TERRACOTTA,

                //#if MC>=11800
                Items.MOSS_BLOCK,
                Items.MOSS_CARPET,
                Items.CALCITE,
                Items.AMETHYST_BLOCK,
                Items.AMETHYST_CLUSTER,
                Items.AMETHYST_SHARD,
                Items.BUDDING_AMETHYST,
                Items.SMOOTH_BASALT,
                Items.FLOWERING_AZALEA,
                Items.COPPER_INGOT,
                Items.DRIPSTONE_BLOCK,
                Items.TUFF,
                Items.POINTED_DRIPSTONE,
                //#endif

                Items.YELLOW_TERRACOTTA,
                Items.IRON_NUGGET,
                Items.COBBLESTONE_WALL,
                Items.COBBLESTONE_STAIRS,
                Items.COBBLESTONE_SLAB,
                Items.CLAY_BALL,
                Items.DANDELION,
                Items.SUGAR_CANE,
                Items.AZURE_BLUET,
                Items.ACACIA_DOOR,
                Items.OAK_FENCE,
                Items.COMPOSTER,
                Items.OAK_PRESSURE_PLATE,
                Items.JUNGLE_DOOR,
                Items.CHISELED_SANDSTONE,
                Items.SMOOTH_SANDSTONE_SLAB,
                Items.SANDSTONE_WALL,
                Items.PRISMARINE_CRYSTALS,
                Items.SNOWBALL,
                Items.SPRUCE_STAIRS,
                Items.SPRUCE_DOOR,
                Items.SPRUCE_FENCE,
                Items.SPRUCE_FENCE_GATE,
                Items.ORANGE_TERRACOTTA,
                Items.HEART_OF_THE_SEA,
                Items.GLASS_BOTTLE,
                Items.ACACIA_SLAB,
                Items.RABBIT_HIDE,
                Items.RABBIT_FOOT,

                //#if MC>=11903
                Items.GRASS_BLOCK,
                //#else
                //$$ Items.GRASS,
                //#endif

                //#if MC>=11900
                Items.MUD,
                Items.MANGROVE_LEAVES,
                //#endif

                // nether stuff
                Items.SOUL_SAND,
                Items.SOUL_SOIL,
                Items.NETHER_BRICK,
                Items.NETHER_BRICK_FENCE
        ));

        // Saplings
        uselessItemList.addAll(Arrays.asList(ItemHelper.SAPLINGS));

        // Saplings
        uselessItemList.addAll(Arrays.asList(ItemHelper.SEEDS));

        if (!config.barterPearlsInsteadOfEndermanHunt) {
            uselessItemList.add(Items.GOLD_NUGGET);
        }


        uselessItems = uselessItemList.toArray(new Item[0]);
    }

}
