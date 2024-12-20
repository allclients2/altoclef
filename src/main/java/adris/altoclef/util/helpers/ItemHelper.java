package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.ItemVer;
import adris.altoclef.util.publicenums.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.publicenums.MiningRequirement;
import adris.altoclef.util.publicenums.OreType;
import adris.altoclef.util.publicenums.WoodType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;


import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

/**
 * Helper functions and definitions for useful groupings of items
 */
public class ItemHelper {

    // TODO: add NETHER_GOLD_ORE, and netherite, which have to be crafted to get their raw drop..
    // Ore Distribution sources: https://www.reddit.com/r/Minecraft/comments/rej0ch/best_y_levels_for_each_ore_in_118_validated_by/, https://www.gamertweak.com/minecraft-1-18-ore-distribution/, https://www.planetminecraft.com/blog/a-good-enough-guide-to-ore-distribution-in-minecraft-1-18/
    public static final Map<OreType, MaterialData> MATERIAL_DATA = new HashMap<>() {
        {
            put(OreType.COAL, new MaterialData(
                    new OreBlockData[]{
                            new OreBlockData(Blocks.COAL_ORE, new OreDistribution(256, 95, 0)),
                            //#if MC >= 11800
                            new OreBlockData(Blocks.DEEPSLATE_COAL_ORE, new OreDistribution(0, -8, -64))
                            //#endif
                    },
                    MiningRequirement.WOOD, Items.COAL, Blocks.COAL_BLOCK
            ));
            put(OreType.IRON, new MaterialData(
                    new OreBlockData[]{
                            new OreBlockData(Blocks.IRON_ORE, new OreDistribution(256, 16, -64)),
                            //#if MC>=11800
                            new OreBlockData(Blocks.DEEPSLATE_IRON_ORE, new OreDistribution(16, 16, -64))
                            //#endif
                    }, MiningRequirement.STONE,
                    //#if MC>=11800
                    Items.RAW_IRON,
                    //#else
                    //$$ Items.IRON_ORE,
                    //#endif
                    Items.IRON_INGOT, IRON_ARMORS, IRON_TOOLS, Blocks.IRON_BLOCK
            ));
            put(OreType.GOLD, new MaterialData(
                    new OreBlockData[]{
                            new OreBlockData(Blocks.GOLD_ORE, new OreDistribution(256, -16, -64)),
                            //#if MC>=11800
                            new OreBlockData(Blocks.DEEPSLATE_GOLD_ORE, new OreDistribution(-16, -16, -64)),
                            //#endif
                            //FIXME: This drops gold nuggets! --> new OreBlockData(Blocks.NETHER_GOLD_ORE, new OreDistribution(256, 32, 0, Dimension.NETHER))
                    }, MiningRequirement.IRON,
                    //#if MC>=11800
                    Items.RAW_GOLD,
                    //#else
                    //$$ Items.GOLD_ORE,
                    //#endif
                    Items.GOLD_INGOT, GOLDEN_ARMORS, GOLDEN_TOOLS, Blocks.GOLD_BLOCK
            ));
            put(OreType.DIAMOND, new MaterialData(
                    new OreBlockData[]{
                            new OreBlockData(Blocks.DIAMOND_ORE, new OreDistribution(16, -59, -64)),
                            //#if MC>=11800
                            new OreBlockData(Blocks.DEEPSLATE_DIAMOND_ORE, new OreDistribution(-59, -59, -64))
                            //#endif
                    }, MiningRequirement.IRON, Items.DIAMOND, DIAMOND_ARMORS, DIAMOND_TOOLS, Blocks.DIAMOND_BLOCK
            ));
            put(OreType.REDSTONE, new MaterialData(
                    new OreBlockData[]{
                            new OreBlockData(Blocks.REDSTONE_ORE, new OreDistribution(16, -59, -64)),
                            //#if MC>=11800
                            new OreBlockData(Blocks.DEEPSLATE_REDSTONE_ORE, new OreDistribution(-59, -59, -64))
                            //#endif
                    }, MiningRequirement.IRON, Items.REDSTONE, Blocks.REDSTONE_BLOCK
            ));
            put(OreType.LAPIS_LAZULI, new MaterialData(
                    new OreBlockData[]{
                            new OreBlockData(Blocks.LAPIS_ORE, new OreDistribution(64, 0, -64)),
                            //#if MC>=11800
                            new OreBlockData(Blocks.DEEPSLATE_LAPIS_ORE, new OreDistribution(0, 0, -64))
                            //#endif
                    }, MiningRequirement.STONE, Items.LAPIS_LAZULI, Blocks.LAPIS_BLOCK
            ));
            put(OreType.QUARTZ, new MaterialData(
                    new OreBlockData[]{
                            new OreBlockData(Blocks.NETHER_QUARTZ_ORE, new OreDistribution(117, 20, 10, Dimension.NETHER))
                    }, MiningRequirement.WOOD, Items.QUARTZ, Blocks.QUARTZ_BLOCK
            ));
            put(OreType.EMERALD, new MaterialData( // TODO: Make this need the Mountain biome
                    new OreBlockData[]{
                            new OreBlockData(Blocks.EMERALD_ORE, new OreDistribution(256, 224, -16)),
                            //#if MC>=11800
                            new OreBlockData(Blocks.DEEPSLATE_EMERALD_ORE, new OreDistribution(224, 96, -23))
                            //#endif
                    }, MiningRequirement.IRON, Items.EMERALD, Blocks.EMERALD_BLOCK
            ));
            //#if MC>=11800
            put(OreType.COPPER, new MaterialData(
                    new OreBlockData[]{
                            new OreBlockData(Blocks.COPPER_ORE, new OreDistribution(112, 48, -16)),
                            new OreBlockData(Blocks.DEEPSLATE_COPPER_ORE, new OreDistribution(48, 48, -16))
                    }, MiningRequirement.STONE, Items.RAW_COPPER, Items.COPPER_INGOT, Blocks.COPPER_BLOCK
            ));
            //#endif
        }
    };

    public static Map<Item, OreType> dropToOreType = Collections.unmodifiableMap(new HashMap<>() {
        {
            MATERIAL_DATA.forEach((oreType, materialData) -> {
                put(materialData.rawItem, oreType);
            });
        }
    });


