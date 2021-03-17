package adris.altoclef.tasks.misc.speedrun;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.GetToBlockTask;
import adris.altoclef.tasks.misc.EnterNetherPortalTask;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * This is the big kahoona. Plays the whole game.
 */
public class BeatMinecraftTask extends Task {

    /// TUNABLE PROPERTIES
    private static final String[] DIAMOND_ARMORS = new String[] {"diamond_chestplate", "diamond_leggings", "diamond_helmet", "diamond_boots"};
    private static final int PRE_NETHER_FOOD = 5 * 40;
    private static final int PRE_NETHER_FOOD_MIN = 5 * 20;

    private static final int TARGET_BLAZE_RODS = 7;
    private static final int TARGET_ENDER_PEARLS = 14;
    private static final int TARGET_ENDER_EYES = 14;
    private static final int PIGLIN_BARTER_GOLD_INGOT_BUFFER = 32;

    private List<BlockPos> _endPortalFrame = null;

    // A flag to determine whether we should continue doing something.
    private ForceState _forceState = ForceState.NONE;

    private BlockPos _cachedPortalInNether;
    private final CollectBlazeRodsTask _blazeCollection = new CollectBlazeRodsTask(TARGET_BLAZE_RODS);

    private final LocateStrongholdTask _strongholdLocater = new LocateStrongholdTask(TARGET_ENDER_EYES);

    private int _cachedEndPearlsInFrame = 0;

    private BlockPos _netherPortalPos;

    // Get 3 diamond picks, because the nether SUCKS
    private final Task _prepareEquipmentTask = TaskCatalogue.getSquashedItemTask(
            new ItemTarget("diamond_chestplate", 1),
                    new ItemTarget("diamond_leggings", 1),
                    new ItemTarget("diamond_helmet", 1),
                    new ItemTarget("diamond_boots", 1),
                    new ItemTarget("diamond_pickaxe", 3),
                    new ItemTarget("diamond_sword", 1),
                    new ItemTarget("crafting_table", 1)
                    );

    private final Task _netherPrepareTaskJustPick = TaskCatalogue.getItemTask("wooden_pickaxe", 1);
    private final Task _netherPrepareTaskWood = TaskCatalogue.getSquashedItemTask(
            new ItemTarget("wooden_pickaxe", 1),
            new ItemTarget("log", 10)
    );

    @Override
    protected void onStart(AltoClef mod) {
        _forceState = ForceState.NONE;
        mod.getConfigState().push();
        // Add some protections so we don't throw these away at any point.
        mod.getConfigState().addProtectedItems(Items.ENDER_EYE, Items.BLAZE_ROD, Items.ENDER_PEARL, Items.DIAMOND);
        mod.getConfigState().addProtectedItems(ItemTarget.BED);

        mod.getBlockTracker().trackBlock(Blocks.END_PORTAL);
        // Allow walking on end portal
        mod.getConfigState().allowWalkingOn(blockPos -> mod.getChunkTracker().isChunkLoaded(blockPos) && mod.getWorld().getBlockState(blockPos).getBlock() == Blocks.END_PORTAL);
    }

    @Override
    protected Task onTick(AltoClef mod) {
        /*
         * ROUGH PLAN:
         * 1) Get full diamond armor
         * 2) Get lots of food
         * 3) Get to nether
         * 4) Find blaze spawner
         * 5) Kill blazes
         * 6) ??? How to get ender pearls automated and fast...
         */

        switch (mod.getCurrentDimension()) {
            case OVERWORLD:
                return overworldTick(mod);
            case NETHER:
                return netherTick(mod);
            case END:
                return endTick(mod);
        }
        throw new IllegalStateException("Shouldn't ever happen.");
    }

