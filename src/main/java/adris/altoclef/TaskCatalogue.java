package adris.altoclef;

import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasks.inventory.CraftInInventoryTask;
import adris.altoclef.tasks.resources.ResourceTask;
import adris.altoclef.tasks.container.SmeltInFurnaceTask;
import adris.altoclef.tasks.container.UpgradeInSmithingTableTask;
import adris.altoclef.tasks.resources.*;
import adris.altoclef.tasks.resources.wood.*;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.util.*;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.publictypes.WoodType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

import static adris.altoclef.util.helpers.ItemHelper.MATERIAL_DATA;
import static adris.altoclef.util.helpers.ItemHelper.trimItemName;

/**
 * Contains a hardcoded list of ALL obtainable resources.
 * <p>
 * Most resources correspond to a single item, but some resources (like "log" or "door") include a range of items.
 * <p>
 * Call `TaskCatalogue.getItemTask` to return a task given a resource key.
 * Call `TaskCatalogue.getSquashedItemTask` to return a task that gets multiple resources, combining their steps.
 */
@SuppressWarnings({"rawtypes"})
public class TaskCatalogue {

    private static final HashMap<String, Item[]> nameToItemMatches = new HashMap<>();
    private static final HashMap<String, CataloguedResource> nameToResourceTask = new HashMap<>();
    private static final HashMap<Item, CataloguedResource> itemToResourceTask = new HashMap<>();
    private static final HashSet<Item> resourcesObtainable = new HashSet<>();

    // For the empty spot in all recipes here
    private static final String o = null;

    // Grid
    private interface IGrid<T> {
        T[] listContent(IntFunction<T[]> arraySupplier);
    }

    private record Grid3x3<T>(
        @Nullable T s0, @Nullable T s1, @Nullable T s2, @Nullable T s3,
        @Nullable T s4, @Nullable T s5, @Nullable T s6, @Nullable T s7, @Nullable T s8
    ) implements IGrid<T> {
        public T[] listContent(IntFunction<T[]> arraySupplier) {
            T[] array = arraySupplier.apply(9);
            array[0] = s0;
            array[1] = s1;
            array[2] = s2;
            array[3] = s3;
            array[4] = s4;
            array[5] = s5;
            array[6] = s6;
            array[7] = s7;
            array[8] = s8;
            return array;
        }
    }

    private record Grid2x2<T>(@Nullable T s0, @Nullable T s1, @Nullable T s2, @Nullable T s3) implements IGrid<T> {
        public T[] listContent(IntFunction<T[]> arraySupplier) {
            T[] array = arraySupplier.apply(4);
            array[0] = s0;
            array[1] = s1;
            array[2] = s2;
            array[3] = s3;
            return array;
        }
    }

    // For common shapes and outputs, like crafting stairs or slabs.
    private record RecipeTemplate(IGrid<String> gridShape, int outputCount) {}

    // For repetitive recipe shapes and patterns
    private static abstract class Templates {
        public static RecipeTemplate Slab(String material) {
            return new RecipeTemplate(new Grid3x3<>(o, o, o, o, o, o, material, material, material), 6);
        }

        public static RecipeTemplate Stairs(String material) {
            return new RecipeTemplate(new Grid3x3<>(material, o, o, material, material, o, material, material, material), 4);
        }

        public static RecipeTemplate Wall(String material) {
            return new RecipeTemplate(new Grid3x3<>(material, material, material, material, material, material, o, o, o), 4);
        }

        public static RecipeTemplate Chiseled(String slab) {
            return new RecipeTemplate(new Grid2x2<>(slab, o, slab, o), 4);
        }

        public static RecipeTemplate Block2x2(String material) {
            return new RecipeTemplate(new Grid2x2<>(material, material, material, material), 1);
        }

        public static RecipeTemplate Block3x3(String material) {
            return new RecipeTemplate(new Grid3x3<>(material, material, material, material, material, material, material, material, material), 1);
        }
    }