    // This is kinda jank ngl
    public static final Map<MapColor, ColorItems> colorMap = new HashMap<MapColor, ColorItems>() {
        {
            makeColorItems(DyeColor.RED, "red", Items.RED_DYE, Items.RED_WOOL, Items.RED_BED, Items.RED_CARPET, Items.RED_STAINED_GLASS, Items.RED_STAINED_GLASS_PANE, Items.RED_TERRACOTTA, Items.RED_GLAZED_TERRACOTTA, Items.RED_CONCRETE, Items.RED_CONCRETE_POWDER, Items.RED_BANNER, Items.RED_SHULKER_BOX, Blocks.RED_WALL_BANNER);
            makeColorItems(DyeColor.WHITE, "white", Items.WHITE_DYE, Items.WHITE_WOOL, Items.WHITE_BED, Items.WHITE_CARPET, Items.WHITE_STAINED_GLASS, Items.WHITE_STAINED_GLASS_PANE, Items.WHITE_TERRACOTTA, Items.WHITE_GLAZED_TERRACOTTA, Items.WHITE_CONCRETE, Items.WHITE_CONCRETE_POWDER, Items.WHITE_BANNER, Items.WHITE_SHULKER_BOX, Blocks.WHITE_WALL_BANNER);
            makeColorItems(DyeColor.BLACK, "black", Items.BLACK_DYE, Items.BLACK_WOOL, Items.BLACK_BED, Items.BLACK_CARPET, Items.BLACK_STAINED_GLASS, Items.BLACK_STAINED_GLASS_PANE, Items.BLACK_TERRACOTTA, Items.BLACK_GLAZED_TERRACOTTA, Items.BLACK_CONCRETE, Items.BLACK_CONCRETE_POWDER, Items.BLACK_BANNER, Items.BLACK_SHULKER_BOX, Blocks.BLACK_WALL_BANNER);
            makeColorItems(DyeColor.BLUE, "blue", Items.BLUE_DYE, Items.BLUE_WOOL, Items.BLUE_BED, Items.BLUE_CARPET, Items.BLUE_STAINED_GLASS, Items.BLUE_STAINED_GLASS_PANE, Items.BLUE_TERRACOTTA, Items.BLUE_GLAZED_TERRACOTTA, Items.BLUE_CONCRETE, Items.BLUE_CONCRETE_POWDER, Items.BLUE_BANNER, Items.BLUE_SHULKER_BOX, Blocks.BLUE_WALL_BANNER);
            makeColorItems(DyeColor.BROWN, "brown", Items.BROWN_DYE, Items.BROWN_WOOL, Items.BROWN_BED, Items.BROWN_CARPET, Items.BROWN_STAINED_GLASS, Items.BROWN_STAINED_GLASS_PANE, Items.BROWN_TERRACOTTA, Items.BROWN_GLAZED_TERRACOTTA, Items.BROWN_CONCRETE, Items.BROWN_CONCRETE_POWDER, Items.BROWN_BANNER, Items.BROWN_SHULKER_BOX, Blocks.BROWN_WALL_BANNER);
            makeColorItems(DyeColor.CYAN, "cyan", Items.CYAN_DYE, Items.CYAN_WOOL, Items.CYAN_BED, Items.CYAN_CARPET, Items.CYAN_STAINED_GLASS, Items.CYAN_STAINED_GLASS_PANE, Items.CYAN_TERRACOTTA, Items.CYAN_GLAZED_TERRACOTTA, Items.CYAN_CONCRETE, Items.CYAN_CONCRETE_POWDER, Items.CYAN_BANNER, Items.CYAN_SHULKER_BOX, Blocks.CYAN_WALL_BANNER);
            makeColorItems(DyeColor.GRAY, "gray", Items.GRAY_DYE, Items.GRAY_WOOL, Items.GRAY_BED, Items.GRAY_CARPET, Items.GRAY_STAINED_GLASS, Items.GRAY_STAINED_GLASS_PANE, Items.GRAY_TERRACOTTA, Items.GRAY_GLAZED_TERRACOTTA, Items.GRAY_CONCRETE, Items.GRAY_CONCRETE_POWDER, Items.GRAY_BANNER, Items.GRAY_SHULKER_BOX, Blocks.GRAY_WALL_BANNER);
            makeColorItems(DyeColor.GREEN, "green", Items.GREEN_DYE, Items.GREEN_WOOL, Items.GREEN_BED, Items.GREEN_CARPET, Items.GREEN_STAINED_GLASS, Items.GREEN_STAINED_GLASS_PANE, Items.GREEN_TERRACOTTA, Items.GREEN_GLAZED_TERRACOTTA, Items.GREEN_CONCRETE, Items.GREEN_CONCRETE_POWDER, Items.GREEN_BANNER, Items.GREEN_SHULKER_BOX, Blocks.GREEN_WALL_BANNER);
            makeColorItems(DyeColor.LIGHT_BLUE, "light_blue", Items.LIGHT_BLUE_DYE, Items.LIGHT_BLUE_WOOL, Items.LIGHT_BLUE_BED, Items.LIGHT_BLUE_CARPET, Items.LIGHT_BLUE_STAINED_GLASS, Items.LIGHT_BLUE_STAINED_GLASS_PANE, Items.LIGHT_BLUE_TERRACOTTA, Items.LIGHT_BLUE_GLAZED_TERRACOTTA, Items.LIGHT_BLUE_CONCRETE, Items.LIGHT_BLUE_CONCRETE_POWDER, Items.LIGHT_BLUE_BANNER, Items.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_BLUE_WALL_BANNER);
            makeColorItems(DyeColor.LIGHT_GRAY, "light_gray", Items.LIGHT_GRAY_DYE, Items.LIGHT_GRAY_WOOL, Items.LIGHT_GRAY_BED, Items.LIGHT_GRAY_CARPET, Items.LIGHT_GRAY_STAINED_GLASS, Items.LIGHT_GRAY_STAINED_GLASS_PANE, Items.LIGHT_GRAY_TERRACOTTA, Items.LIGHT_GRAY_GLAZED_TERRACOTTA, Items.LIGHT_GRAY_CONCRETE, Items.LIGHT_GRAY_CONCRETE_POWDER, Items.LIGHT_GRAY_BANNER, Items.LIGHT_GRAY_SHULKER_BOX, Blocks.LIGHT_GRAY_WALL_BANNER);
            makeColorItems(DyeColor.LIME, "lime", Items.LIME_DYE, Items.LIME_WOOL, Items.LIME_BED, Items.LIME_CARPET, Items.LIME_STAINED_GLASS, Items.LIME_STAINED_GLASS_PANE, Items.LIME_TERRACOTTA, Items.LIME_GLAZED_TERRACOTTA, Items.LIME_CONCRETE, Items.LIME_CONCRETE_POWDER, Items.LIME_BANNER, Items.LIME_SHULKER_BOX, Blocks.LIME_WALL_BANNER);
            makeColorItems(DyeColor.MAGENTA, "magenta", Items.MAGENTA_DYE, Items.MAGENTA_WOOL, Items.MAGENTA_BED, Items.MAGENTA_CARPET, Items.MAGENTA_STAINED_GLASS, Items.MAGENTA_STAINED_GLASS_PANE, Items.MAGENTA_TERRACOTTA, Items.MAGENTA_GLAZED_TERRACOTTA, Items.MAGENTA_CONCRETE, Items.MAGENTA_CONCRETE_POWDER, Items.MAGENTA_BANNER, Items.MAGENTA_SHULKER_BOX, Blocks.MAGENTA_WALL_BANNER);
            makeColorItems(DyeColor.ORANGE, "orange", Items.ORANGE_DYE, Items.ORANGE_WOOL, Items.ORANGE_BED, Items.ORANGE_CARPET, Items.ORANGE_STAINED_GLASS, Items.ORANGE_STAINED_GLASS_PANE, Items.ORANGE_TERRACOTTA, Items.ORANGE_GLAZED_TERRACOTTA, Items.ORANGE_CONCRETE, Items.ORANGE_CONCRETE_POWDER, Items.ORANGE_BANNER, Items.ORANGE_SHULKER_BOX, Blocks.ORANGE_WALL_BANNER);
            makeColorItems(DyeColor.PINK, "pink", Items.PINK_DYE, Items.PINK_WOOL, Items.PINK_BED, Items.PINK_CARPET, Items.PINK_STAINED_GLASS, Items.PINK_STAINED_GLASS_PANE, Items.PINK_TERRACOTTA, Items.PINK_GLAZED_TERRACOTTA, Items.PINK_CONCRETE, Items.PINK_CONCRETE_POWDER, Items.PINK_BANNER, Items.PINK_SHULKER_BOX, Blocks.PINK_WALL_BANNER);
            makeColorItems(DyeColor.PURPLE, "purple", Items.PURPLE_DYE, Items.PURPLE_WOOL, Items.PURPLE_BED, Items.PURPLE_CARPET, Items.PURPLE_STAINED_GLASS, Items.PURPLE_STAINED_GLASS_PANE, Items.PURPLE_TERRACOTTA, Items.PURPLE_GLAZED_TERRACOTTA, Items.PURPLE_CONCRETE, Items.PURPLE_CONCRETE_POWDER, Items.PURPLE_BANNER, Items.PURPLE_SHULKER_BOX, Blocks.PURPLE_WALL_BANNER);
            makeColorItems(DyeColor.RED, "red", Items.RED_DYE, Items.RED_WOOL, Items.RED_BED, Items.RED_CARPET, Items.RED_STAINED_GLASS, Items.RED_STAINED_GLASS_PANE, Items.RED_TERRACOTTA, Items.RED_GLAZED_TERRACOTTA, Items.RED_CONCRETE, Items.RED_CONCRETE_POWDER, Items.RED_BANNER, Items.RED_SHULKER_BOX, Blocks.RED_WALL_BANNER);
            makeColorItems(DyeColor.YELLOW, "yellow", Items.YELLOW_DYE, Items.YELLOW_WOOL, Items.YELLOW_BED, Items.YELLOW_CARPET, Items.YELLOW_STAINED_GLASS, Items.YELLOW_STAINED_GLASS_PANE, Items.YELLOW_TERRACOTTA, Items.YELLOW_GLAZED_TERRACOTTA, Items.YELLOW_CONCRETE, Items.YELLOW_CONCRETE_POWDER, Items.YELLOW_BANNER, Items.YELLOW_SHULKER_BOX, Blocks.YELLOW_WALL_BANNER);
        }

        void makeColorItems(DyeColor color, String colorName, Item dye, Item wool, Item bed, Item carpet, Item stainedGlass, Item stainedGlassPane, Item terracotta, Item glazedTerracotta, Item concrete, Item concretePowder, Item banner, Item shulker, Block wallBanner) {
            put(color.getMapColor(), new ColorItems(color, colorName, dye, wool, bed, carpet, stainedGlass, stainedGlassPane, terracotta, glazedTerracotta, concrete, concretePowder, banner, shulker, wallBanner));
        }
    };


