package adris.altoclef.tasks.speedrun.maintasks;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.multiversion.BaritoneVer;
import adris.altoclef.tasks.block.DoToClosestBlockTask;
import adris.altoclef.tasks.block.InteractWithBlockTask;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.misc.SleepThroughNightTask;
import adris.altoclef.tasks.movement.*;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasks.speedrun.KillEnderDragonTask;
import adris.altoclef.tasks.speedrun.KillEnderDragonWithBedsTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.publicenums.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.math.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeKeys;

import java.util.List;
import java.util.Optional;

// Another Beat Minecraft task, entirely rewritten, based off SelfCareTask.
// Only consists of about 320 lines of code. (This might limit compatibility for some situations)
// Author: allclients2

public class BeatMinecraftMicroTask extends Task {

    // Configuration
    private static final int targetEnderEyeCount = 14;
    private static final int targetBedCount = 12;
    private static final boolean getMaxSetBeforeNether = true;

    private static final ItemTarget[] diamondToolSet = ItemHelper.toItemTargets(ItemHelper.DIAMOND_TOOLS);

    private static final Item[] requiredStoneToolset = new Item[] { Items.STONE_SWORD };
    private static final Item[] requiredIronToolset = new Item[] {
        Items.IRON_AXE, Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_SHOVEL
    };
    private static final Item[] requiredDiamondToolset = new Item[] {
        Items.DIAMOND_AXE, Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL
    };

    private static final Block[] warpedForestCommonBlocks = new Block[] {
            Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WARPED_HYPHAE, Blocks.WARPED_NYLIUM
    };
    private static final Block[] netherFortressCommonBlocks = new Block[] {
            Blocks.NETHER_BRICKS,  Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART
    };
    private static final Item[] armorSetMax = new Item[] {  // Gold helmet but with rest as diamond
        Items.GOLDEN_HELMET,
        Items.DIAMOND_CHESTPLATE,
        Items.DIAMOND_LEGGINGS,
        Items.DIAMOND_BOOTS
    };

    private static final Task getBed = TaskCatalogue.getItemTask("bed", targetBedCount);
    private static final Task getFood = new CollectFoodTask(50);
    private static final Task sleepThroughNight = new SleepThroughNightTask();
    private static final Task equipShield = new EquipArmorTask(Items.SHIELD);
    private static final Task getWaterBucket = TaskCatalogue.getItemTask("water_bucket", 1);
    private static final Task getToStronghold = new GoToStrongholdPortalTask(targetEnderEyeCount);
    private static final Task killDragonWithBedsTask = new KillEnderDragonWithBedsTask();
    private static final Task killDragonNormalTask = new KillEnderDragonTask();

    private static boolean taskIsFinished = false;

    // Pair format is <foundPosition, isOpen>
    private static Pair<BlockPos, Boolean> endPortalCenterFound;

    private static Task getToolSet;
    private static Task equipArmorSet;
    private static Task journeyPrimaryTask;

    private static boolean taskUnfinished(AltoClef mod, Task task) {
        //Debug.logMessage("Task is null:" + (task == null));
        return !task.isFinished(mod);
    }

    @Override
    protected void onStart(AltoClef mod) {}

    // Sort enderman by if angry or by distance.
    public static void sortHostileList(List<? extends HostileEntity> hostileEntities, ClientPlayerEntity player) {
        hostileEntities.sort((e1, e2) -> {
            if (e1.isAngryAt(player) != e2.isAngryAt(player)) {
                return e1.isAngryAt(player) ? -1 : 1;
            }
            return Double.compare(e1.squaredDistanceTo(player), e2.squaredDistanceTo(player));
        });
    }

    // Finds Warped forest and kills Ender man for ender pearls.
    private static Task getEnderPearlsTask(AltoClef mod) {
        if (WorldHelper.getCurrentDimension() != Dimension.NETHER) {
            return new DefaultGoToDimensionTask(Dimension.NETHER);
        } else if (WorldHelper.getBiomeAtBlockPos(mod.getWorld(), mod.getPlayer().getBlockPos()) != BiomeKeys.WARPED_FOREST) {
            return new SearchChunkForBlockTask(warpedForestCommonBlocks); // Searched for a WARPED_FOREST
        } else if (!mod.getEntityTracker().entityFound(EndermanEntity.class)) {
            return new SearchWithinBiomeTask(BiomeKeys.WARPED_FOREST);
        }
        final List<EndermanEntity> endermanEntityList = mod.getEntityTracker().getTrackedEntities(EndermanEntity.class);
        sortHostileList(endermanEntityList, mod.getPlayer());
        if (!endermanEntityList.isEmpty()) {
            final EndermanEntity bestTarget = endermanEntityList.get(0); // Can't use getFirst() due to compatibility
            if (bestTarget.getPos().isInRange(mod.getPlayer().getPos(), 50)) {
                return new KillEntityTask(bestTarget);
            }
        }
        return null;
    }