    public static void InitRecipes() {

        // COMMON ITEMS/STRINGS
        final String plk = "planks";
        final String stk = "stick";
        final String irn = "iron_ingot";
        final String gld = "gold_ingot";
        final String gln = "gold_nugget";
        final String cbl = "cobblestone";
        final String inn = "iron_nugget";

        /// RESOURCE DEFINITIONS HERE!!
        {
            /// RAW RESOURCES
            mine("log", MiningRequirement.HAND, ItemHelper.LOG, ItemHelper.LOG).anyDimension();
            woodTasks("log", wood -> wood.log, (wood, count) -> new MineAndCollectTask(wood.log, count, new Block[]{Block.getBlockFromItem(wood.log)}, MiningRequirement.HAND), true);
            mine("dirt", MiningRequirement.HAND, new Block[]{Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.DIRT_PATH}, Items.DIRT);
            custom(cbl, Items.COBBLESTONE, CollectBlockByOneTask.CollectCobblestoneTask::new).dontMineIfPresent();
            mine("andesite", MiningRequirement.WOOD, Blocks.ANDESITE, Items.ANDESITE);
            mine("granite", MiningRequirement.WOOD, Blocks.GRANITE, Items.GRANITE);
            mine("diorite", MiningRequirement.WOOD, Blocks.DIORITE, Items.DIORITE);
            mine("netherrack", MiningRequirement.WOOD, Blocks.NETHERRACK, Items.NETHERRACK).forceDimension(Dimension.NETHER);
            mine("magma_block", MiningRequirement.WOOD, Blocks.MAGMA_BLOCK, Items.MAGMA_BLOCK).forceDimension(Dimension.NETHER);
            mine("blackstone", MiningRequirement.WOOD, Blocks.BLACKSTONE, Items.BLACKSTONE).forceDimension(Dimension.NETHER);
            mine("basalt", MiningRequirement.WOOD, Blocks.BASALT, Items.BASALT).forceDimension(Dimension.NETHER);
            mine("soul_sand", Items.SOUL_SAND).forceDimension(Dimension.NETHER);
            mine("soul_soil", Items.SOUL_SOIL).forceDimension(Dimension.NETHER);
            mine("glowstone_dust", Blocks.GLOWSTONE, Items.GLOWSTONE_DUST).forceDimension(Dimension.NETHER);

            //#if MC>=11800
            mine("calcite", MiningRequirement.WOOD, Blocks.CALCITE, Items.CALCITE);
            mine("tuff", MiningRequirement.WOOD, Blocks.TUFF, Items.TUFF);
            custom("cobbled_deepslate", Items.COBBLED_DEEPSLATE, CollectBlockByOneTask.CollectCobbledDeepslateTask::new).dontMineIfPresent();
            //#endif

            // Specify Ores from ItemHelper info..
            MATERIAL_DATA.forEach((oreType, materialData) -> {
                final Item itemToIndex;
                if (materialData.rawItem != null) {
                    itemToIndex = materialData.rawItem;
                } else {
                    itemToIndex = materialData.defaultItem;
                }

                mine(ItemHelper.trimItemName(itemToIndex.getTranslationKey()).toLowerCase(), materialData.miningRequirement, Arrays.stream(materialData.oreBlocks).map(data -> data.oreBlock).toArray(Block[]::new), itemToIndex);
            });
            alias("lapis", "lapis_lazuli");

            //#if MC >= 11700
            mine("amethyst_shard", MiningRequirement.WOOD, Blocks.AMETHYST_CLUSTER, Items.AMETHYST_SHARD);
            mine("pointed_dripstone", MiningRequirement.WOOD, Blocks.POINTED_DRIPSTONE, Items.POINTED_DRIPSTONE);
            custom("amethyst_block", Items.AMETHYST_BLOCK, CollectAmethystBlockTask::new).dontMineIfPresent();
            custom("dripstone_block", Items.DRIPSTONE_BLOCK, CollectDripstoneBlockTask::new).dontMineIfPresent();
            //#endif

            mine("sand", Blocks.SAND, Items.SAND);
            mine("red_sand", Blocks.RED_SAND, Items.RED_SAND);
            mine("gravel", Blocks.GRAVEL, Items.GRAVEL);
            mine("clay_ball", Blocks.CLAY, Items.CLAY_BALL);
            mine("ancient_debris", MiningRequirement.DIAMOND, Blocks.ANCIENT_DEBRIS, Items.ANCIENT_DEBRIS).forceDimension(Dimension.NETHER);
            mine("gilded_blackstone", MiningRequirement.STONE, Blocks.GILDED_BLACKSTONE, Items.GILDED_BLACKSTONE).forceDimension(Dimension.NETHER);

            craftTmpl("glowstone", Items.GLOWSTONE, Templates.Block2x2("glowstone_dust")).dontMineIfPresent();
            craftTmpl("clay", Items.CLAY, Templates.Block2x2("clay_ball")).dontMineIfPresent();
            craftTmpl("bricks", Items.BRICKS, Templates.Block2x2("brick"));
            craftTmpl("brick_slab", Items.BRICK_SLAB, Templates.Slab("brick"));
            craftTmpl("brick_stairs", Items.BRICK_STAIRS, Templates.Stairs("brick"));
            craftTmpl("brick_wall", Items.BRICK_WALL, Templates.Wall("brick"));

            ItemHelper.woodMap.forEach((woodType, woodItems) -> {
                //Debug.logInternal("NAME: " + trimItemName(woodItems.sapling.getTranslationKey()));
                mine(trimItemName(woodItems.sapling.getTranslationKey()), Block.getBlockFromItem(woodItems.leaves), woodItems.sapling);
            });

            custom("sapling", ItemHelper.SAPLINGS, CollectSaplingsTask::new);
            custom("sandstone", Items.SANDSTONE, CollectSandstoneTask::new).dontMineIfPresent();
            custom("red_sandstone", Items.RED_SANDSTONE, CollectRedSandstoneTask::new).dontMineIfPresent();
            custom("coarse_dirt", Items.COARSE_DIRT, CollectCoarseDirtTask::new).dontMineIfPresent();
            custom("flint", Items.FLINT, CollectFlintTask::new);
            custom("obsidian", Items.OBSIDIAN, CollectObsidianTask::new).dontMineIfPresent();
            custom("wool", ItemHelper.WOOL, CollectWoolTask::new);
            custom("egg", Items.EGG, CollectEggsTask::new);

            // Mob drops
            mobDrop("bone", Items.BONE, SkeletonEntity.class);
            mobDrop("gunpowder", Items.GUNPOWDER, CreeperEntity.class);
            custom("ender_pearl", Items.ENDER_PEARL, KillEndermanTask::new);
            mobDrop("spider_eye", Items.SPIDER_EYE, SpiderEntity.class);
            mobDrop("leather", Items.LEATHER, CowEntity.class);
            mobDrop("feather", Items.FEATHER, ChickenEntity.class);
            mobDrop("rotten_flesh", Items.ROTTEN_FLESH, ZombieEntity.class);
            mobDrop("rabbit_foot", Items.RABBIT_FOOT, RabbitEntity.class);
            mobDrop("rabbit_hide", Items.RABBIT_HIDE, RabbitEntity.class);
            mobDrop("slime_ball", Items.SLIME_BALL, SlimeEntity.class);
            mobDrop("wither_skeleton_skull", Items.WITHER_SKELETON_SKULL, WitherSkeletonEntity.class).forceDimension(Dimension.NETHER);
            mobDrop("ink_sac", Items.INK_SAC, SquidEntity.class); // Warning, this probably won't work.
            mobDrop("string", Items.STRING, SpiderEntity.class); // Warning, this probably won't work.

            //#if MC >= 11800
            mobDrop("glow_ink_sac", Items.GLOW_INK_SAC, GlowSquidEntity.class); // Warning, this probably won't work.
            //#endif

            mine("sugar_cane", Items.SUGAR_CANE);
            mine("brown_mushroom", MiningRequirement.HAND, new Block[]{Blocks.BROWN_MUSHROOM, Blocks.BROWN_MUSHROOM_BLOCK}, Items.BROWN_MUSHROOM);
            mine("red_mushroom", MiningRequirement.HAND, new Block[]{Blocks.RED_MUSHROOM, Blocks.RED_MUSHROOM_BLOCK}, Items.RED_MUSHROOM);
            mine("mushroom", MiningRequirement.HAND, new Block[]{Blocks.BROWN_MUSHROOM, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.RED_MUSHROOM, Blocks.RED_MUSHROOM_BLOCK}, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM);
            mine("melon_slice", MiningRequirement.HAND, Blocks.MELON, Items.MELON_SLICE);
            mine("pumpkin", MiningRequirement.HAND, Blocks.PUMPKIN, Items.PUMPKIN);
            mine("bell", MiningRequirement.WOOD, Blocks.BELL, Items.BELL);
            mine("nether_wart", MiningRequirement.HAND, Blocks.NETHER_WART, Items.NETHER_WART).forceDimension(Dimension.NETHER);
            mine("crimson_roots", MiningRequirement.HAND, Blocks.CRIMSON_ROOTS, Items.CRIMSON_ROOTS).forceDimension(Dimension.NETHER);
            mine("warped_roots", MiningRequirement.HAND, Blocks.WARPED_ROOTS, Items.WARPED_ROOTS).forceDimension(Dimension.NETHER);
            mine("weeping_vines", MiningRequirement.HAND, Blocks.WEEPING_VINES, Items.WEEPING_VINES).forceDimension(Dimension.NETHER);
            mine("twisting_vines", MiningRequirement.HAND, Blocks.TWISTING_VINES, Items.TWISTING_VINES).forceDimension(Dimension.NETHER);
            mine("nether_wart_block", MiningRequirement.HAND, Blocks.NETHER_WART_BLOCK, Items.NETHER_WART_BLOCK).forceDimension(Dimension.NETHER);
            mine("warped_wart_block", MiningRequirement.HAND, Blocks.WARPED_WART_BLOCK, Items.WARPED_WART_BLOCK).forceDimension(Dimension.NETHER);
            mine("shroomlight", MiningRequirement.HAND, Blocks.SHROOMLIGHT, Items.SHROOMLIGHT).forceDimension(Dimension.NETHER);
            custom("blaze_rod", Items.BLAZE_ROD, CollectBlazeRodsTask::new).forceDimension(Dimension.NETHER); // Not super simple tbh lmao
            custom("cocoa_beans", Items.COCOA_BEANS, CollectCocoaBeansTask::new);
            shear("cobweb", Blocks.COBWEB, Items.COBWEB).dontMineIfPresent();
            colorfulTasks("wool", color -> color.wool, (color, count) -> new CollectWoolTask(color.color, count));

            // Misc greenery
            shear("leaves", ItemHelper.itemsToBlocks(ItemHelper.LEAVES), ItemHelper.LEAVES).dontMineIfPresent();
            for (CataloguedResource resource : woodTasks("leaves", woodItems -> woodItems.leaves, (woodItems, count) -> {
                if (woodItems.isNetherWood()) {
                    // Nether "leaves" aren't sheared, they can simply be mined.
                    return new MineAndCollectTask(woodItems.leaves, count, new Block[]{Block.getBlockFromItem(woodItems.leaves)}, MiningRequirement.HAND).forceDimension(Dimension.NETHER);
                } else {
                    return new ShearAndCollectBlockTask(woodItems.leaves, count, Block.getBlockFromItem(woodItems.leaves));
                }
            })) {
                resource.dontMineIfPresent();
            }

            shear("vine", Blocks.VINE, Items.VINE).dontMineIfPresent();
            shear("grass", Blocks.SHORT_GRASS, Items.SHORT_GRASS).dontMineIfPresent();
            shear("lily_pad", Blocks.LILY_PAD, Items.LILY_PAD).dontMineIfPresent();
            shear("tall_grass", Blocks.TALL_GRASS, Items.TALL_GRASS).dontMineIfPresent();
            shear("fern", Blocks.FERN, Items.FERN).dontMineIfPresent();
            shear("large_fern", Blocks.LARGE_FERN, Items.LARGE_FERN).dontMineIfPresent();
            shear("dead_bush", Blocks.DEAD_BUSH, Items.DEAD_BUSH).dontMineIfPresent();

            //#if MC >= 11800
            shear("glow_lichen", Blocks.GLOW_LICHEN, Items.GLOW_LICHEN).dontMineIfPresent();
            //#endif

            // Flowers
            custom("flower", ItemHelper.FLOWER, CollectFlowerTask::new);
            mine("allium", Items.ALLIUM);
            mine("azure_bluet", Items.AZURE_BLUET);
            mine("blue_orchid", Items.BLUE_ORCHID);
            mine("cactus", Items.CACTUS);
            mine("cornflower", Items.CORNFLOWER);
            mine("dandelion", Items.DANDELION);
            mine("lilac", Items.LILAC);
            mine("lily_of_the_valley", Items.LILY_OF_THE_VALLEY);
            mine("orange_tulip", Items.ORANGE_TULIP);
            mine("oxeye_daisy", Items.OXEYE_DAISY);
            mine("pink_tulip", Items.PINK_TULIP);
            mine("poppy", Items.POPPY);
            mine("peony", Items.PEONY);
            mine("red_tulip", Items.RED_TULIP);
            mine("rose_bush", Items.ROSE_BUSH);
            mine("sunflower", Items.SUNFLOWER);
            mine("white_tulip", Items.WHITE_TULIP);

            // Crops
            custom("wheat", Items.WHEAT, CollectWheatTask::new);
            custom("wheat_seeds", Items.WHEAT_SEEDS, CollectWheatSeedsTask::new);
            harvest("carrot", Items.CARROT, Blocks.CARROTS, Items.CARROT);
            harvest("potato", Items.POTATO, Blocks.POTATOES, Items.POTATO);
            harvest("poisonous_potato", Items.POISONOUS_POTATO, Blocks.POTATOES, Items.POTATO);
            harvest("beetroot", Items.BEETROOT, Blocks.BEETROOTS, Items.BEETROOT_SEEDS);
            harvest("beetroot_seeds", Items.BEETROOT_SEEDS, Blocks.BEETROOTS, Items.BEETROOT_SEEDS);

            craftTmpl("melon", Items.MELON, Templates.Block3x3("melon_slice")).dontMineIfPresent();

            craftGrid("glistering_melon_slice", Items.GLISTERING_MELON_SLICE, 1, new Grid3x3<>(gln, gln, gln, gln, "melon_slice", gln, gln, gln, gln));
            craftGrid("sugar", Items.SUGAR, 1, new Grid2x2<>("sugar_cane", o, o, o));
            craftGrid("bone_meal", Items.BONE_MEAL, 3, new Grid2x2<>("bone", o, o, o));
            craftGrid("melon_seeds", Items.MELON_SEEDS, 1, new Grid2x2<>("melon_slice", o, o, o));
            custom("hay_block", Items.HAY_BLOCK, CollectHayBlockTask::new).dontMineIfPresent();


            // MATERIALS
            custom("planks", ItemHelper.PLANKS, CollectPlanksTask::new).dontMineIfPresent();
            // Per-tree Planks. At the moment, nether planks need to be specified that their logs are in the nether.
            for (CataloguedResource woodCatalogue : woodTasks("planks", wood -> wood.planks, (wood, count) -> {
                final CollectPlanksTask result = new CollectPlanksTask(wood.planks, count);
                return wood.isNetherWood() ? result.logsInNether() : result;
            }, true)) {
                // Don't mine individual planks either!! Handled internally.
                woodCatalogue.dontMineIfPresent();
            }

            custom("stripped_logs", ItemHelper.STRIPPED_LOG, CollectStrippedLogTask::new).dontMineIfPresent();
            for (CataloguedResource woodCatalogue : woodTasks("stripped_logs", wood -> wood.strippedLog, (wood, count) -> new CollectStrippedLogTask(wood.strippedLog, count))) {
                woodCatalogue.dontMineIfPresent();
            }

            custom("stick", Items.STICK, CollectSticksTask::new);
            smelt("stone", Items.STONE, cbl).dontMineIfPresent();

            //#if MC>=11800
            smelt("deepslate", Items.DEEPSLATE, "cobbled_deepslate").dontMineIfPresent();
            smelt("smooth_basalt", Items.SMOOTH_BASALT, "basalt");
            //#endif

            smelt("smooth_stone", Items.SMOOTH_STONE, "stone");
            smelt("smooth_quartz", Items.SMOOTH_QUARTZ, "quartz_block");
            smelt("glass", Items.GLASS, "sand").dontMineIfPresent();
            smelt("charcoal", Items.CHARCOAL, "log");
            smelt("brick", Items.BRICK, "clay_ball");
            smelt("nether_brick", Items.NETHER_BRICK, "netherrack");
            smelt("green_dye", Items.GREEN_DYE, "cactus");

            custom(gln, Items.GOLD_NUGGET, CollectGoldNuggetsTask::new);
            custom(irn, Items.IRON_INGOT, CollectIronIngotTask::new).forceDimension(Dimension.OVERWORLD);
            custom(gld, Items.GOLD_INGOT, CollectGoldIngotTask::new).anyDimension(); // accounts for nether too
            smelt("netherite_scrap", Items.NETHERITE_SCRAP, "ancient_debris");
            craftGrid("netherite_ingot", Items.NETHERITE_INGOT, 1, new Grid3x3<>("netherite_scrap", "netherite_scrap", "netherite_scrap", "netherite_scrap", gld, gld, gld, gld, o));


            // Copper Blocks
            //#if MC>=11700
            smelt("copper_ingot", Items.COPPER_INGOT, "raw_copper", Items.COPPER_ORE);
            craftTmpl("raw_iron_block", Items.RAW_IRON_BLOCK, Templates.Block3x3("raw_iron"));
            craftTmpl("raw_gold_block", Items.RAW_GOLD_BLOCK, Templates.Block3x3("raw_gold"));
            craftTmpl("raw_copper_block", Items.RAW_COPPER_BLOCK, Templates.Block3x3("raw_copper"));
            //#endif

            // Ore Blocks
            MATERIAL_DATA.forEach((oreType, materialData) -> craftTmpl(trimItemName(materialData.fullBlock.getTranslationKey()), materialData.fullBlock.asItem(), Templates.Block3x3(trimItemName(materialData.defaultItem.getTranslationKey()))));

            craftTmpl("polished_andesite", Items.POLISHED_ANDESITE, Templates.Block2x2("andesite"), 4);
            craftTmpl("polished_diorite", Items.POLISHED_DIORITE, Templates.Block2x2("diorite"), 4);
            craftTmpl("polished_granite", Items.POLISHED_GRANITE, Templates.Block2x2("granite"), 4);
            craftTmpl("polished_blackstone", Items.POLISHED_BLACKSTONE, Templates.Block2x2("blackstone"), 4);
            craftTmpl("polished_blackstone_bricks", Items.POLISHED_BLACKSTONE_BRICKS, Templates.Block2x2("polished_blackstone"), 4);
            craftTmpl("polished_basalt", Items.POLISHED_BASALT, Templates.Block2x2("basalt"), 4);
            craftTmpl("cut_sandstone", Items.CUT_SANDSTONE, Templates.Block2x2("sandstone"), 4);
            craftTmpl("cut_red_sandstone", Items.CUT_RED_SANDSTONE, Templates.Block2x2("red_sandstone"), 4);
            craftTmpl("quartz_bricks", Items.QUARTZ_BRICKS, Templates.Block2x2("quartz_block"), 4);
            craftTmpl("stone_bricks", Items.STONE_BRICKS, Templates.Block2x2("stone"), 4);
            craftGrid("quartz_pillar", Items.QUARTZ_PILLAR, 4, new Grid2x2<>("quartz_block", o, "quartz_block", o));
            craftGrid("mossy_stone_bricks", Items.MOSSY_STONE_BRICKS, 1, new Grid2x2<>("stone_bricks", "vine", o, o));
            craftGrid("mossy_cobblestone", Items.MOSSY_COBBLESTONE, 1, new Grid2x2<>(cbl, "vine", o, o));
            custom("nether_bricks", Items.NETHER_BRICKS, CollectNetherBricksTask::new).dontMineIfPresent();
            craftGrid("nether_brick_fence", Items.NETHER_BRICK_FENCE, 6, new Grid3x3<>(o, o, o, "nether_bricks", "nether_brick", "nether_bricks", "nether_bricks", "nether_brick", "nether_bricks"));
            craftTmpl("red_nether_bricks", Items.RED_NETHER_BRICKS, Templates.Block2x2("nether_wart"), 4);
            smelt("cracked_stone_bricks", Items.CRACKED_STONE_BRICKS, "stone_bricks");
            smelt("cracked_nether_bricks", Items.CRACKED_NETHER_BRICKS, "nether_bricks");
            smelt("cracked_polished_blackstone_bricks", Items.CRACKED_POLISHED_BLACKSTONE_BRICKS, "polished_blackstone_bricks");
            smelt("smooth_sandstone", Items.SMOOTH_SANDSTONE, "sandstone");
            smelt("smooth_red_sandstone", Items.SMOOTH_RED_SANDSTONE, "red_sandstone");

            //#if MC >= 11800
            craftTmpl("polished_deepslate", Items.POLISHED_DEEPSLATE, Templates.Block2x2("cobbled_deepslate"), 4);
            craftTmpl("deepslate_bricks", Items.DEEPSLATE_BRICKS, Templates.Block2x2("polished_deepslate"), 4);
            craftTmpl("deepslate_tiles", Items.DEEPSLATE_TILES, Templates.Block2x2("deepslate_bricks"), 4);
            craftTmpl("cut_copper", Items.CUT_COPPER, Templates.Block2x2("copper_block"), 4);
            smelt("cracked_deepslate_bricks", Items.CRACKED_DEEPSLATE_BRICKS, "deepslate_bricks");
            smelt("cracked_deepslate_tiles", Items.CRACKED_DEEPSLATE_TILES, "deepslate_tiles");
            //#endif

            {
                // Slabs + Stairs + Walls
                craftTmpl("cobblestone_slab", Items.COBBLESTONE_SLAB, Templates.Slab(cbl));
                craftTmpl("cobblestone_stairs", Items.COBBLESTONE_STAIRS, Templates.Stairs(cbl));
                craftTmpl("cobblestone_wall", Items.COBBLESTONE_WALL, Templates.Wall(cbl));
                craftTmpl("stone_slab", Items.STONE_SLAB, Templates.Slab("stone"));
                craftTmpl("stone_stairs", Items.STONE_STAIRS, Templates.Stairs("stone"));
                craftTmpl("smooth_stone_slab", Items.SMOOTH_STONE_SLAB, Templates.Slab("smooth_stone"));
                craftTmpl("stone_brick_slab", Items.STONE_BRICK_SLAB, Templates.Slab("stone_bricks"));
                craftTmpl("stone_brick_stairs", Items.STONE_BRICK_STAIRS, Templates.Stairs("stone_bricks"));
                craftTmpl("stone_brick_wall", Items.STONE_BRICK_WALL, Templates.Wall("stone_bricks"));
                craftTmpl("mossy_stone_brick_slab", Items.MOSSY_STONE_BRICK_SLAB, Templates.Slab("mossy_stone_bricks"));
                craftTmpl("mossy_stone_brick_stairs", Items.MOSSY_STONE_BRICK_STAIRS, Templates.Stairs("mossy_stone_bricks"));
                craftTmpl("mossy_stone_brick_wall", Items.MOSSY_STONE_BRICK_WALL, Templates.Wall("mossy_stone_bricks"));
                craftTmpl("mossy_cobblestone_slab", Items.MOSSY_COBBLESTONE_SLAB, Templates.Slab("mossy_cobblestone"));
                craftTmpl("mossy_cobblestone_stairs", Items.MOSSY_COBBLESTONE_STAIRS, Templates.Stairs("mossy_cobblestone"));
                craftTmpl("mossy_cobblestone_wall", Items.MOSSY_COBBLESTONE_WALL, Templates.Wall("mossy_cobblestone"));
                craftTmpl("andesite_slab", Items.ANDESITE_SLAB, Templates.Slab("andesite"));
                craftTmpl("andesite_stairs", Items.ANDESITE_STAIRS, Templates.Stairs("andesite"));
                craftTmpl("andesite_wall", Items.ANDESITE_WALL, Templates.Wall("andesite"));
                craftTmpl("granite_slab", Items.GRANITE_SLAB, Templates.Slab("granite"));
                craftTmpl("granite_stairs", Items.GRANITE_STAIRS, Templates.Stairs("granite"));
                craftTmpl("granite_wall", Items.GRANITE_WALL, Templates.Wall("granite"));
                craftTmpl("diorite_stairs", Items.DIORITE_STAIRS, Templates.Stairs("diorite"));
                craftTmpl("diorite_wall", Items.DIORITE_WALL, Templates.Wall("diorite"));
                craftTmpl("polished_andesite_slab", Items.POLISHED_ANDESITE_SLAB, Templates.Slab("polished_andesite"));
                craftTmpl("polished_andesite_stairs", Items.POLISHED_ANDESITE_STAIRS, Templates.Stairs("polished_andesite"));
                craftTmpl("polished_granite_slab", Items.POLISHED_GRANITE_SLAB, Templates.Slab("polished_granite"));
                craftTmpl("polished_granite_stairs", Items.POLISHED_GRANITE_STAIRS, Templates.Stairs("polished_granite"));
                craftTmpl("polished_diorite_slab", Items.POLISHED_DIORITE_SLAB, Templates.Slab("polished_diorite"));
                craftTmpl("polished_diorite_stairs", Items.POLISHED_DIORITE_STAIRS, Templates.Stairs("polished_diorite"));
                craftTmpl("sandstone_slab", Items.SANDSTONE_SLAB, Templates.Slab("sandstone"));
                craftTmpl("sandstone_stairs", Items.SANDSTONE_STAIRS, Templates.Stairs("sandstone"));
                craftTmpl("sandstone_wall", Items.SANDSTONE_WALL, Templates.Wall("sandstone"));
                craftTmpl("cut_sandstone_slab", Items.CUT_SANDSTONE_SLAB, Templates.Slab("cut_sandstone"));
                craftTmpl("smooth_sandstone_slab", Items.SMOOTH_SANDSTONE_SLAB, Templates.Slab("smooth_sandstone"));
                craftTmpl("smooth_sandstone_stairs", Items.SMOOTH_SANDSTONE_STAIRS, Templates.Stairs("smooth_sandstone"));
                craftTmpl("red_sandstone_slab", Items.RED_SANDSTONE_SLAB, Templates.Slab("red_sandstone"));
                craftTmpl("red_sandstone_stairs", Items.RED_SANDSTONE_STAIRS, Templates.Stairs("red_sandstone"));
                craftTmpl("red_sandstone_wall", Items.RED_SANDSTONE_WALL, Templates.Wall("red_sandstone"));
                craftTmpl("cut_red_sandstone_slab", Items.CUT_RED_SANDSTONE_SLAB, Templates.Slab("cut_red_sandstone"));
                craftTmpl("smooth_red_sandstone_slab", Items.SMOOTH_RED_SANDSTONE_SLAB, Templates.Slab("smooth_red_sandstone"));
                craftTmpl("smooth_red_sandstone_stairs", Items.SMOOTH_RED_SANDSTONE_STAIRS, Templates.Stairs("smooth_red_sandstone"));
                craftTmpl("nether_brick_slab", Items.NETHER_BRICK_SLAB, Templates.Slab("nether_bricks"));
                craftTmpl("nether_brick_stairs", Items.NETHER_BRICK_STAIRS, Templates.Stairs("nether_bricks"));
                craftTmpl("nether_brick_wall", Items.NETHER_BRICK_WALL, Templates.Wall("nether_bricks"));
                craftTmpl("red_nether_brick_slab", Items.RED_NETHER_BRICK_SLAB, Templates.Slab("red_nether_bricks"));
                craftTmpl("red_nether_brick_stairs", Items.RED_NETHER_BRICK_STAIRS, Templates.Stairs("red_nether_bricks"));
                craftTmpl("red_nether_brick_wall", Items.RED_NETHER_BRICK_WALL, Templates.Wall("red_nether_bricks"));
                craftTmpl("quartz_slab", Items.QUARTZ_SLAB, Templates.Slab("quartz_block"));
                craftTmpl("quartz_stairs", Items.QUARTZ_STAIRS, Templates.Stairs("quartz_block"));
                craftTmpl("smooth_quartz_slab", Items.SMOOTH_QUARTZ_SLAB, Templates.Slab("smooth_quartz"));
                craftTmpl("smooth_quartz_stairs", Items.SMOOTH_QUARTZ_STAIRS, Templates.Stairs("smooth_quartz"));
                craftTmpl("blackstone_slab", Items.BLACKSTONE_SLAB, Templates.Slab("blackstone"));
                craftTmpl("blackstone_stairs", Items.BLACKSTONE_STAIRS, Templates.Stairs("blackstone"));
                craftTmpl("blackstone_wall", Items.BLACKSTONE_WALL, Templates.Wall("blackstone"));
                craftTmpl("polished_blackstone_slab", Items.POLISHED_BLACKSTONE_SLAB, Templates.Slab("polished_blackstone"));
                craftTmpl("polished_blackstone_stairs", Items.POLISHED_BLACKSTONE_STAIRS, Templates.Stairs("polished_blackstone"));
                craftTmpl("polished_blackstone_wall", Items.POLISHED_BLACKSTONE_WALL, Templates.Wall("polished_blackstone"));
                craftTmpl("polished_blackstone_brick_slab", Items.POLISHED_BLACKSTONE_BRICK_SLAB, Templates.Slab("polished_blackstone_bricks"));
                craftTmpl("polished_blackstone_brick_stairs", Items.POLISHED_BLACKSTONE_BRICK_STAIRS, Templates.Stairs("polished_blackstone_bricks"));
                craftTmpl("polished_blackstone_brick_wall", Items.POLISHED_BLACKSTONE_BRICK_WALL, Templates.Wall("polished_blackstone_bricks"));
                craftTmpl("chiseled_sandstone", Items.CHISELED_SANDSTONE, Templates.Chiseled("sandstone_slab"));
                craftTmpl("chiseled_red_sandstone", Items.CHISELED_RED_SANDSTONE, Templates.Chiseled("red_sandstone_slab"));
                craftTmpl("chiseled_stone_bricks", Items.CHISELED_STONE_BRICKS, Templates.Chiseled("stone_brick_slab"));
                craftTmpl("chiseled_nether_bricks", Items.CHISELED_NETHER_BRICKS, Templates.Chiseled("nether_brick_slab"));
                craftTmpl("chiseled_quartz_block", Items.CHISELED_QUARTZ_BLOCK, Templates.Chiseled("quartz_slab"));

                //#if MC>=11800
                craftTmpl("cut_copper_slab", Items.CUT_COPPER_SLAB, Templates.Slab("cut_copper"));
                craftTmpl("cut_copper_stairs", Items.CUT_COPPER_STAIRS, Templates.Stairs("cut_copper"));
                craftTmpl("cobbled_deepslate_slab", Items.COBBLED_DEEPSLATE_SLAB, Templates.Slab("cobbled_deepslate"));
                craftTmpl("cobbled_deepslate_stairs", Items.COBBLED_DEEPSLATE_STAIRS, Templates.Stairs("cobbled_deepslate"));
                craftTmpl("cobbled_deepslate_wall", Items.COBBLED_DEEPSLATE_WALL, Templates.Wall("cobbled_deepslate"));
                craftTmpl("polished_deepslate_slab", Items.POLISHED_DEEPSLATE_SLAB, Templates.Slab("polished_deepslate"));
                craftTmpl("polished_deepslate_stairs", Items.POLISHED_DEEPSLATE_STAIRS, Templates.Stairs("polished_deepslate"));
                craftTmpl("polished_deepslate_wall", Items.POLISHED_DEEPSLATE_WALL, Templates.Wall("polished_deepslate"));
                craftTmpl("deepslate_brick_slab", Items.DEEPSLATE_BRICK_SLAB, Templates.Slab("deepslate_bricks"));
                craftTmpl("deepslate_brick_stairs", Items.DEEPSLATE_BRICK_STAIRS, Templates.Stairs("deepslate_bricks"));
                craftTmpl("deepslate_brick_wall", Items.DEEPSLATE_BRICK_WALL, Templates.Wall("deepslate_bricks"));
                craftTmpl("deepslate_tile_slab", Items.DEEPSLATE_TILE_SLAB, Templates.Slab("deepslate_tiles"));
                craftTmpl("deepslate_tile_stairs", Items.DEEPSLATE_TILE_STAIRS, Templates.Stairs("deepslate_tiles"));
                craftTmpl("deepslate_tile_wall", Items.DEEPSLATE_TILE_WALL, Templates.Wall("deepslate_tiles"));
                craftTmpl("chiseled_deepslate", Items.CHISELED_DEEPSLATE, Templates.Chiseled("cobbled_deepslate_slab"));
                //#endif
            }

            /// MATERIAL-BASED ITEMS (WOOD, STONE, IRON, ETC.)
            tools("wooden", "planks", Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_SWORD, Items.WOODEN_AXE, Items.WOODEN_HOE);
            tools("stone", cbl, Items.STONE_PICKAXE, Items.STONE_SHOVEL, Items.STONE_SWORD, Items.STONE_AXE, Items.STONE_HOE);
            tools("iron", irn, Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_SWORD, Items.IRON_AXE, Items.IRON_HOE);
            tools("golden", gld, Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_SWORD, Items.GOLDEN_AXE, Items.GOLDEN_HOE);
            tools("diamond", "diamond", Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_HOE);

            alias("wooden_pick", "wooden_pickaxe");
            alias("stone_pick", "stone_pickaxe");
            alias("iron_pick", "iron_pickaxe");
            alias("gold_pick", "golden_pickaxe");
            alias("diamond_pick", "diamond_pickaxe");
            alias("netherite_pick", "netherite_pickaxe");

            armor("leather", "leather", Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
            armor("iron", irn, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
            armor("golden", gld, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
            armor("diamond", "diamond", Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);

            // NETHERITE SMITHING
            smith("netherite_helmet", Items.NETHERITE_HELMET, "netherite_ingot", "diamond_helmet");
            smith("netherite_chestplate", Items.NETHERITE_CHESTPLATE, "netherite_ingot", "diamond_chestplate");
            smith("netherite_leggings", Items.NETHERITE_LEGGINGS, "netherite_ingot", "diamond_leggings");
            smith("netherite_boots", Items.NETHERITE_BOOTS, "netherite_ingot", "diamond_boots");
            smith("netherite_pickaxe", Items.NETHERITE_PICKAXE, "netherite_ingot", "diamond_pickaxe");
            smith("netherite_axe", Items.NETHERITE_AXE, "netherite_ingot", "diamond_axe");
            smith("netherite_shovel", Items.NETHERITE_SHOVEL, "netherite_ingot", "diamond_shovel");
            smith("netherite_sword", Items.NETHERITE_SWORD, "netherite_ingot", "diamond_sword");
            smith("netherite_hoe", Items.NETHERITE_HOE, "netherite_ingot", "diamond_hoe");

            //#if MC >= 12000
            custom("netherite_upgrade_smithing_template", Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, GetSmithingTemplateTask::new);
            alias("netherite_upgrade", "netherite_upgrade_smithing_template");
            //#endif

            // BUCKETS
            custom("water_bucket", Items.WATER_BUCKET, CollectBucketLiquidTask.CollectWaterBucketTask::new);
            custom("lava_bucket", Items.LAVA_BUCKET, CollectBucketLiquidTask.CollectLavaBucketTask::new);

            // MISCELLANEOUS
            craftGrid("bow", Items.BOW, 1, new Grid3x3<>("string", stk, o, "string", o, stk, "string", stk, o));
            craftGrid("arrow", Items.ARROW, 4, new Grid3x3<>("flint", o, o, stk, o, o, "feather", o, o));
            craftGrid("bucket", Items.BUCKET, 1, new Grid3x3<>(irn, o, irn, o, irn, o, o, o, o));
            craftGrid("flint_and_steel", Items.FLINT_AND_STEEL, 1, new Grid2x2<>(irn, o, o, "flint"));
            craftGrid("shears", Items.SHEARS, 1, new Grid2x2<>(irn, o, o, irn));
            craftGrid(inn, Items.IRON_NUGGET, 9, new Grid2x2<>(irn, o, o, o));
            craftGrid("compass", Items.COMPASS, 1, new Grid3x3<>(o, irn, o, irn, "redstone", irn, o, irn, o));
            craftGrid("shield", Items.SHIELD, 1, new Grid3x3<>(plk, irn, plk, plk, plk, plk, o, plk, o));
            craftGrid("clock", Items.CLOCK, 1, new Grid3x3<>(o, gld, o, gld, "redstone", gld, o, gld, o));
            craftGrid("map", Items.MAP, 1, new Grid3x3<>("paper", "paper", "paper", "paper", "compass", "paper", "paper", "paper", "paper"));
            craftGrid("fishing_rod", Items.FISHING_ROD, 1, new Grid3x3<>(o, o, stk, o, stk, "string", stk, o, "string"));
            craftGrid("carrot_on_a_stick", Items.CARROT_ON_A_STICK, 1, new Grid2x2<>("fishing_rod", "carrot", o, o));
            craftGrid("warped_fungus_on_a_stick", Items.WARPED_FUNGUS_ON_A_STICK, 1, new Grid2x2<>("fishing_rod", "warped_fungus", o, o));
            craftGrid("glass_bottle", Items.GLASS_BOTTLE, 3, new Grid3x3<>("glass", o, "glass", o, "glass", o, o, o, o));
            craftGrid("leather_horse_armor", Items.LEATHER_HORSE_ARMOR, 1, new Grid3x3<>("leather", o, "leather", "leather", "leather", "leather", "leather", o, "leather"));
            custom("boat", ItemHelper.WOOD_BOAT, CollectBoatTask::new);
            woodTasks("boat", woodItems -> woodItems.boat, (woodItems, count) -> new CollectBoatTask(woodItems.boat, woodItems.prefix + "_planks", count));
            craftGrid("lead", Items.LEAD, 1, new Grid3x3<>("string", "string", o, "string", "slime_ball", o, o, o, "string"));
            custom("honeycomb", Items.HONEYCOMB, CollectHoneycombTask::new);
            craftTmpl("honeycomb_block", Items.HONEYCOMB_BLOCK, Templates.Block2x2("honeycomb"));
            craftGrid("beehive", Items.BEEHIVE, 1, new Grid3x3<>(plk, plk, plk, "honeycomb", "honeycomb", "honeycomb", plk, plk, plk));
            craftGrid("paper", Items.PAPER, 3, new Grid3x3<>("sugar_cane", "sugar_cane", "sugar_cane", o, o, o, o, o, o));
            craftGrid("book", Items.BOOK, 1, new Grid2x2<>("paper", "paper", "paper", "leather"));
            craftGrid("writable_book", Items.WRITABLE_BOOK, 1, new Grid2x2<>("book", "ink_sac", o, "feather"));
            alias("book_and_quill", "writable_book");
            craftGrid("bowl", Items.BOWL, 4, new Grid3x3<>(plk, o, plk, o, plk, o, o, o, o));
            craftGrid("blaze_powder", Items.BLAZE_POWDER, 2, new Grid2x2<>("blaze_rod", o, o, o));
            craftGrid("ender_eye", Items.ENDER_EYE, 1, new Grid2x2<>("blaze_powder", "ender_pearl", o, o));
            alias("eye_of_ender", "ender_eye");
            craftGrid("fermented_spider_eye", Items.FERMENTED_SPIDER_EYE, 1, new Grid2x2<>("brown_mushroom", "sugar", o, "spider_eye"));
            craftGrid("fire_charge", Items.FIRE_CHARGE, 3, new Grid3x3<>(o, "blaze_powder", o, o, "coal", o, o, "gunpowder", o));
            craftGrid("firework_rocket", Items.FIREWORK_ROCKET, 2, new Grid2x2<>("gunpowder", "paper", o, o));
            craftGrid("flower_banner_pattern", Items.FLOWER_BANNER_PATTERN, 1, new Grid2x2<>("gunpowder", "paper", o, o));
            custom("magma_cream", Items.MAGMA_CREAM, CollectMagmaCreamTask::new);

            //#if MC>=11700
            craftGrid("candle", Items.CANDLE, 1, new Grid2x2<>("string", o, "honeycomb", o));
            //#endif
            //#if MC>=11800
            craftGrid("spyglass", Items.SPYGLASS, 1, new Grid3x3<>(o, "amethyst_shard", o, o, "copper_ingot", o, o, "copper_ingot", o));
            //#endif
            //#if MC>=12000
            craftGrid("brush", Items.BRUSH, 1, new Grid3x3<>(o, "feather", o, o, "copper_ingot", o, o, stk, o));
            //#endif

            // PRESSURE PLATES?
            custom("wooden_pressure_plate", ItemHelper.WOOD_PRESSURE_PLATE, CollectWoodenPressurePlateTask::new);
            //woodTasks("pressure_plate", woodItems -> woodItems.pressurePlate, (woodItems, count) -> new CollectWoodenPressurePlateTask(woodItems.pressurePlate, woodItems.prefix + "_planks", count));
            custom("wooden_button", ItemHelper.WOOD_BUTTON, CollectWoodenButtonTask::new);
            //woodTasks("button", woodItems -> woodItems.button, (woodItems, count) -> new CraftInInventoryTask(new RecipeTarget(woodItems.button, 1, CraftingRecipe.newShapedRecipe(woodItems.prefix + "_button", new ItemTarget[]{new ItemTarget(woodItems.planks, 1), null, null, null}, 1))));
            craftGrid("stone_pressure_plate", Items.STONE_PRESSURE_PLATE, 1, new Grid2x2<>(o, o, "stone", "stone"));
            craftGrid("stone_button", Items.STONE_BUTTON, 1, new Grid2x2<>("stone", o, o, o));
            craftGrid("polished_blackstone_pressure_plate", Items.POLISHED_BLACKSTONE_PRESSURE_PLATE, 1, new Grid2x2<>(o, o, "polished_blackstone", "polished_blackstone"));
            craftGrid("polished_blackstone_button", Items.POLISHED_BLACKSTONE_BUTTON, 1, new Grid2x2<>("polished_blackstone", o, o, o));

            // SIGNS
            custom("sign", ItemHelper.WOOD_SIGN, CollectSignTask::new).dontMineIfPresent(); // By default, we save signs round these parts.
            woodTasks("sign", woodItems -> woodItems.sign, (woodItems, count) -> new CollectSignTask(woodItems.sign, woodItems.prefix + "_planks", count));

            //#if MC>=12000
            custom("hanging_sign", ItemHelper.WOOD_HANGING_SIGN, CollectHangingSignTask::new).dontMineIfPresent();
            woodTasks("hanging_sign", woodItems -> woodItems.hangingSign, (woodItems, count) -> new CollectHangingSignTask(woodItems.hangingSign, woodItems.prefix + "_stripped_logs", count));
            //#endif

            // FURNITURE

            //craftTmpl("bricks", Items.BRICKS, Templates.Block2x2("brick"));
            craftTmpl("crafting_table", Items.CRAFTING_TABLE, Templates.Block2x2(plk)).dontMineIfPresent();
            craftGrid("smithing_table", Items.SMITHING_TABLE, 1, new Grid3x3<>(irn, irn, o, plk, plk, o, plk, plk, o));
            craftGrid("grindstone", Items.GRINDSTONE, 1, new Grid3x3<>(stk, "stone_slab", stk, plk, o, plk, o, o, o));
            craftGrid("furnace", Items.FURNACE, 1, new Grid3x3<>(cbl, cbl, cbl, cbl, o, cbl, cbl, cbl, cbl)).dontMineIfPresent();
            craftGrid("dropper", Items.DROPPER, 1, new Grid3x3<>(cbl, cbl, cbl, cbl, o, cbl, cbl, "redstone", cbl));
            craftGrid("dispenser", Items.DISPENSER, 1, new Grid3x3<>(cbl, cbl, cbl, cbl, "bow", cbl, cbl, "redstone", cbl));
            craftGrid("brewing_stand", Items.BREWING_STAND, 1, new Grid3x3<>(o, o, o, o, "blaze_rod", o, cbl, cbl, cbl));
            craftGrid("piston", Items.PISTON, 1, new Grid3x3<>(plk, plk, plk, cbl, irn, cbl, cbl, "redstone", cbl));
            craftGrid("observer", Items.OBSERVER, 1, new Grid3x3<>(cbl, cbl, cbl, "redstone", "redstone", "quartz", cbl, cbl, cbl));
            craftGrid("lever", Items.LEVER, 1, new Grid2x2<>(stk, o, cbl, o));

            craftGrid("chest", Items.CHEST, 1, new Grid3x3<>(plk, plk, plk, plk, o, plk, plk, plk, plk)).dontMineIfPresent();
            craftGrid("torch", Items.TORCH, 4, new Grid2x2<>("coal", o, stk, o));
            custom("bed", ItemHelper.BED, CollectBedTask::new);
            colorfulTasks("bed", colors -> colors.bed, (colors, count) -> new CollectBedTask(colors.bed, colors.colorName + "_wool", count));
            craftGrid("anvil", Items.ANVIL, 1, new Grid3x3<>("iron_block", "iron_block", "iron_block", o, irn, o, irn, irn, irn));
            craftGrid("cauldron", Items.CAULDRON, 1, new Grid3x3<>(irn, o, irn, irn, o, irn, irn, irn, irn));
            craftGrid("minecart", Items.MINECART, 1, new Grid3x3<>(o, o, o, irn, o, irn, irn, irn, irn));
            craftGrid("iron_door", Items.IRON_DOOR, 3, new Grid3x3<>(irn, irn, o, irn, irn, o, irn, irn, o));
            craftGrid("iron_bars", Items.IRON_BARS, 16, new Grid3x3<>(irn, irn, irn, irn, irn, irn, o, o, o));
            craftGrid("blast_furnace", Items.BLAST_FURNACE, 1, new Grid3x3<>(irn, irn, irn, irn, "furnace", irn, "smooth_stone", "smooth_stone", "smooth_stone"));
            craftTmpl("iron_trapdoor", Items.IRON_TRAPDOOR, Templates.Block2x2(irn));
            craftGrid("armor_stand", Items.ARMOR_STAND, 1, new Grid3x3<>(stk, stk, stk, o, stk, o, stk, "smooth_stone_slab", stk));
            craftGrid("enchanting_table", Items.ENCHANTING_TABLE, 1, new Grid3x3<>(o, "book", o, "diamond", "obsidian", "diamond", "obsidian", "obsidian", "obsidian"));
            craftGrid("ender_chest", Items.ENDER_CHEST, 1, new Grid3x3<>("obsidian", "obsidian", "obsidian", "obsidian", "ender_eye", "obsidian", "obsidian", "obsidian", "obsidian")).dontMineIfPresent();

            craftGrid("flower_pot", Items.FLOWER_POT, 1, new Grid3x3<>("brick", o, "brick", o, "brick", o, o, o, o));
            //#if MC>=12000
            craftGrid("decorated_pot", Items.DECORATED_POT, 1, new Grid3x3<>(o, "brick", o, "brick", o, "brick", o, "brick", o));
            //#endif

            craftGrid("ladder", Items.LADDER, 3, new Grid3x3<>(stk, o, stk, stk, stk, stk, stk, o, stk));
            craftGrid("jukebox", Items.JUKEBOX, 1, new Grid3x3<>(plk, plk, plk, plk, "diamond", plk, plk, plk, plk));
            craftGrid("note_block", Items.NOTE_BLOCK, 1, new Grid3x3<>(plk, plk, plk, plk, "redstone", plk, plk, plk, plk));
            craftGrid("redstone_lamp", Items.REDSTONE_LAMP, 1, new Grid3x3<>(o, "redstone", o, "redstone", "glowstone", "redstone", o, "redstone", o));
            craftGrid("bookshelf", Items.BOOKSHELF, 1, new Grid3x3<>(plk, plk, plk, "book", "book", "book", plk, plk, plk));
            craftGrid("loom", Items.LOOM, 1, new Grid2x2<>("string", "string", plk, plk));

            craftGrid("glass_pane", Items.GLASS_PANE, 16, new Grid3x3<>("glass", "glass", "glass", "glass", "glass", "glass", o, o, o)).dontMineIfPresent();

            custom("carved_pumpkin", Items.CARVED_PUMPKIN, count -> new CarveThenCollectTask(Items.CARVED_PUMPKIN, count, Blocks.CARVED_PUMPKIN, Items.PUMPKIN, Blocks.PUMPKIN, Items.SHEARS));
            craftGrid("jack_o_lantern", Items.JACK_O_LANTERN, 1, new Grid2x2<>("carved_pumpkin", o, "torch", o));
            craftGrid("target", Items.TARGET, 1, new Grid3x3<>(o, "redstone", o, "redstone", "hay_block", "redstone", o, "redstone", o));
            craftGrid("campfire", Items.CAMPFIRE, 1, new Grid3x3<>(o, stk, o, stk, "coal", stk, "log", "log", "log"));
            craftGrid("soul_campfire", Items.SOUL_CAMPFIRE, 1, new Grid3x3<>(o, stk, o, stk, "soul_soil", stk, "log", "log", "log"));
            craftGrid("soul_torch", Items.SOUL_TORCH, 4, new Grid3x3<>(o, "coal", o, o, stk, o, o, "soul_soil", o));
            craftGrid("smoker", Items.SMOKER, 1, new Grid3x3<>(o, "log", o, "log", "furnace", "log", o, "log", o));

            craftGrid("lantern", Items.LANTERN, 1, new Grid3x3<>(inn, inn, inn, inn, "torch", inn, inn, inn, inn));
            craftGrid("soul_lantern", Items.SOUL_LANTERN, 1, new Grid3x3<>(inn, inn, inn, inn, "soul_torch", inn, inn, inn, inn));
            craftGrid("chain", Items.CHAIN, 1, new Grid3x3<>(o, inn, o, o, irn, o, o, inn, o));

            craftGrid("lodestone", Items.LODESTONE, 1, new Grid3x3<>("chiseled_stone_bricks", "chiseled_stone_bricks", "chiseled_stone_bricks", "chiseled_stone_bricks", "netherite_ingot", "chiseled_stone_bricks", "chiseled_stone_bricks", "chiseled_stone_bricks", "chiseled_stone_bricks"));

            //#if MC>=11800
            craftGrid("lightning_rod", Items.LIGHTNING_ROD, 1, new Grid3x3<>(o, "copper_ingot", o, o, "copper_ingot", o, o, "copper_ingot", o));
            craftGrid("tinted_glass", Items.TINTED_GLASS, 2, new Grid3x3<>(o, "amethyst_shard", o, "amethyst_shard", "glass", "amethyst_shard", o, "amethyst_shard", o));
            //#endif

            // A BUNCH OF WOODEN STUFF
            custom("wooden_stairs", ItemHelper.WOOD_STAIRS, CollectWoodenStairsTask::new);
            woodTasks("stairs", woodItems -> woodItems.stairs, (woodItems, count) -> new CollectWoodenStairsTask(woodItems.stairs, woodItems.prefix + "_planks", count));
            custom("wooden_slab", ItemHelper.WOOD_SLAB, CollectWoodenSlabTask::new);
            woodTasks("slab", woodItems -> woodItems.slab, (woodItems, count) -> new CollectWoodenSlabTask(woodItems.slab, woodItems.prefix + "_planks", count));
            custom("wooden_door", ItemHelper.WOOD_DOOR, CollectWoodenDoorTask::new);
            woodTasks("door", woodItems -> woodItems.door, (woodItems, count) -> new CollectWoodenDoorTask(woodItems.door, woodItems.prefix + "_planks", count));
            custom("wooden_trapdoor", ItemHelper.WOOD_TRAPDOOR, CollectWoodenTrapDoorTask::new);
            woodTasks("trapdoor", woodItems -> woodItems.trapdoor, (woodItems, count) -> new CollectWoodenTrapDoorTask(woodItems.trapdoor, woodItems.prefix + "_planks", count));
            custom("wooden_fence", ItemHelper.WOOD_FENCE, CollectFenceTask::new);
            woodTasks("fence", woodItems -> woodItems.fence, (woodItems, count) -> new CollectFenceTask(woodItems.fence, woodItems.prefix + "_planks", count));
            custom("wooden_fence_gate", ItemHelper.WOOD_FENCE_GATE, CollectFenceGateTask::new);
            woodTasks("fence_gate", woodItems -> woodItems.fenceGate, (woodItems, count) -> new CollectFenceGateTask(woodItems.fenceGate, woodItems.prefix + "_planks", count));


            //#if MC>=12000
            craftGrid("chiseled_bookshelf", Items.CHISELED_BOOKSHELF, 1, new Grid3x3<>(plk, plk, plk, "wooden_slab", "wooden_slab", "wooden_slab", plk, plk, plk)).dontMineIfPresent();
            //#endif

            craftGrid("barrel", Items.BARREL, 1, new Grid3x3<>(plk, "wooden_slab", plk, plk, o, plk, plk, "wooden_slab", plk));
            craftGrid("cartography_table", Items.CARTOGRAPHY_TABLE, 1, new Grid3x3<>("paper", "paper", o, plk, plk, o, plk, plk, o));
            craftGrid("composter", Items.COMPOSTER, 1, new Grid3x3<>("wooden_slab", o, "wooden_slab", "wooden_slab", o, "wooden_slab", "wooden_slab", "wooden_slab", "wooden_slab"));
            craftGrid("fletching_table", Items.FLETCHING_TABLE, 1, new Grid3x3<>("flint", "flint", o, plk, plk, o, plk, plk, o));
            craftGrid("lectern", Items.LECTERN, 1, new Grid3x3<>("wooden_slab", "wooden_slab", "wooden_slab", o, "bookshelf", o, o, "wooden_slab", o));

            alias("door", "wooden_door");
            alias("trapdoor", "wooden_trapdoor");
            alias("fence", "wooden_fence");
            alias("fence_gate", "wooden_fence_gate");

            craftGrid("heavy_weighted_pressure_plate", Items.HEAVY_WEIGHTED_PRESSURE_PLATE, 1, new Grid2x2<>(irn, irn, o, o));
            craftGrid("light_weighted_pressure_plate", Items.LIGHT_WEIGHTED_PRESSURE_PLATE, 1, new Grid2x2<>(gld, gld, o, o));

            craftGrid("daylight_detector", Items.DAYLIGHT_DETECTOR, 1, new Grid3x3<>("glass", "glass", "glass", "quartz", "quartz", "quartz", "wooden_slab", "wooden_slab", "wooden_slab"));
            craftGrid("tripwire_hook", Items.TRIPWIRE_HOOK, 2, new Grid3x3<>(irn, o, o, "stick", o, o, "planks", o, o));
            craftGrid("trapped_chest", Items.TRAPPED_CHEST, 1, new Grid2x2<>("chest", "tripwire_hook", o, o));
            craftGrid("crossbow", Items.CROSSBOW, 1, new Grid3x3<>(stk, irn, stk, "string", "tripwire_hook", "string", o, stk, o));
            craftGrid("tnt", Items.TNT, 1, new Grid3x3<>("gunpowder", "sand", "gunpowder", "sand", "gunpowder", "sand", "gunpowder", "sand", "gunpowder"));
            craftGrid("sticky_piston", Items.STICKY_PISTON, 1, new Grid2x2<>("slime_ball", o, "piston", o));
            craftGrid("redstone_torch", Items.REDSTONE_TORCH, 1, new Grid2x2<>("redstone", o, stk, o));
            craftGrid("repeater", Items.REPEATER, 1, new Grid3x3<>("redstone_torch", "redstone", "redstone_torch", "stone", "stone", "stone", o, o, o));
            craftGrid("comparator", Items.COMPARATOR, 1, new Grid3x3<>(o, "redstone_torch", o, "redstone_torch", "quartz", "redstone_torch", "stone", "stone", "stone"));
            craftGrid("rail", Items.RAIL, 16, new Grid3x3<>(irn, o, irn, irn, stk, irn, irn, o, irn));
            craftGrid("powered_rail", Items.POWERED_RAIL, 6, new Grid3x3<>(gld, o, gld, gld, stk, gld, gld, "redstone", gld));
            craftGrid("detector_rail", Items.DETECTOR_RAIL, 6, new Grid3x3<>(irn, o, irn, irn, "stone_pressure_plate", irn, irn, "redstone", irn));
            craftGrid("activator_rail", Items.ACTIVATOR_RAIL, 6, new Grid3x3<>(irn, stk, irn, irn, "redstone_torch", irn, irn, stk, irn));
            craftGrid("hopper", Items.HOPPER, 1, new Grid3x3<>(irn, o, irn, irn, "chest", irn, o, irn, o));
            craftGrid("painting", Items.PAINTING, 1, new Grid3x3<>(stk, stk, stk, stk, "wool", stk, stk, stk, stk));
            craftGrid("item_frame", Items.ITEM_FRAME, 1, new Grid3x3<>(stk, stk, stk, stk, "leather", stk, stk, stk, stk));
            craftGrid("chest_minecart", Items.CHEST_MINECART, 1, new Grid2x2<>("chest", o, "minecart", o));
            craftGrid("furnace_minecart", Items.FURNACE_MINECART, 1, new Grid2x2<>("furnace", o, "minecart", o));
            craftGrid("hopper_minecart", Items.HOPPER_MINECART, 1, new Grid2x2<>("hopper", o, "minecart", o));
            craftGrid("tnt_minecart", Items.TNT_MINECART, 1, new Grid2x2<>("tnt", o, "minecart", o));
            alias("minecart_with_chest", "chest_minecart");
            alias("minecart_with_furnace", "furnace_minecart");
            alias("minecart_with_hopper", "hopper_minecart");
            alias("minecart_with_tnt", "tnt_minecart");

            //#if MC>=11800
            craftGrid("glow_item_frame", Items.GLOW_ITEM_FRAME, 1, new Grid2x2<>("item_frame", "glow_ink_sac", o, o));
            //#endif


            /// FOOD
            mobCook("porkchop", Items.PORKCHOP, Items.COOKED_PORKCHOP, PigEntity.class);
            mobCook("beef", Items.BEEF, Items.COOKED_BEEF, CowEntity.class);
            mobCook("chicken", Items.CHICKEN, Items.COOKED_CHICKEN, ChickenEntity.class);
            mobCook("mutton", Items.MUTTON, Items.COOKED_MUTTON, SheepEntity.class);
            mobCook("rabbit", Items.RABBIT, Items.COOKED_RABBIT, RabbitEntity.class);
            mobCook("salmon", Items.SALMON, Items.COOKED_SALMON, SalmonEntity.class);
            mobCook("cod", Items.COD, Items.COOKED_COD, CodEntity.class);
            custom("milk", Items.MILK_BUCKET, CollectMilkTask::new);
            mine("apple", Blocks.OAK_LEAVES, Items.APPLE);
            smelt("baked_potato", Items.BAKED_POTATO, "potato");
            craftGrid("mushroom_stew", Items.MUSHROOM_STEW, 1, new Grid2x2<>("red_mushroom", "brown_mushroom", "bowl", o));
            craftGrid("suspicious_stew", Items.SUSPICIOUS_STEW, 1, new Grid2x2<>("red_mushroom", "brown_mushroom", "bowl", "flower"));
            craftGrid("bread", Items.BREAD, 1, new Grid3x3<>("wheat", "wheat", "wheat", o, o, o, o, o, o));
            craftGrid("cookie", Items.COOKIE, 8, new Grid3x3<>("wheat", "cocoa_beans", "wheat", o, o, o, o, o, o));
            craftGrid("pumpkin_pie", Items.PUMPKIN_PIE, 1, new Grid2x2<>("pumpkin", "sugar", o, "egg"));
            craftGrid("cake", Items.CAKE, 1, new Grid3x3<>("milk", "milk", "milk", "sugar", "egg", "sugar", "wheat", "wheat", "wheat")).dontMineIfPresent();
            craftGrid("golden_carrot", Items.GOLDEN_CARROT, 1, new Grid3x3<>(gln, gln, gln, gln, "carrot", gln, gln, gln, gln));
            craftGrid("golden_apple", Items.GOLDEN_APPLE, 1, new Grid3x3<>(gld, gld, gld, gld, "apple", gld, gld, gld, gld));

            craftGrid("rabbit_stew", Items.RABBIT_STEW, 1, new Grid3x3<>(o, "cooked_rabbit", o, "carrot", "baked_potato", "mushroom", o, "bowl", o));
            craftGrid("beetroot_soup", Items.BEETROOT_SOUP, 1, new Grid3x3<>("beetroot", "beetroot", "beetroot", "beetroot", "beetroot", "beetroot", o, "bowl", o));
        }

        //Debug.logInternal("All items: " + Arrays.toString(itemToResourceTask.keySet().toArray(Item[]::new)));
        Debug.logInternal("Successfully indexed " + itemToResourceTask.size() + " recipes!");
    }

    private static CataloguedResource put(String name, Item[] matches, Function<Integer, ResourceTask> getTask) {
        CataloguedResource result = new CataloguedResource(matches, getTask);
        Block[] blocks = ItemHelper.itemsToBlocks(matches);
        // DEFAULT BEHAVIOUR: Mine if present & assume overworld is required!
        if (blocks.length != 0) {
            result.mineIfPresent();
        }

        result.forceDimension(Dimension.OVERWORLD);
        if (nameToResourceTask.containsKey(name)) {
            throw new IllegalStateException("Tried cataloguing " + name + " twice!");
        }
        nameToResourceTask.put(name, result);
        nameToItemMatches.put(name, matches);
        resourcesObtainable.addAll(Arrays.asList(matches));

        // If this resource is just one item, consider it collectable.
        if (matches.length == 1) {
            if (itemToResourceTask.containsKey(matches[0])) {
                throw new IllegalStateException("Tried cataloguing " + matches[0].getTranslationKey() + " twice!");
            }
            itemToResourceTask.put(matches[0], result);
        }

        return result;
    }

    // This is here so that we can use strings for item targets (optionally) and stuff like that.
    public static Item[] getItemMatches(String name) {
        if (!nameToItemMatches.containsKey(name)) {
            return new Item[0];
        }
        return nameToItemMatches.get(name);
    }

    public static boolean isObtainable(Item item) {
        return resourcesObtainable.contains(item);
    }

    public static ItemTarget getItemTarget(String name, int count) {
        return new ItemTarget(name, count);
    }

    public static CataloguedResourceTask getSquashedItemTask(ItemTarget... targets) {
        return new CataloguedResourceTask(true, targets);
    }

    public static ResourceTask getItemTask(String name, int count) {

        if (!taskExists(name)) {
            Debug.logWarning("Task " + name + " does not exist. Error possibly.");
            Debug.logStack();
            return null;
        }

        return nameToResourceTask.get(name).getResource(count);
    }

    public static ResourceTask getItemTask(Item item, int count) {
        if (!taskExists(item)) {
            Debug.logWarning("Task " + item + " does not exist. Error possibly.");
            Debug.logStack();
            return null;
        }

        return itemToResourceTask.get(item).getResource(count);
    }

    public static ResourceTask getItemTask(ItemTarget target) {
        if (target.isCatalogueItem()) {
            return getItemTask(target.getCatalogueName(), target.getTargetCount());
        } else if (target.getMatches().length == 1) {
            return getItemTask(target.getMatches()[0], target.getTargetCount());
        } else {
            return getSquashedItemTask(target);
        }
    }

    public static boolean taskExists(String name) {
        return nameToResourceTask.containsKey(name);
    }

    public static boolean taskExists(Item item) {
        return itemToResourceTask.containsKey(item);
    }

    public static Collection<String> resourceNames() {
        return nameToResourceTask.keySet();
    }


    private static CataloguedResource custom(String name, Item[] matches, Function<Integer, ResourceTask> getTask) {
        return put(name, matches, getTask);
    }

    private static CataloguedResource custom(String name, Item matches, Function<Integer, ResourceTask> getTask) {
        return custom(name, new Item[]{matches}, getTask);
    }

    private static CataloguedResource mine(String name, MiningRequirement requirement, Item[] toMine, Item... targets) {
        Block[] toMineBlocks = new Block[toMine.length];
        for (int i = 0; i < toMine.length; ++i) toMineBlocks[i] = Block.getBlockFromItem(toMine[i]);
        return mine(name, requirement, toMineBlocks, targets);
    }

    private static CataloguedResource mine(String name, MiningRequirement requirement, Block[] toMine, Item... targets) {
        return put(name, targets, count -> new MineAndCollectTask(new ItemTarget(targets, count), toMine, requirement)).dontMineIfPresent(); // Mining already taken care of!!
    }

    private static CataloguedResource mine(String name, MiningRequirement requirement, Block toMine, Item target) {
        return mine(name, requirement, new Block[]{toMine}, target);
    }

    private static CataloguedResource mine(String name, Block toMine, Item target) {
        return mine(name, MiningRequirement.HAND, toMine, target);
    }

    private static CataloguedResource mine(String name, Item target) {
        return mine(name, Block.getBlockFromItem(target), target);
    }

    private static CataloguedResource shear(String name, Block[] toShear, Item... targets) {
        return put(name, targets, count -> new ShearAndCollectBlockTask(new ItemTarget[]{new ItemTarget(targets, count)}, toShear)).dontMineIfPresent();
    }

    private static CataloguedResource shear(String name, Block toShear, Item... targets) {
        return shear(name, new Block[]{toShear}, targets);
    }

    // NOTICE: Template in craftTemplate is **NOT** a smithing template.
    private static CataloguedResource craftTmpl(String name, Item match, RecipeTemplate recipeTemplate) {
        return craftTmpl(name, match, recipeTemplate, recipeTemplate.outputCount);
    }

    private static CataloguedResource craftTmpl(String name, Item match, RecipeTemplate recipeTemplate, int outputCount) {
        return craftGrid(name, match, outputCount, recipeTemplate.gridShape);
    }

    private static CataloguedResource craftGrid(String name, Item match, int outputCount, IGrid<String> craftingGrid) {
        ItemTarget[] craftingSlots =
                Arrays.stream(craftingGrid.listContent(String[]::new))
                        .map(TaskCatalogue::target)
                        .toArray(ItemTarget[]::new);

        CraftingRecipe recipe = CraftingRecipe.newShapedRecipe(name, craftingSlots, outputCount);
        Function<Integer, ResourceTask> craftingTaskFunction = craftingGrid instanceof Grid3x3<String>
                ? (count) -> new CraftInTableTask(new RecipeTarget(match, count, recipe)) // 3x3 Grid - Crafting table
                : (count) -> new CraftInInventoryTask(new RecipeTarget(match, count, recipe)); // 2x2 Grid - Inventory

        return put(name, new Item[]{match}, craftingTaskFunction);
    }

    /*
    private static CataloguedResource shapedRecipe3x3(String name, Item match, int outputCount, String s0, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8) {
        CraftingRecipe recipe = CraftingRecipe.newShapedRecipe(name, new ItemTarget[]{t(s0), t(s1), t(s2), t(s3), t(s4), t(s5), t(s6), t(s7), t(s8)}, outputCount);
        return put(name, new Item[]{match}, count -> new CraftInTableTask(new RecipeTarget(match, count, recipe)));
    }
     */

    private static CataloguedResource smelt(String name, Item[] matches, String materials, Item... optionalMaterials) {
        return put(name, matches, count -> new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(matches, count), new ItemTarget(materials, count), optionalMaterials)));
    }
    private static CataloguedResource smelt(String name, Item match, String materials, Item... optionalMaterials) {
        return smelt(name, new Item[]{match}, materials, optionalMaterials);
    }

    private static CataloguedResource smith(String name, Item[] matches, String materials, String tool) {
        return put(name, matches, count -> new UpgradeInSmithingTableTask(new ItemTarget(tool, count), new ItemTarget(materials, count), new ItemTarget(matches, count)));//new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(matches, count), new ItemTarget(materials, count))));
    }
    private static CataloguedResource smith(String name, Item match, String materials, String tool) {
        return smith(name, new Item[]{match}, materials, tool);
    }

    private static CataloguedResource mobDrop(String name, Item[] matches, Class mobClass) {
        return put(name, matches, count -> new KillAndLootTask(mobClass, new ItemTarget(matches, count)));
    }
    private static CataloguedResource mobDrop(String name, Item match, Class mobClass) {
        return mobDrop(name, new Item[]{match}, mobClass);
    }

    private static void mobCook(String uncookedName, String cookedName, Item uncooked, Item cooked, Class mobClass) {
        mobDrop(uncookedName, uncooked, mobClass);
        smelt(cookedName, cooked, uncookedName);
    }
    private static void mobCook(String uncookedName, Item uncooked, Item cooked, Class mobClass) {
        mobCook(uncookedName, "cooked_" + uncookedName, uncooked, cooked, mobClass);
    }

    private static CataloguedResource harvest(String name, Item[] matches, Block[] cropBlocks, Item[] cropSeeds) {
        return put(name, matches, count -> new CollectCropTask(new ItemTarget(matches, count), cropBlocks, cropSeeds));
    }

    public static CataloguedResource harvest(String name, Item match, Block cropBlock, Item cropSeed) {
        return harvest(name, new Item[]{match}, new Block[]{cropBlock}, new Item[]{cropSeed});
    }

    private static void colorfulTasks(String baseName, Function<ItemHelper.ColorItems, Item> getMatch, BiFunction<ItemHelper.ColorItems, Integer, ResourceTask> getTask) {
        for (DyeColor dCol : DyeColor.values()) {
            MapColor mCol = dCol.getMapColor();
            ItemHelper.ColorItems color = ItemHelper.colorMap.get(mCol);
            String prefix = color.colorName;
            put(prefix + "_" + baseName, new Item[]{getMatch.apply(color)}, count -> getTask.apply(color, count));
        }
    }

    private static CataloguedResource[] woodTasks(Function<ItemHelper.WoodItems, String> getCatalogueName, Function<ItemHelper.WoodItems, Item> getMatch, BiFunction<ItemHelper.WoodItems, Integer, ResourceTask> getTask, boolean requireNetherForNetherStuff) {
        List<CataloguedResource> result = new ArrayList<>();
        for (WoodType woodType : WoodType.values()) {
            ItemHelper.WoodItems woodItems = ItemHelper.woodMap.get(woodType);
            Item match = getMatch.apply(woodItems);
            String cataloguedName = getCatalogueName.apply(woodItems);
            if (match == null) continue;
            boolean isNether = woodItems.isNetherWood();
            CataloguedResource t = put(cataloguedName, new Item[]{match}, count -> getTask.apply(woodItems, count));
            if (requireNetherForNetherStuff && isNether) {
                t.forceDimension(Dimension.NETHER);
            }
            result.add(t);
        }
        return result.toArray(CataloguedResource[]::new);
    }

    private static CataloguedResource[] woodTasks(String baseName, Function<ItemHelper.WoodItems, Item> getMatch, BiFunction<ItemHelper.WoodItems, Integer, ResourceTask> getTask, boolean requireNetherForNetherStuff) {
        return woodTasks(woodItem -> woodItem.prefix + "_" + baseName, getMatch, getTask, requireNetherForNetherStuff);
    }

    private static CataloguedResource[] woodTasks(String baseName, Function<ItemHelper.WoodItems, Item> getMatch, BiFunction<ItemHelper.WoodItems, Integer, ResourceTask> getTask) {
        return woodTasks(baseName, getMatch, getTask, false);
    }

    private static void tools(String toolMaterialName, String material, Item pickaxeItem, Item shovelItem, Item swordItem, Item axeItem, Item hoeItem) {
        craftGrid(toolMaterialName + "_pickaxe", pickaxeItem, 1, new Grid3x3<>(material, material, material, o, "stick", o, o, "stick", o));
        craftGrid(toolMaterialName + "_shovel", shovelItem, 1, new Grid3x3<>(o, material, o, o, "stick", o, o, "stick", o));
        craftGrid(toolMaterialName + "_sword", swordItem, 1, new Grid3x3<>(o, material, o, o, material, o, o, "stick", o));
        craftGrid(toolMaterialName + "_axe", axeItem, 1, new Grid3x3<>(material, material, o, material, "stick", o, o, "stick", o));
        craftGrid(toolMaterialName + "_hoe", hoeItem, 1, new Grid3x3<>(material, material, o, o, "stick", o, o, "stick", o));
    }

    private static void armor(String armorMaterialName, String material, Item helmetItem, Item chestplateItem, Item leggingsItem, Item bootsItem) {
        craftGrid(armorMaterialName + "_helmet", helmetItem, 1, new Grid3x3<>(material, material, material, material, o, material, o, o, o));
        craftGrid(armorMaterialName + "_chestplate", chestplateItem, 1, new Grid3x3<>(material, o, material, material, material, material, material, material, material));
        craftGrid(armorMaterialName + "_leggings", leggingsItem, 1, new Grid3x3<>(material, material, material, material, o, material, material, o, material));
        craftGrid(armorMaterialName + "_boots", bootsItem, 1, new Grid3x3<>(o, o, o, material, o, material, material, o, material));
    }

    private static void alias(String newName, String original) {
        if (!nameToResourceTask.containsKey(original) || !nameToItemMatches.containsKey(original)) {
            Debug.logWarning("Invalid resource: " + original + ". Will not create alias.");
        } else {
            nameToResourceTask.put(newName, nameToResourceTask.get(original));
            nameToItemMatches.put(newName, nameToItemMatches.get(original));
        }
    }

    private static ItemTarget target(String cataloguedName) {
        return new ItemTarget(cataloguedName);
    }

    private static class CataloguedResource {
        private final Item[] targets;
        private final Function<Integer, ResourceTask> getResource;

        private boolean mineIfPresent;
        private boolean forceDimension = false;
        private Dimension targetDimension;

        public CataloguedResource(Item[] targets, Function<Integer, ResourceTask> getResource) {
            this.targets = targets;
            this.getResource = getResource;
        }

        public CataloguedResource mineIfPresent() {
            mineIfPresent = true;
            return this;
        }

        public CataloguedResource dontMineIfPresent() {
            mineIfPresent = false;
            return this;
        }

        public CataloguedResource forceDimension(Dimension dimension) {
            forceDimension = true;
            targetDimension = dimension;
            return this;
        }

        public CataloguedResource anyDimension() {
            forceDimension = false;
            return this;
        }

        public ResourceTask getResource(int count) {
            ResourceTask result = getResource.apply(count);
            if (mineIfPresent) {
                result = result.mineIfPresent(ItemHelper.itemsToBlocks(targets));
            }
            if (forceDimension) {
                result = result.forceDimension(targetDimension);
            }
            return result;
        }
    }
}