    private Task overworldTick(AltoClef mod) {

        if (_prepareEquipmentTask.isActive() && !_prepareEquipmentTask.isFinished(mod)) {
            setDebugState("Getting equipment");
            return _prepareEquipmentTask;
        }

        // Equip diamond armor asap
        if (hasDiamondArmor(mod) && !diamondArmorEquipped(mod)) {
            return new EquipArmorTask(DIAMOND_ARMORS);
        }
        // Get diamond armor + gear first
        if (!hasDiamondArmor(mod) || !mod.getInventoryTracker().hasItem(Items.DIAMOND_PICKAXE) || !mod.getInventoryTracker().hasItem(Items.DIAMOND_SWORD)) {
            return _prepareEquipmentTask;
        }

        // Stronghold portal located.
        if (strongholdPortalFound() || isEndPortalOpened(mod)) {
            if (mod.getChunkTracker().isChunkLoaded(_endPortalFrame.get(0))) {

                BlockPos nearestPortal = mod.getBlockTracker().getNearestTracking(mod.getPlayer().getPos(), Blocks.END_PORTAL);
                if (nearestPortal != null && mod.getChunkTracker().isChunkLoaded(nearestPortal)) {
                    setDebugState("ENTER PORTAL");
                    return new DoToClosestBlockTask(
                            () -> mod.getPlayer().getPos(),
                            blockPos -> new GetToBlockTask(blockPos.up(), false),
                            pos -> mod.getBlockTracker().getNearestTracking(pos, Blocks.END_PORTAL),
                            Blocks.END_PORTAL
                    );
                }

                int eyesNeeded = 12 - portalEyesInFrame(mod);
                if (mod.getInventoryTracker().getItemCount(Items.ENDER_EYE) < eyesNeeded) {
                    setDebugState("Really bad news, we don't have enough eyes to fill the portal.");
                }

                setDebugState("Filling in portal...");
                return new FillStrongholdPortalTask(true);
            } else {
                // Go to portal
                setDebugState("Going back to portal...");
                return new GetToBlockTask(_endPortalFrame.get(0), false);
            }
        }

        // Locate stronghold portal
        if (_strongholdLocater.isActive() && !_strongholdLocater.isFinished(mod)) {
            setDebugState("Locating end portal.");
            return _strongholdLocater;
        } else {
            if (_endPortalFrame == null && _strongholdLocater.portalFound()) {
                Debug.logMessage("Now we have our portal position.");
                _endPortalFrame = _strongholdLocater.getPortalFrame();
            }
        }
        if (_endPortalFrame == null && mod.getInventoryTracker().getItemCount(Items.ENDER_EYE) >= TARGET_ENDER_EYES) {
            return _strongholdLocater;
        }

        // Get food
        if (mod.getInventoryTracker().totalFoodScore() < PRE_NETHER_FOOD_MIN) {
            _forceState = ForceState.GETTING_FOOD;
        }
        if (_forceState == ForceState.GETTING_FOOD) {
            if (mod.getInventoryTracker().totalFoodScore() < PRE_NETHER_FOOD) {
                setDebugState("Getting food");
                return new CollectFoodTask(PRE_NETHER_FOOD);
            } else {
                _forceState = ForceState.NONE;
            }
        }

        int eyes = mod.getInventoryTracker().getItemCount(Items.ENDER_EYE) + portalEyesInFrame(mod);
        int rodsNeeded = TARGET_BLAZE_RODS - (mod.getInventoryTracker().getItemCount(Items.BLAZE_POWDER) / 2) - eyes;
        int pearlsNeeded = TARGET_ENDER_PEARLS - eyes;

        if (!isEndPortalOpened(mod)) {
            // Get blaze rods by going to nether
            if (mod.getInventoryTracker().getItemCount(Items.BLAZE_ROD) < rodsNeeded || mod.getInventoryTracker().getItemCount(Items.ENDER_PEARL) < pearlsNeeded) {
                //Debug.logInternal(mod.getInventoryTracker().getItemCount(Items.ENDER_PEARL) + "< " + TARGET_ENDER_PEARLS + " : " + mod.getInventoryTracker().getItemCount(Items.BLAZE_ROD) + " < " + rodsNeeded);
                // Go to nether
                if (_netherPortalPos != null) {
                    if (mod.getBlockTracker().isTracking(Blocks.NETHER_PORTAL)) {
                        if (!mod.getBlockTracker().blockIsValid(_netherPortalPos, Blocks.NETHER_PORTAL)) {
                            double MAX_PORTAL_DISTANCE = 2000;
                            // Reset portal if it's far away or we confirmed it being incorrect in this chunk.
                            if (mod.getChunkTracker().isChunkLoaded(_netherPortalPos) || _netherPortalPos.getSquaredDistance(mod.getPlayer().getPos(), false) > MAX_PORTAL_DISTANCE * MAX_PORTAL_DISTANCE) {
                                Debug.logMessage("Invalid portal position detected at " + _netherPortalPos + ", finding new nether portal.");
                                _netherPortalPos = null;
                            }
                        }
                    }
                    if (_netherPortalPos != null) {
                        setDebugState("Going to previously created nether portal...");
                        return new EnterNetherPortalTask(new GetToBlockTask(_netherPortalPos, false), Dimension.NETHER);
                    }
                } else {
                    if (mod.getBlockTracker().isTracking(Blocks.NETHER_PORTAL)) {
                        if (mod.getBlockTracker().anyFound(Blocks.NETHER_PORTAL)) {
                            _netherPortalPos = mod.getBlockTracker().getNearestTracking(mod.getPlayer().getPos(), Blocks.NETHER_PORTAL);
                            Debug.logMessage("Tracked portal at " + _netherPortalPos);
                        }
                    }
                }

                setDebugState("Build nether portal + go to nether");
                return new EnterNetherPortalTask(new ConstructNetherPortalBucketTask(), Dimension.NETHER);
            } else {
                setDebugState("Crafting our blaze powder + eyes");
                int powderNeeded = (TARGET_ENDER_EYES - eyes);
                if (mod.getInventoryTracker().getItemCount(Items.BLAZE_POWDER) < powderNeeded) {
                    return new CraftInInventoryTask(new ItemTarget(Items.BLAZE_POWDER, powderNeeded), CraftingRecipe.newShapedRecipe("blaze_powder",
                            new ItemTarget[]{new ItemTarget(Items.BLAZE_ROD, 1), null, null, null}, 2));
                }
                // Craft
                return new CraftInInventoryTask(new ItemTarget(Items.ENDER_EYE, TARGET_ENDER_EYES), CraftingRecipe.newShapedRecipe("ender_eye",
                        new ItemTarget[]{new ItemTarget(Items.ENDER_PEARL, 1), new ItemTarget(Items.BLAZE_POWDER, 1), null, null}, 1));
            }
        }
        // The end portal is opened. Ummmmm... We shouldn't be here.
        Debug.logError("THIS SHOULDN'T HAPPEN OH NO");
        return null;
    }