    // Finds nether fortress and kills blazes for blaze rods.
    private static Task getBlazeRodsTask(AltoClef mod) {
        if (WorldHelper.getCurrentDimension() != Dimension.NETHER) {
            return new DefaultGoToDimensionTask(Dimension.NETHER);
        } else if (!mod.getBlockScanner().anyFound(Blocks.SPAWNER)) { // Find "blaze" spawner, hopefully it's the only spawner in the end...
            if (mod.getBlockScanner().anyFound(netherFortressCommonBlocks)) {
                return new SearchChunkForBlockTask(Blocks.SPAWNER);
            } else {
                return new SearchChunkForBlockTask(netherFortressCommonBlocks);
            }
        } else if (!mod.getEntityTracker().entityFound(BlazeEntity.class)) {
            final Optional<BlockPos> blazeSpawnerPosOptional = mod.getBlockScanner().getNearestBlockType(Blocks.SPAWNER);
            if (blazeSpawnerPosOptional.isPresent()) { // Hopefully its always present, it should be...
                return new GetToBlockTask(blazeSpawnerPosOptional.get());
            }
        }
        final List<BlazeEntity> blazeEntityList = mod.getEntityTracker().getTrackedEntities(BlazeEntity.class);
        sortHostileList(blazeEntityList, mod.getPlayer());
        if (!blazeEntityList.isEmpty()) {
            final BlazeEntity bestTarget = blazeEntityList.get(0); // Compatibility
            if (bestTarget.getPos().isInRange(mod.getPlayer().getPos(), 10)) {
                return new KillEntityTask(bestTarget);
            }
        }
        return null;
    }

    // Gets ender eyes ...
    private static Task obtainEnderEyesTask(AltoClef mod) {
        if (mod.getItemStorage().getItemCount(Items.ENDER_PEARL) < targetEnderEyeCount) {
           return getEnderPearlsTask(mod);
        } else if (mod.getItemStorage().getItemCount(Items.BLAZE_ROD) < (int) Math.ceil((double) targetEnderEyeCount / 2)) {
            return getBlazeRodsTask(mod);
        } else { // Should just be able to craft it now...
            return TaskCatalogue.getItemTask(Items.ENDER_EYE, targetEnderEyeCount);
        }
    }

    // Locates and travels to Stronghold, then lights and enters the end portal.
    // Or if an end portal before has already been found (endPortalCenterFound != null) then just travels to that.
    private static Task enterEndWithEnderEyesTask(AltoClef mod) {
        if (endPortalCenterFound != null) {
            final Optional<BlockPos> endPortal = mod.getBlockScanner().getNearestBlockType(Blocks.END_PORTAL);
            if (endPortal.isPresent()) { // Enter portal
                return new DoToClosestBlockTask(blockPos -> new GetToBlockTask(blockPos.up()), Blocks.END_PORTAL);
            } else { // Light portal
                return new DoToClosestBlockTask(blockPos -> new InteractWithBlockTask(Items.ENDER_EYE, blockPos), blockPos -> !WorldHelper.isEndPortalFrameFilled(mod, blockPos), Blocks.END_PORTAL_FRAME);
            }
        } else if (WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
            return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
        } else {
            return getToStronghold;
        }
    }

    // Every tick, find any end portals around us, if found set to the found end portal (endPortalCenterFound)..
    private static void findPossibleEndPortals(AltoClef mod) {
        if (WorldHelper.getCurrentDimension() == Dimension.OVERWORLD) {
            final Optional<BlockPos> nearestEndPortalBlockPos = mod.getBlockScanner().getNearestBlockType(Blocks.END_PORTAL);
            if (nearestEndPortalBlockPos.isPresent()) {
                endPortalCenterFound = new Pair<>(nearestEndPortalBlockPos.get(), true); // `isOpen` always true because the `END_PORTAL` block is present... Which is the portal block its-self...
            } else {
                final BlockPos searchForEndPortal = WorldHelper.doSimpleSearchForEndPortal(mod);
                if (searchForEndPortal != null) {
                    final boolean isOpen = WorldHelper.isEndPortalOpened(mod, searchForEndPortal);
                    // Check if the end portal found is not opened, but if `searchForEndPortal` is open, then dont write it...
                    if (!endPortalCenterFound.getRight() || isOpen)
                        endPortalCenterFound = new Pair<>(searchForEndPortal, isOpen);
                }
            }
        }
    }