    public static final Map<WoodType, WoodItems> woodMap = new HashMap<WoodType, WoodItems>() {
        {
            //TODO: Add all WALL_HANGING_SIGN for `WOOD_SIGNS_ALL`
            //#if MC >= 12000
            makeWoodClass(WoodType.MANGROVE, "mangrove", Items.MANGROVE_PLANKS, Items.MANGROVE_LOG, Items.STRIPPED_MANGROVE_LOG, Items.STRIPPED_MANGROVE_WOOD, Items.MANGROVE_WOOD, Items.MANGROVE_SIGN, Items.MANGROVE_DOOR, Items.MANGROVE_BUTTON, Items.MANGROVE_STAIRS, Items.MANGROVE_SLAB, Items.MANGROVE_FENCE, Items.MANGROVE_FENCE_GATE, Items.MANGROVE_BOAT, Items.MANGROVE_PROPAGULE, Items.MANGROVE_LEAVES, Items.MANGROVE_PRESSURE_PLATE, Items.MANGROVE_TRAPDOOR, Items.MANGROVE_HANGING_SIGN, Blocks.MANGROVE_SIGN, Blocks.MANGROVE_WALL_SIGN);
            makeWoodClass(WoodType.CHERRY, "cherry", Items.CHERRY_PLANKS, Items.CHERRY_LOG, Items.STRIPPED_CHERRY_LOG, Items.STRIPPED_CHERRY_WOOD, Items.CHERRY_WOOD, Items.CHERRY_SIGN, Items.CHERRY_DOOR, Items.CHERRY_BUTTON, Items.CHERRY_STAIRS, Items.CHERRY_SLAB, Items.CHERRY_FENCE, Items.CHERRY_FENCE_GATE, Items.CHERRY_BOAT, Items.CHERRY_SAPLING, Items.CHERRY_LEAVES, Items.CHERRY_PRESSURE_PLATE, Items.CHERRY_TRAPDOOR, Items.CHERRY_HANGING_SIGN, Blocks.CHERRY_SIGN, Blocks.CHERRY_WALL_SIGN);
            makeWoodClass(WoodType.BAMBOO, "bamboo", null, null, Items.STRIPPED_BAMBOO_BLOCK, null, null, Items.BAMBOO_SIGN, Items.BAMBOO_DOOR, Items.BAMBOO_BUTTON, Items.BAMBOO_STAIRS, Items.BAMBOO_SLAB, Items.BAMBOO_FENCE, Items.BAMBOO_FENCE_GATE, Items.BAMBOO_RAFT, Items.BAMBOO, null, Items.BAMBOO_PRESSURE_PLATE, Items.BAMBOO_TRAPDOOR, Items.BAMBOO_HANGING_SIGN, Blocks.BAMBOO_SIGN, Blocks.BAMBOO_WALL_SIGN);
            makeWoodClass(WoodType.ACACIA, "acacia", Items.ACACIA_PLANKS, Items.ACACIA_LOG, Items.STRIPPED_ACACIA_LOG, Items.STRIPPED_ACACIA_WOOD, Items.ACACIA_WOOD, Items.ACACIA_SIGN, Items.ACACIA_DOOR, Items.ACACIA_BUTTON, Items.ACACIA_STAIRS, Items.ACACIA_SLAB, Items.ACACIA_FENCE, Items.ACACIA_FENCE_GATE, Items.ACACIA_BOAT, Items.ACACIA_SAPLING, Items.ACACIA_LEAVES, Items.ACACIA_PRESSURE_PLATE, Items.ACACIA_TRAPDOOR, Items.ACACIA_HANGING_SIGN, Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN);
            makeWoodClass(WoodType.BIRCH, "birch", Items.BIRCH_PLANKS, Items.BIRCH_LOG, Items.STRIPPED_BIRCH_LOG, Items.STRIPPED_BIRCH_WOOD, Items.BIRCH_WOOD, Items.BIRCH_SIGN, Items.BIRCH_DOOR, Items.BIRCH_BUTTON, Items.BIRCH_STAIRS, Items.BIRCH_SLAB, Items.BIRCH_FENCE, Items.BIRCH_FENCE_GATE, Items.BIRCH_BOAT, Items.BIRCH_SAPLING, Items.BIRCH_LEAVES, Items.BIRCH_PRESSURE_PLATE, Items.BIRCH_TRAPDOOR, Items.BIRCH_HANGING_SIGN, Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN);
            makeWoodClass(WoodType.CRIMSON, "crimson", Items.CRIMSON_PLANKS, Items.CRIMSON_STEM, Items.STRIPPED_CRIMSON_STEM, Items.STRIPPED_CRIMSON_HYPHAE, Items.CRIMSON_HYPHAE, Items.CRIMSON_SIGN, Items.CRIMSON_DOOR, Items.CRIMSON_BUTTON, Items.CRIMSON_STAIRS, Items.CRIMSON_SLAB, Items.CRIMSON_FENCE, Items.CRIMSON_FENCE_GATE, null, Items.CRIMSON_FUNGUS, null, Items.CRIMSON_PRESSURE_PLATE, Items.CRIMSON_TRAPDOOR, Items.CRIMSON_HANGING_SIGN, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN);
            makeWoodClass(WoodType.DARK_OAK, "dark_oak", Items.DARK_OAK_PLANKS, Items.DARK_OAK_LOG, Items.STRIPPED_DARK_OAK_LOG, Items.STRIPPED_DARK_OAK_WOOD, Items.DARK_OAK_WOOD, Items.DARK_OAK_SIGN, Items.DARK_OAK_DOOR, Items.DARK_OAK_BUTTON, Items.DARK_OAK_STAIRS, Items.DARK_OAK_SLAB, Items.DARK_OAK_FENCE, Items.DARK_OAK_FENCE_GATE, Items.DARK_OAK_BOAT, Items.DARK_OAK_SAPLING, Items.DARK_OAK_LEAVES, Items.DARK_OAK_PRESSURE_PLATE, Items.DARK_OAK_TRAPDOOR, Items.DARK_OAK_HANGING_SIGN, Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN);
            makeWoodClass(WoodType.OAK, "oak", Items.OAK_PLANKS, Items.OAK_LOG, Items.STRIPPED_OAK_LOG, Items.STRIPPED_OAK_WOOD, Items.OAK_WOOD, Items.OAK_SIGN, Items.OAK_DOOR, Items.OAK_BUTTON, Items.OAK_STAIRS, Items.OAK_SLAB, Items.OAK_FENCE, Items.OAK_FENCE_GATE, Items.OAK_BOAT, Items.OAK_SAPLING, Items.OAK_LEAVES, Items.OAK_PRESSURE_PLATE, Items.OAK_TRAPDOOR, Items.OAK_HANGING_SIGN, Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN);
            makeWoodClass(WoodType.JUNGLE, "jungle", Items.JUNGLE_PLANKS, Items.JUNGLE_LOG, Items.STRIPPED_JUNGLE_LOG, Items.STRIPPED_JUNGLE_WOOD, Items.JUNGLE_WOOD, Items.JUNGLE_SIGN, Items.JUNGLE_DOOR, Items.JUNGLE_BUTTON, Items.JUNGLE_STAIRS, Items.JUNGLE_SLAB, Items.JUNGLE_FENCE, Items.JUNGLE_FENCE_GATE, Items.JUNGLE_BOAT, Items.JUNGLE_SAPLING, Items.JUNGLE_LEAVES, Items.JUNGLE_PRESSURE_PLATE, Items.JUNGLE_TRAPDOOR, Items.JUNGLE_HANGING_SIGN, Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN);
            makeWoodClass(WoodType.SPRUCE, "spruce", Items.SPRUCE_PLANKS, Items.SPRUCE_LOG, Items.STRIPPED_SPRUCE_LOG, Items.STRIPPED_SPRUCE_WOOD, Items.SPRUCE_WOOD, Items.SPRUCE_SIGN, Items.SPRUCE_DOOR, Items.SPRUCE_BUTTON, Items.SPRUCE_STAIRS, Items.SPRUCE_SLAB, Items.SPRUCE_FENCE, Items.SPRUCE_FENCE_GATE, Items.SPRUCE_BOAT, Items.SPRUCE_SAPLING, Items.SPRUCE_LEAVES, Items.SPRUCE_PRESSURE_PLATE, Items.SPRUCE_TRAPDOOR, Items.SPRUCE_HANGING_SIGN, Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN);
            makeWoodClass(WoodType.WARPED, "warped", Items.WARPED_PLANKS, Items.WARPED_STEM, Items.STRIPPED_WARPED_STEM, Items.STRIPPED_WARPED_HYPHAE, Items.WARPED_HYPHAE, Items.WARPED_SIGN, Items.WARPED_DOOR, Items.WARPED_BUTTON, Items.WARPED_STAIRS, Items.WARPED_SLAB, Items.WARPED_FENCE, Items.WARPED_FENCE_GATE, null, Items.WARPED_FUNGUS, null, Items.WARPED_PRESSURE_PLATE, Items.WARPED_TRAPDOOR, Items.WARPED_HANGING_SIGN, Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN);
            //#elseif MC >= 11900
            //$$ makeWoodType(WoodType.MANGROVE, "mangrove", Items.MANGROVE_PLANKS, Items.MANGROVE_LOG, Items.STRIPPED_MANGROVE_LOG, Items.STRIPPED_MANGROVE_WOOD, Items.MANGROVE_WOOD, Items.MANGROVE_SIGN, Items.MANGROVE_DOOR, Items.MANGROVE_BUTTON, Items.MANGROVE_STAIRS, Items.MANGROVE_SLAB, Items.MANGROVE_FENCE, Items.MANGROVE_FENCE_GATE, Items.MANGROVE_BOAT, Items.MANGROVE_PROPAGULE, Items.MANGROVE_LEAVES, Items.MANGROVE_PRESSURE_PLATE, Items.MANGROVE_TRAPDOOR, Blocks.MANGROVE_SIGN, Blocks.MANGROVE_WALL_SIGN);
            //$$ makeWoodType(WoodType.ACACIA, "acacia", Items.ACACIA_PLANKS, Items.ACACIA_LOG, Items.STRIPPED_ACACIA_LOG, Items.STRIPPED_ACACIA_WOOD, Items.ACACIA_WOOD, Items.ACACIA_SIGN, Items.ACACIA_DOOR, Items.ACACIA_BUTTON, Items.ACACIA_STAIRS, Items.ACACIA_SLAB, Items.ACACIA_FENCE, Items.ACACIA_FENCE_GATE, Items.ACACIA_BOAT, Items.ACACIA_SAPLING, Items.ACACIA_LEAVES, Items.ACACIA_PRESSURE_PLATE, Items.ACACIA_TRAPDOOR, Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN);
            //$$ makeWoodType(WoodType.BIRCH, "birch", Items.BIRCH_PLANKS, Items.BIRCH_LOG, Items.STRIPPED_BIRCH_LOG, Items.STRIPPED_BIRCH_WOOD, Items.BIRCH_WOOD, Items.BIRCH_SIGN, Items.BIRCH_DOOR, Items.BIRCH_BUTTON, Items.BIRCH_STAIRS, Items.BIRCH_SLAB, Items.BIRCH_FENCE, Items.BIRCH_FENCE_GATE, Items.BIRCH_BOAT, Items.BIRCH_SAPLING, Items.BIRCH_LEAVES, Items.BIRCH_PRESSURE_PLATE, Items.BIRCH_TRAPDOOR, Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN);
            //$$ makeWoodType(WoodType.CRIMSON, "crimson", Items.CRIMSON_PLANKS, Items.CRIMSON_STEM, Items.STRIPPED_CRIMSON_STEM, Items.STRIPPED_CRIMSON_HYPHAE, Items.CRIMSON_HYPHAE, Items.CRIMSON_SIGN, Items.CRIMSON_DOOR, Items.CRIMSON_BUTTON, Items.CRIMSON_STAIRS, Items.CRIMSON_SLAB, Items.CRIMSON_FENCE, Items.CRIMSON_FENCE_GATE, null, Items.CRIMSON_FUNGUS, null, Items.CRIMSON_PRESSURE_PLATE, Items.CRIMSON_TRAPDOOR, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN);
            //$$ makeWoodType(WoodType.DARK_OAK, "dark_oak", Items.DARK_OAK_PLANKS, Items.DARK_OAK_LOG, Items.STRIPPED_DARK_OAK_LOG, Items.STRIPPED_DARK_OAK_WOOD, Items.DARK_OAK_WOOD, Items.DARK_OAK_SIGN, Items.DARK_OAK_DOOR, Items.DARK_OAK_BUTTON, Items.DARK_OAK_STAIRS, Items.DARK_OAK_SLAB, Items.DARK_OAK_FENCE, Items.DARK_OAK_FENCE_GATE, Items.DARK_OAK_BOAT, Items.DARK_OAK_SAPLING, Items.DARK_OAK_LEAVES, Items.DARK_OAK_PRESSURE_PLATE, Items.DARK_OAK_TRAPDOOR, Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN);
            //$$ makeWoodType(WoodType.OAK, "oak", Items.OAK_PLANKS, Items.OAK_LOG, Items.STRIPPED_OAK_LOG, Items.STRIPPED_OAK_WOOD, Items.OAK_WOOD, Items.OAK_SIGN, Items.OAK_DOOR, Items.OAK_BUTTON, Items.OAK_STAIRS, Items.OAK_SLAB, Items.OAK_FENCE, Items.OAK_FENCE_GATE, Items.OAK_BOAT, Items.OAK_SAPLING, Items.OAK_LEAVES, Items.OAK_PRESSURE_PLATE, Items.OAK_TRAPDOOR, Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN);
            //$$ makeWoodType(WoodType.JUNGLE, "jungle", Items.JUNGLE_PLANKS, Items.JUNGLE_LOG, Items.STRIPPED_JUNGLE_LOG, Items.STRIPPED_JUNGLE_WOOD, Items.JUNGLE_WOOD, Items.JUNGLE_SIGN, Items.JUNGLE_DOOR, Items.JUNGLE_BUTTON, Items.JUNGLE_STAIRS, Items.JUNGLE_SLAB, Items.JUNGLE_FENCE, Items.JUNGLE_FENCE_GATE, Items.JUNGLE_BOAT, Items.JUNGLE_SAPLING, Items.JUNGLE_LEAVES, Items.JUNGLE_PRESSURE_PLATE, Items.JUNGLE_TRAPDOOR, Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN);
            //$$ makeWoodType(WoodType.SPRUCE, "spruce", Items.SPRUCE_PLANKS, Items.SPRUCE_LOG, Items.STRIPPED_SPRUCE_LOG, Items.STRIPPED_SPRUCE_WOOD, Items.SPRUCE_WOOD, Items.SPRUCE_SIGN, Items.SPRUCE_DOOR, Items.SPRUCE_BUTTON, Items.SPRUCE_STAIRS, Items.SPRUCE_SLAB, Items.SPRUCE_FENCE, Items.SPRUCE_FENCE_GATE, Items.SPRUCE_BOAT, Items.SPRUCE_SAPLING, Items.SPRUCE_LEAVES, Items.SPRUCE_PRESSURE_PLATE, Items.SPRUCE_TRAPDOOR, Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN);
            //$$ makeWoodType(WoodType.WARPED, "warped", Items.WARPED_PLANKS, Items.WARPED_STEM, Items.STRIPPED_WARPED_STEM, Items.STRIPPED_WARPED_HYPHAE, Items.WARPED_HYPHAE, Items.WARPED_SIGN, Items.WARPED_DOOR, Items.WARPED_BUTTON, Items.WARPED_STAIRS, Items.WARPED_SLAB, Items.WARPED_FENCE, Items.WARPED_FENCE_GATE, null, Items.WARPED_FUNGUS, null, Items.WARPED_PRESSURE_PLATE, Items.WARPED_TRAPDOOR, Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN);
            //#else
            //$$ makeWoodType(WoodType.ACACIA, "acacia", Items.ACACIA_PLANKS, Items.ACACIA_LOG, Items.STRIPPED_ACACIA_LOG, Items.STRIPPED_ACACIA_WOOD, Items.ACACIA_WOOD, Items.ACACIA_SIGN, Items.ACACIA_DOOR, Items.ACACIA_BUTTON, Items.ACACIA_STAIRS, Items.ACACIA_SLAB, Items.ACACIA_FENCE, Items.ACACIA_FENCE_GATE, Items.ACACIA_BOAT, Items.ACACIA_SAPLING, Items.ACACIA_LEAVES, Items.ACACIA_PRESSURE_PLATE, Items.ACACIA_TRAPDOOR, Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN);
            //$$ makeWoodType(WoodType.BIRCH, "birch", Items.BIRCH_PLANKS, Items.BIRCH_LOG, Items.STRIPPED_BIRCH_LOG, Items.STRIPPED_BIRCH_WOOD, Items.BIRCH_WOOD, Items.BIRCH_SIGN, Items.BIRCH_DOOR, Items.BIRCH_BUTTON, Items.BIRCH_STAIRS, Items.BIRCH_SLAB, Items.BIRCH_FENCE, Items.BIRCH_FENCE_GATE, Items.BIRCH_BOAT, Items.BIRCH_SAPLING, Items.BIRCH_LEAVES, Items.BIRCH_PRESSURE_PLATE, Items.BIRCH_TRAPDOOR, Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN);
            //$$ makeWoodType(WoodType.CRIMSON, "crimson", Items.CRIMSON_PLANKS, Items.CRIMSON_STEM, Items.STRIPPED_CRIMSON_STEM, Items.STRIPPED_CRIMSON_HYPHAE, Items.CRIMSON_HYPHAE, Items.CRIMSON_SIGN, Items.CRIMSON_DOOR, Items.CRIMSON_BUTTON, Items.CRIMSON_STAIRS, Items.CRIMSON_SLAB, Items.CRIMSON_FENCE, Items.CRIMSON_FENCE_GATE, null, Items.CRIMSON_FUNGUS, null, Items.CRIMSON_PRESSURE_PLATE, Items.CRIMSON_TRAPDOOR, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN);
            //$$ makeWoodType(WoodType.DARK_OAK, "dark_oak", Items.DARK_OAK_PLANKS, Items.DARK_OAK_LOG, Items.STRIPPED_DARK_OAK_LOG, Items.STRIPPED_DARK_OAK_WOOD, Items.DARK_OAK_WOOD, Items.DARK_OAK_SIGN, Items.DARK_OAK_DOOR, Items.DARK_OAK_BUTTON, Items.DARK_OAK_STAIRS, Items.DARK_OAK_SLAB, Items.DARK_OAK_FENCE, Items.DARK_OAK_FENCE_GATE, Items.DARK_OAK_BOAT, Items.DARK_OAK_SAPLING, Items.DARK_OAK_LEAVES, Items.DARK_OAK_PRESSURE_PLATE, Items.DARK_OAK_TRAPDOOR, Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN);
            //$$ makeWoodType(WoodType.OAK, "oak", Items.OAK_PLANKS, Items.OAK_LOG, Items.STRIPPED_OAK_LOG, Items.STRIPPED_OAK_WOOD, Items.OAK_WOOD, Items.OAK_SIGN, Items.OAK_DOOR, Items.OAK_BUTTON, Items.OAK_STAIRS, Items.OAK_SLAB, Items.OAK_FENCE, Items.OAK_FENCE_GATE, Items.OAK_BOAT, Items.OAK_SAPLING, Items.OAK_LEAVES, Items.OAK_PRESSURE_PLATE, Items.OAK_TRAPDOOR, Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN);
            //$$ makeWoodType(WoodType.JUNGLE, "jungle", Items.JUNGLE_PLANKS, Items.JUNGLE_LOG, Items.STRIPPED_JUNGLE_LOG, Items.STRIPPED_JUNGLE_WOOD, Items.JUNGLE_WOOD, Items.JUNGLE_SIGN, Items.JUNGLE_DOOR, Items.JUNGLE_BUTTON, Items.JUNGLE_STAIRS, Items.JUNGLE_SLAB, Items.JUNGLE_FENCE, Items.JUNGLE_FENCE_GATE, Items.JUNGLE_BOAT, Items.JUNGLE_SAPLING, Items.JUNGLE_LEAVES, Items.JUNGLE_PRESSURE_PLATE, Items.JUNGLE_TRAPDOOR, Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN);
            //$$ makeWoodType(WoodType.SPRUCE, "spruce", Items.SPRUCE_PLANKS, Items.SPRUCE_LOG, Items.STRIPPED_SPRUCE_LOG, Items.STRIPPED_SPRUCE_WOOD, Items.SPRUCE_WOOD, Items.SPRUCE_SIGN, Items.SPRUCE_DOOR, Items.SPRUCE_BUTTON, Items.SPRUCE_STAIRS, Items.SPRUCE_SLAB, Items.SPRUCE_FENCE, Items.SPRUCE_FENCE_GATE, Items.SPRUCE_BOAT, Items.SPRUCE_SAPLING, Items.SPRUCE_LEAVES, Items.SPRUCE_PRESSURE_PLATE, Items.SPRUCE_TRAPDOOR, Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN);
            //$$ makeWoodType(WoodType.WARPED, "warped", Items.WARPED_PLANKS, Items.WARPED_STEM, Items.STRIPPED_WARPED_STEM, Items.STRIPPED_WARPED_HYPHAE, Items.WARPED_HYPHAE, Items.WARPED_SIGN, Items.WARPED_DOOR, Items.WARPED_BUTTON, Items.WARPED_STAIRS, Items.WARPED_SLAB, Items.WARPED_FENCE, Items.WARPED_FENCE_GATE, null, Items.WARPED_FUNGUS, null, Items.WARPED_PRESSURE_PLATE, Items.WARPED_TRAPDOOR, Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN);
            //#endif
        }
    
        // Just removes the hangingSign argument.
        //#if MC >= 12000
        void makeWoodClass(WoodType type, String prefix, Item planks, Item log, Item strippedLog, Item strippedWood, Item wood, Item sign, Item door, Item button, Item stairs, Item slab, Item fence, Item fenceGate, Item boat, Item sapling, Item leaves, Item pressurePlate, Item trapdoor, Item hangingSign, Block signBlock, Block signWallBlock) {
            put(type, new WoodItems(prefix, planks, log, strippedLog, strippedWood, wood, sign, door, button, stairs, slab, fence, fenceGate, boat, sapling, leaves, pressurePlate, trapdoor, hangingSign, signBlock, signWallBlock));
        }
        //#else
        //$$ void makeWoodType(WoodType type, String prefix, Item planks, Item log, Item strippedLog, Item strippedWood, Item wood, Item sign, Item door, Item button, Item stairs, Item slab, Item fence, Item fenceGate, Item boat, Item sapling, Item leaves, Item pressurePlate, Item trapdoor, Block signBlock, Block signWallBlock) {
        //$$     put(type, new WoodItems(prefix, planks, log, strippedLog, strippedWood, wood, sign, door, button, stairs, slab, fence, fenceGate, boat, sapling, leaves, pressurePlate, trapdoor, null, signBlock, signWallBlock));
        //$$ }
        //#endif
    };