    private Task netherTick(AltoClef mod) {

        // Keep track of our portal so we may return to it.
        if (_cachedPortalInNether == null) {
            _cachedPortalInNether = mod.getPlayer().getBlockPos();
        }

        // Collect tools while we're here

        if (_netherPrepareTaskWood.isActive() && !_netherPrepareTaskWood.isFinished(mod)) {
            return _netherPrepareTaskWood;
        }
        if (_netherPrepareTaskJustPick.isActive() && !_netherPrepareTaskJustPick.isFinished(mod)) {
            return _netherPrepareTaskJustPick;
        }

        // Make sure we have at least a wooden pickaxe at all times
        // AND materials to craft a new one, so we aren't stuck in a cavern somewhere.
        int planksCount = 4*mod.getInventoryTracker().getItemCount(ItemTarget.LOG) + mod.getInventoryTracker().getItemCount(ItemTarget.PLANKS);
        int planksNeeded = 3 + (mod.getInventoryTracker().hasItem(Items.CRAFTING_TABLE)? 0 : 4) + (mod.getInventoryTracker().getItemCount(Items.STICK) >= 2? 0 : 2);
        if (!mod.getInventoryTracker().miningRequirementMet(MiningRequirement.WOOD) || planksCount < planksNeeded) {
            // If we ran out of wood, go get more.
            if (planksCount >= planksNeeded) {
                return _netherPrepareTaskJustPick;
            } else {
                return _netherPrepareTaskWood;
            }
        }

        // Blaze rods
        if (mod.getInventoryTracker().getItemCount(Items.BLAZE_ROD) < TARGET_BLAZE_RODS) {
            setDebugState("Collecting Blaze Rods");
            return _blazeCollection;
        }

        // Piglin Barter
        if (mod.getInventoryTracker().getItemCount(Items.ENDER_PEARL) < TARGET_ENDER_PEARLS) {

            if (!mod.getInventoryTracker().miningRequirementMet(MiningRequirement.STONE)) {
                // Eh just in case if we have a few extra diamonds laying around
                if (mod.getInventoryTracker().getItemCount(Items.DIAMOND) >= 3) {
                    setDebugState("Collecting a diamond pickaxe instead of a wooden one, since we can.");
                    return TaskCatalogue.getItemTask("diamond_pickaxe", 1);
                }
                // In case if we have extra stone
                if (mod.getInventoryTracker().getItemCount(Items.COBBLESTONE) >= 3) {
                    setDebugState("Collecting a stone pickaxe instead of a wooden one, since we can.");
                    return TaskCatalogue.getItemTask("stone_pickaxe", 1);
                }
            }

            setDebugState("Collecting Ender Pearls");
            return new TradeWithPiglinsTask(PIGLIN_BARTER_GOLD_INGOT_BUFFER, new ItemTarget(Items.ENDER_PEARL, TARGET_ENDER_PEARLS));
        }

        setDebugState("Getting the hell out of here");
        return new EnterNetherPortalTask(new GetToBlockTask(_cachedPortalInNether, false), Dimension.OVERWORLD);
    }