    @Override
    protected Task onTick(AltoClef mod) {
        final boolean hasStoneToolSet = mod.getItemStorage().hasItemAll(requiredStoneToolset);
        final boolean hasIronToolSet = mod.getItemStorage().hasItemAll(requiredIronToolset);
        final boolean hasDiamondToolSet = mod.getItemStorage().hasItemAll(requiredDiamondToolset);
        final boolean hasIronArmorSet = StorageHelper.isArmorEquippedAll(mod, ItemHelper.IRON_ARMORS);
        final boolean hasMaxArmorSet = StorageHelper.isArmorEquippedAll(mod, armorSetMax);

        final boolean hasBed = mod.getItemStorage().hasItem(ItemHelper.BED);
        final boolean hasWaterBucket = mod.getItemStorage().hasItem(Items.WATER_BUCKET);
        final boolean hasShield = mod.getItemStorage().hasItemInOffhand(Items.SHIELD);

        // We could find an End portal along the way maybe....
        findPossibleEndPortals(mod);

        // FIXME: If you lose lets say a waterbucket after getting a DiamondToolSet, its never checked again so it will never obtain another. This concept is also true for beds, shields, etc..

        if (MinecraftClient.getInstance().currentScreen instanceof CreditsScreen && WorldHelper.getCurrentDimension() == Dimension.END) {
            Debug.logInternal("Yay! Minecraft beat successfully!");
            taskIsFinished = true;
            return null;
        }

            // Main armor and toolset tasks
        if (!hasDiamondToolSet) {
            if (!hasIronToolSet) {
                if (!hasStoneToolSet) {
                    getToolSet = TaskCatalogue.getSquashedItemTask(requiredStoneToolset);
                } else if (!hasShield) { // AFTER stone tool set Get shield and bed before iron toolset, i think it's very important.
                    return equipShield;
                } else {
                    getToolSet = TaskCatalogue.getSquashedItemTask(requiredIronToolset);
                }
            } else if (taskUnfinished(mod, getWaterBucket) || !hasWaterBucket) { // AFTER iron tool Get water bucket for falls.
                return getWaterBucket;
            } else if (!hasBed) { // AFTER Water Bucket get a bed
                return getBed;
            } else if (!hasIronArmorSet) { // AFTER Bed get iron armor.
                equipArmorSet = new EquipArmorTask(ItemHelper.IRON_ARMORS);
            } else { // After iron armor get a diamond toolset
                getToolSet = TaskCatalogue.getSquashedItemTask(diamondToolSet);
            }
        } else if (!hasMaxArmorSet && getMaxSetBeforeNether) { // After diamond toolset MAYBE get max armor set
            equipArmorSet = new EquipArmorTask(armorSetMax);
        } else if (mod.getItemStorage().getItemCount(Items.ENDER_EYE) < targetEnderEyeCount) { // Get the ender eyes.
            journeyPrimaryTask = obtainEnderEyesTask(mod);
        } else if (WorldHelper.getCurrentDimension() != Dimension.END) { // Enter the end
            journeyPrimaryTask = enterEndWithEnderEyesTask(mod);
        } else if (mod.getBlockScanner().anyFound(Blocks.END_PORTAL)) { // If we are in the End (checked last elseif) and find an end portal, jump in!
            if (!BaritoneVer.isCanWalkOnEndPortal(mod)) {
                BaritoneVer.canWalkOnEndPortal(mod, true);
            }
            journeyPrimaryTask = new DoToClosestBlockTask(blockPos -> new GetToBlockTask(blockPos.up()), (pos) -> Math.abs(pos.getX()) + Math.abs(pos.getZ()) <= 1, Blocks.END_PORTAL);
        } else {
            final Optional<Entity> enderDragonEntity = mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class);
            if (enderDragonEntity.isPresent()) { // Kill the dragon!
                journeyPrimaryTask = hasBed ? killDragonWithBedsTask : killDragonNormalTask;
            } else {
                Debug.logWarning("Ender dragon not found, or is loading.."); // Maybe its just loading in..
            }
        }

        // Priority
        if ((mod.IsNight() && WorldHelper.getCurrentDimension() == Dimension.OVERWORLD) || !mod.getPlayer().isSleeping()) {
            setDebugState("Sleeping through night");
            return sleepThroughNight;
        } else if (taskUnfinished(mod, getFood)) {
            setDebugState("Getting food");
            return getFood;
        } else if (journeyPrimaryTask != null && taskUnfinished(mod, journeyPrimaryTask)) {
            setDebugState("Commiting to journey task");
            return journeyPrimaryTask;
        } else if (getToolSet != null && taskUnfinished(mod, getToolSet)) {
            setDebugState("Getting a toolset");
            return getToolSet;
        } else if (equipArmorSet != null && taskUnfinished(mod, equipArmorSet)) {
            setDebugState("Getting an armor set");
            return equipArmorSet;
        }

        if (WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
            setDebugState("Returning to overworld");
            return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
        }

        setDebugState("All tasks done; wandering indefinitely.");
        return new TimeoutWanderTask();
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return taskIsFinished;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {}

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof BeatMinecraftMicroTask;
    }

    @Override
    protected String toDebugString() {
        return "Beating the game. (allclients2 Micro version)";
    }
}

// No really, "under 320 lines of code"!