    /*
        private <T, K> T[] mapFromHashMap(Map<?, K> source, Function<K, T> access, IntFunction<T[]> arrayGenerator) {
        return source.values().stream()
                .map(access)
                .toArray(arrayGenerator);
        }
     */

    private abstract static class MapUtil {
        private static <T, K> T[] mapAndCollect(Map<?, K> source, Function<K, T> access, IntFunction<T[]> arrayConstructor) {
            return source.values().stream()
                    .map(access)
                    .filter(Objects::nonNull)
                    .toArray(arrayConstructor);
        }

        public static <T> T[] mapWoodItems(Map<?, WoodItems> woodMap, Function<WoodItems, T> accessor, IntFunction<T[]> arrayConstructor) {
            return mapAndCollect(woodMap, accessor, arrayConstructor);
        }

        public static <T> T[] mapColorItems(Map<?, ColorItems> colorMap, Function<ColorItems, T> accessor, IntFunction<T[]> arrayConstructor) {
            return mapAndCollect(colorMap, accessor, arrayConstructor);
        }
    }

    // Ore arrays
    public static final Block[] ORES = MATERIAL_DATA.values().stream() // This took me a long time....
            .flatMap(materialData -> Arrays.stream(materialData.oreBlocks))
            .map(oreBlockData -> oreBlockData.oreBlock)
            .toArray(Block[]::new);