    private Task endTick(AltoClef mod) {
        return null;
    }

    private boolean diamondArmorEquipped(AltoClef mod) {
        for (String armor : DIAMOND_ARMORS) {
            if (!mod.getInventoryTracker().isArmorEquipped(TaskCatalogue.getItemMatches(armor)[0])) return false;
        }
        return true;
    }
    private boolean hasDiamondArmor(AltoClef mod) {
        for (String armor : DIAMOND_ARMORS) {
            Item item = TaskCatalogue.getItemMatches(armor)[0];
            if (mod.getInventoryTracker().isArmorEquipped(item)) continue;
            if (!mod.getInventoryTracker().hasItem(item)) return false;
        }
        return true;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        // Most likely we have failed or cancelled at this point.
        // But one day this will actually trigger after the game is completed. Just you wait.
        mod.getConfigState().pop();
            mod.getBlockTracker().stopTracking(Blocks.END_PORTAL);
    }

    @Override
    protected boolean isEqual(Task obj) {
        return obj instanceof BeatMinecraftTask;
    }

    @Override
    protected String toDebugString() {
        return "Beating the game";
    }

    private boolean strongholdPortalFound() {
        return _endPortalFrame != null;
    }

    private boolean isEndPortalOpened(AltoClef mod) {
        return portalEyesInFrame(mod) >= 12;
    }

    private int portalEyesInFrame(AltoClef mod) {
        int count = 0;
        if (strongholdPortalFound()) {
            for (BlockPos b : _endPortalFrame) {
                if (!mod.getChunkTracker().isChunkLoaded(b)) {
                    return _cachedEndPearlsInFrame;
                }
                boolean filled = isEndPortalFrameFilled(mod, b);
                if (filled) {
                    count++;
                }
            }
        }
        _cachedEndPearlsInFrame = count;
        return count;
    }

    public static boolean isEndPortalFrameFilled(AltoClef mod, BlockPos pos) {
        if (!mod.getChunkTracker().isChunkLoaded(pos)) return false;
        BlockState state = mod.getWorld().getBlockState(pos);
        if (state.getBlock() != Blocks.END_PORTAL_FRAME) {
            Debug.logWarning("BLOCK POS " + pos + " DOES NOT CONTAIN END PORTAL FRAME! This is probably due to a bug/incorrect assumption.");
        }
        return state.get(EndPortalFrameBlock.EYE);
    }

    private enum ForceState {
        NONE,
        GETTING_DIAMOND_GEAR,
        GETTING_FOOD
    }
}