    public static final Block[] DROP_TO_ORE = MATERIAL_DATA.values().stream() // This took me a long time....
            .flatMap(materialData -> Arrays.stream(materialData.oreBlocks))
            .map(oreBlockData -> oreBlockData.oreBlock)
            .toArray(Block[]::new);

    // Wood-item arrays...
    public static final Item[] SAPLINGS = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.sapling, Item[]::new);
    public static final Block[] SAPLING_SOURCES = MapUtil.mapWoodItems(woodMap, woodItems -> Block.getBlockFromItem(woodItems.sapling), Block[]::new);
    public static final Item[] PLANKS = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.planks, Item[]::new);
    public static final Item[] LEAVES = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.leaves, Item[]::new);
    public static final Item[] WOOD = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.wood, Item[]::new);
    public static final Item[] WOOD_BUTTON = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.button, Item[]::new);
    public static final Item[] WOOD_SIGN = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.sign, Item[]::new);
    public static final Item[] WOOD_PRESSURE_PLATE = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.pressurePlate, Item[]::new);
    public static final Item[] WOOD_FENCE = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.fence, Item[]::new);
    public static final Item[] WOOD_FENCE_GATE = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.fenceGate, Item[]::new);
    public static final Item[] WOOD_BOAT = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.boat, Item[]::new);
    public static final Item[] WOOD_DOOR = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.door, Item[]::new);
    public static final Item[] WOOD_SLAB = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.slab, Item[]::new);
    public static final Item[] WOOD_STAIRS = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.stairs, Item[]::new);
    public static final Item[] WOOD_TRAPDOOR = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.trapdoor, Item[]::new);
    public static final Item[] STRIPPED_LOG = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.strippedLog, Item[]::new);
    public static final Item[] LOG = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.log, Item[]::new);
    public static final Item[] LOGS_ALL = Stream.concat(Arrays.stream(STRIPPED_LOG), Arrays.stream(LOG)).toArray(Item[]::new);
    public static final Block[] WOOD_SIGNS_BLOCK = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.signBlock, Block[]::new);
    public static final Block[] WOOD_SIGNS_WALL = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.signWallBlock, Block[]::new);
    public static final Block[] WOOD_SIGNS_ALL = Stream.concat(Arrays.stream(WOOD_SIGNS_BLOCK), Arrays.stream(WOOD_SIGNS_WALL)).toArray(Block[]::new);
    //#if MC >= 12000
    public static final Item[] WOOD_HANGING_SIGN = MapUtil.mapWoodItems(woodMap, woodItems -> woodItems.hangingSign, Item[]::new);
    //#endif

    // Color-item Arrays...
    public static final Item[] DYE = MapUtil.mapColorItems(colorMap, colorMap -> colorMap.dye, Item[]::new);
    public static final Item[] WOOL = MapUtil.mapColorItems(colorMap, colorMap -> colorMap.wool, Item[]::new);
    public static final Item[] BED = MapUtil.mapColorItems(colorMap, colorMap -> colorMap.bed, Item[]::new);
    public static final Item[] CARPET = MapUtil.mapColorItems(colorMap, colorMap -> colorMap.carpet, Item[]::new);
    public static final Item[] SHULKER_BOXES = MapUtil.mapColorItems(colorMap, colorMap -> colorMap.shulker, Item[]::new);


    public static final Item[] HOSTILE_MOB_DROPS = new Item[]{
            Items.BLAZE_ROD, Items.FEATHER, Items.CHICKEN,
            Items.COOKED_CHICKEN, Items.ROTTEN_FLESH, Items.ZOMBIE_HEAD, Items.GUNPOWDER, Items.CREEPER_HEAD,
            Items.TOTEM_OF_UNDYING, Items.EMERALD, Items.PORKCHOP, Items.COOKED_PORKCHOP, Items.LEATHER,
            Items.MAGMA_CREAM, Items.PHANTOM_MEMBRANE, Items.ARROW, Items.SADDLE, Items.SHULKER_SHELL, Items.BONE,
            Items.SKELETON_SKULL, Items.SLIME_BALL, Items.STRING, Items.SPIDER_EYE,
            Items.GLASS_BOTTLE, Items.GLOWSTONE_DUST, Items.REDSTONE, Items.STICK, Items.SUGAR, Items.POTION,
            Items.NETHER_STAR, Items.COAL, Items.WITHER_SKELETON_SKULL, Items.GHAST_TEAR, Items.IRON_INGOT,
            Items.CARROT, Items.POTATO, Items.BAKED_POTATO,
            //#if MC >= 11800
            Items.COPPER_INGOT,
            //#endif
            //#if MC >= 11900
            Items.SCULK_CATALYST
            //#endif
    };

    //TODO: Add to useless items class
    public static final Item[] DIRTS = new Item[]{
            Items.DIRT, Items.DIRT_PATH, Items.COARSE_DIRT
            //#if MC >= 11800
            , Items.ROOTED_DIRT
            //#endif
    };

    public static final Item[] BUCKETS = new Item[] {
            Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET
            //#if MC >= 11700
            , Items.AXOLOTL_BUCKET
            //#endif
            //#if MC >= 11800
            , Items.POWDER_SNOW_BUCKET
            //#endif
    };


    public static final Item[] FLOWER = new Item[]{Items.ALLIUM, Items.AZURE_BLUET, Items.BLUE_ORCHID, Items.CORNFLOWER, Items.DANDELION, Items.LILAC, Items.LILY_OF_THE_VALLEY, Items.ORANGE_TULIP, Items.OXEYE_DAISY, Items.PINK_TULIP, Items.POPPY, Items.PEONY, Items.RED_TULIP, Items.ROSE_BUSH, Items.SUNFLOWER, Items.WHITE_TULIP};

    public static final Item[] LEATHER_ARMORS = new Item[]{Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_HELMET, Items.LEATHER_BOOTS};
    public static final Item[] GOLDEN_ARMORS = new Item[]{Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_HELMET, Items.GOLDEN_BOOTS};
    public static final Item[] IRON_ARMORS = new Item[]{Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_HELMET, Items.IRON_BOOTS};
    public static final Item[] DIAMOND_ARMORS = new Item[]{Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_HELMET, Items.DIAMOND_BOOTS};
    public static final Item[] NETHERITE_ARMORS = new Item[]{Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_HELMET, Items.NETHERITE_BOOTS};

    public static final Item[] WOODEN_TOOLS = new Item[]{Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_SWORD, Items.WOODEN_AXE, Items.WOODEN_HOE};
    public static final Item[] STONE_TOOLS = new Item[]{Items.STONE_PICKAXE, Items.STONE_SHOVEL, Items.STONE_SWORD, Items.STONE_AXE, Items.STONE_HOE};
    public static final Item[] IRON_TOOLS = new Item[]{Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_SWORD, Items.IRON_AXE, Items.IRON_HOE};
    public static final Item[] GOLDEN_TOOLS = new Item[]{Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_SWORD, Items.GOLDEN_AXE, Items.GOLDEN_HOE};
    public static final Item[] DIAMOND_TOOLS = new Item[]{Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_HOE};
    public static final Item[] NETHERITE_TOOLS = new Item[]{Items.NETHERITE_PICKAXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_SWORD, Items.NETHERITE_AXE, Items.NETHERITE_HOE};

    private static final Map<Item, Item> oreToDrop = new HashMap<>() {
        {
            MATERIAL_DATA.forEach((oreType, materialData) -> {
                Arrays.stream(materialData.oreBlocks).forEach(block -> put(block.oreBlock.asItem(), materialData.rawItem));
            });
        }
    };


    private static final Map<Item, WoodType> planksToType = new HashMap<>() {
        {
            woodMap.forEach((woodType, woodItems) -> put(woodItems.planks, woodType));
        }
    };

    private static final Map<Item, WoodType> logsToTypeMap = new HashMap<>() {
        {
            woodMap.forEach((woodType, woodItems) -> put(woodItems.log, woodType));
        }
    };

    private static final Map<Item, WoodType> strippedToWoodItemsMap = new HashMap<>() {
        {
            woodMap.forEach((woodType, woodItems) -> put(woodItems.strippedLog, woodType));
        }
    };

    public static final Item[] SEEDS = new Item[]{
            Items.BEETROOT_SEEDS,
            Items.MELON_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.WHEAT_SEEDS,

            //#if MC>=12000
            Items.TORCHFLOWER_SEEDS
            //#endif
    };

    public static final HashMap<Item, Item> cookableFoodMap = new HashMap<>() {
        {
            put(Items.PORKCHOP, Items.COOKED_PORKCHOP);
            put(Items.BEEF, Items.COOKED_BEEF);
            put(Items.CHICKEN, Items.COOKED_CHICKEN); // chicken is best meat, fight me
            put(Items.MUTTON, Items.COOKED_MUTTON);
            put(Items.RABBIT, Items.COOKED_RABBIT);
            put(Items.SALMON, Items.COOKED_SALMON);
            put(Items.COD, Items.COOKED_COD);
            put(Items.POTATO, Items.BAKED_POTATO);
        }
    };
    public static final Item[] RAW_FOODS = cookableFoodMap.keySet().toArray(Item[]::new);
    public static final Item[] COOKED_FOODS = cookableFoodMap.values().toArray(Item[]::new);
    private static Map<Item, Integer> fuelTimeMap = null;

    public static String stripItemName(Item item) {
        String[] possibilities = new String[]{"item.minecraft.", "block.minecraft."};
        for (String possible : possibilities) {
            if (item.getTranslationKey().startsWith(possible)) {
                return item.getTranslationKey().substring(possible.length());
            }
        }
        return item.getTranslationKey();
    }

    public static Item[] blocksToItems(Block[] blocks) {
        Item[] result = new Item[blocks.length];
        for (int i = 0; i < blocks.length; ++i) {
            result[i] = blocks[i].asItem();
        }
        return result;
    }



    /* Logs:
        ACACIA
        BIRCH
        CHERRY
        CRIMSON
        DARK_OAK
        OAK
        JUNGLE
        SPRUCE
        WARPED
     */

    /* Colors:
        WHITE
        BLACK
        BLUE
        BROWN
        CYAN
        GRAY
        GREEN
        LIGHT_BLUE
        LIGHT_GRAY
        LIME
        MAGENTA
        ORANGE
        PINK
        PURPLE
        RED
        YELLOW
     */

    public static Block[] itemsToBlocks(Item[] items) {
        ArrayList<Block> result = new ArrayList<>();
        for (Item item : items) {
            if (item instanceof BlockItem) {
                Block b = Block.getBlockFromItem(item);
                if (b != null && b != Blocks.AIR) {
                    result.add(b);
                }
            }
        }
        return result.toArray(Block[]::new);
    }

    public static Item oreToDrop(Item ore) {
        return oreToDrop.getOrDefault(ore, null);
    }

    public static Item logToPlanks(Item logItem) {
        return woodMap.get(logsToTypeMap.get(logItem)).planks;
    }

    public static Item planksToLog(Item plankItem) {
        return woodMap.get(planksToType.get(plankItem)).log;
    }

    public static Item strippedToLogs(Item logItem) {
        return woodMap.get(strippedToWoodItemsMap.get(logItem)).log;
    }


    // Fuel-related methods
    public static Optional<Item> getCookedFood(Item rawFood) {
        return Optional.ofNullable(cookableFoodMap.getOrDefault(rawFood, null));
    }

    public static double getFuelAmount(Item... items) {
        double total = 0;
        for (Item item : items) {
            if (getFuelTimeMap().containsKey(item)) {
                int timeTicks = getFuelTimeMap().get(item);
                // 300 ticks of wood -> 1.5 operations
                // 200 ticks -> 1 operation
                total += (double) timeTicks / 200.0;
            }
        }
        return total;
    }

    public static double getFuelAmount(ItemStack stack) {
        return getFuelAmount(stack.getItem()) * stack.getCount();
    }

    public static boolean isFuel(Item item) {
        return getFuelTimeMap().containsKey(item) || Arrays.stream(PLANKS).toList().contains(item) || Arrays.stream(LOGS_ALL).toList().contains(item);
    }

    private static Map<Item, Integer> getFuelTimeMap() {
        if (fuelTimeMap == null) {
            fuelTimeMap = AbstractFurnaceBlockEntity.createFuelTimeMap();
        }
        return fuelTimeMap;
    }


    public static String trimItemName(String name) {
        if (name.startsWith("block.minecraft.")) {
            name = name.substring("block.minecraft.".length());
        } else if (name.startsWith("item.minecraft.")) {
            name = name.substring("item.minecraft.".length());
        }
        return name;
    }

    /*
    public static ItemTarget[] toItemTargets(Item... items) {
        final int itemCount = items.length;
        final ItemTarget[] itemTargetArray = new ItemTarget[itemCount];
        for (int index = 0; index < itemCount; ++index) {
            itemTargetArray[index] = new ItemTarget(items[index]);
        }
        return itemTargetArray;
    }
    */

    public static ItemTarget[] toItemTargets(Item... items) {
        return Arrays.stream(items)
                .map(ItemTarget::new)
                .toArray(ItemTarget[]::new);
    }

    public static ItemTarget[] toItemTarget(Item item, int count) {
        // Create a new array of ItemTargets with a length of 1.
        ItemTarget[] itemTargets = {new ItemTarget(item, count)};

        // Return the array of ItemTargets.
        return itemTargets;
    }


    private static final List<Block> woolList = Arrays.stream(itemsToBlocks(WOOL)).toList();
    public static boolean areShearsEffective(Block b) {
        return
                //b.getRegistryEntry().streamTags().anyMatch(t -> t ==
                // BlockTags.LEAVES); should also work... but is slower
                b instanceof LeavesBlock
                        || b == Blocks.COBWEB
                        || b == Blocks.TALL_GRASS
                        || b == Blocks.LILY_PAD
                        || b == Blocks.FERN
                        || b == Blocks.DEAD_BUSH
                        || b == Blocks.VINE
                        || b == Blocks.TRIPWIRE
                        || woolList.contains(b)
                        || b == Blocks.NETHER_SPROUTS
                        //#if MC >= 12003
                        || b == Blocks.GRASS_BLOCK;
                        //#else
                        //$$ || b == Blocks.GRASS;
                        //#endif
    }

    public static boolean isStackProtected(AltoClef mod, ItemStack stack) {
        if (stack.hasEnchantments() && mod.getModSettings().getDontThrowAwayEnchantedItems())
            return true;
        if (ItemVer.hasCustomName(stack) && mod.getModSettings().getDontThrowAwayCustomNameItems())
            return true;
        return mod.getBehaviour().isProtected(stack.getItem()) || mod.getModSettings().isImportant(stack.getItem());
    }

    public static boolean canThrowAwayStack(AltoClef mod, ItemStack stack) {
        // Can't throw away empty stacks!
        if (stack.isEmpty())
            return false;
        if (isStackProtected(mod, stack))
            return false;
        return mod.getModSettings().isThrowaway(stack.getItem()) || mod.getModSettings().shouldThrowawayUnusedItems();
    }

    public static boolean canStackTogether(ItemStack from, ItemStack to) {
        if (to.isEmpty() && from.getCount() <= from.getMaxCount())
            return true;
        return to.getItem().equals(from.getItem()) && (from.getCount() + to.getCount() < to.getMaxCount());
    }

    public static class ColorItems {
        public final DyeColor color;
        public final String colorName;
        public final Item dye;
        public final Item wool;
        public final Item bed;
        public final Item carpet;
        public final Item stainedGlass;
        public final Item stainedGlassPane;
        public final Item terracotta;
        public final Item glazedTerracotta;
        public final Item concrete;
        public final Item concretePowder;
        public final Item banner;
        public final Item shulker;
        public final Block wallBanner;

        public ColorItems(DyeColor color, String colorName, Item dye, Item wool, Item bed, Item carpet, Item stainedGlass, Item stainedGlassPane, Item terracotta, Item glazedTerracotta, Item concrete, Item concretePowder, Item banner, Item shulker, Block wallBanner) {
            this.color = color;
            this.colorName = colorName;
            this.dye = dye;
            this.wool = wool;
            this.bed = bed;
            this.carpet = carpet;
            this.stainedGlass = stainedGlass;
            this.stainedGlassPane = stainedGlassPane;
            this.terracotta = terracotta;
            this.glazedTerracotta = glazedTerracotta;
            this.concrete = concrete;
            this.concretePowder = concretePowder;
            this.banner = banner;
            this.shulker = shulker;
            this.wallBanner = wallBanner;
        }
    }

    public static class WoodItems {
        public final String prefix;
        public final Item planks;
        public final Item log;
        public final Item strippedLog;
        public final Item strippedWood;
        public final Item wood;
        public final Item sign;
        public final Item door;
        public final Item button;
        public final Item stairs;
        public final Item slab;
        public final Item fence;
        public final Item fenceGate;
        public final Item boat;
        public final Item sapling;
        public final Item leaves;
        public final Item pressurePlate;
        public final Item trapdoor;
        public final Block signBlock;
        public final Block signWallBlock;

        @Nullable
        public Item hangingSign;

        public WoodItems(String prefix, Item planks, Item log, Item strippedLog, Item strippedWood, Item wood, Item sign, Item door, Item button, Item stairs, Item slab, Item fence, Item fenceGate, Item boat, Item sapling, Item leaves, Item pressurePlate, Item trapdoor, @Nullable Item hangingSign, Block signBlock, Block signWallBlock) {
            this.prefix = prefix;
            this.planks = planks;
            this.log = log;
            this.strippedLog = strippedLog;
            this.strippedWood = strippedWood;
            this.wood = wood;
            this.sign = sign;
            this.door = door;
            this.button = button;
            this.stairs = stairs;
            this.slab = slab;
            this.fence = fence;
            this.fenceGate = fenceGate;
            this.boat = boat;
            this.sapling = sapling;
            this.leaves = leaves;
            this.pressurePlate = pressurePlate;
            this.trapdoor = trapdoor;
            this.hangingSign = hangingSign;
            this.signBlock = signBlock;
            this.signWallBlock = signWallBlock;
        }

        public boolean isNetherWood() {
            return planks == Items.CRIMSON_PLANKS || planks == Items.WARPED_PLANKS;
        }
    }

    // Records, oh records...
    public static class OreBlockData {
        public final Block oreBlock;
        public final OreDistribution distribution;

        public OreBlockData(Block oreBlock, OreDistribution distribution) {
            this.oreBlock = oreBlock;
            this.distribution = distribution;
        }
    }



    public static class MaterialData {
        public final OreBlockData[] oreBlocks;
        public final Item[] armorSetItems;
        public final Item[] toolSetItems;
        public final Block fullBlock;
        public final MiningRequirement miningRequirement;

        public final Item rawItem;
        public final Item defaultItem;


        public MaterialData(OreBlockData[] oreBlocks, MiningRequirement miningRequirement, Item rawItem, Item defaultItem, Item[] armorSetItems, Item[] toolSetItems, Block fullBlock) {
            this.rawItem = rawItem;
            this.defaultItem = defaultItem;
            this.fullBlock = fullBlock;
            this.oreBlocks = oreBlocks;
            this.armorSetItems = armorSetItems;
            this.toolSetItems = toolSetItems;
            this.miningRequirement = miningRequirement;
        }

        public MaterialData(OreBlockData[] oreBlocks, MiningRequirement miningRequirement, Item rawItem, Item defaultItem, Block fullBlock) {
            this(oreBlocks, miningRequirement, rawItem, defaultItem, null, null, fullBlock);
        }

        public MaterialData(OreBlockData[] oreBlocks, MiningRequirement miningRequirement, Item item, Item[] armorSetItems, Item[] toolSetItems, Block fullBlock) {
            this(oreBlocks, miningRequirement, null, item, armorSetItems, toolSetItems, fullBlock);
        }

        public MaterialData(OreBlockData[] oreBlocks, MiningRequirement miningRequirement, Item item, Block fullBlock) {
            this(oreBlocks, miningRequirement, null, item, null, null, fullBlock);
        }
    }

    public static class OreDistribution {
        public final int maxHeight;
        public final int optimalHeight;
        public final int minHeight;
        public final Dimension dimension;

        public OreDistribution(int maxHeight, int optimalHeight, int minHeight, Dimension dimension) {
            if (minHeight > maxHeight) {
                throw new ArithmeticException("Min height more than max height while constructing OreDistribution!");
            }

            this.maxHeight = maxHeight;
            this.optimalHeight = optimalHeight;
            this.minHeight = minHeight;
            this.dimension = dimension;
        }

        public OreDistribution(int maxHeight, int optimalHeight, int minHeight) {
            this(maxHeight, optimalHeight, minHeight, Dimension.OVERWORLD);
        }
    }
}
