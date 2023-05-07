package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.misc.SleepThroughNightTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class SelfCareTask extends Task {
    private static final ItemTarget[] woodToolSet = new ItemTarget[]{
            new ItemTarget(Items.WOODEN_SWORD, 1),
            new ItemTarget(Items.WOODEN_PICKAXE, 1),
            new ItemTarget(Items.WOODEN_AXE, 1),
            new ItemTarget(Items.WOODEN_SHOVEL, 1),
    };
    private static final ItemTarget[] stoneToolSet = new ItemTarget[]{
            new ItemTarget(Items.STONE_SWORD, 1),
            new ItemTarget(Items.STONE_PICKAXE, 1),
            new ItemTarget(Items.STONE_AXE, 1),
            new ItemTarget(Items.STONE_SHOVEL, 1),
    };
    private static final ItemTarget[] ironToolSet = new ItemTarget[]{
            new ItemTarget(Items.IRON_SWORD, 1),
            new ItemTarget(Items.IRON_PICKAXE, 1),
            new ItemTarget(Items.IRON_AXE, 1),
            new ItemTarget(Items.IRON_SHOVEL, 1),
    };
    private static final ItemTarget[] diamondToolSet = new ItemTarget[]{
            new ItemTarget(Items.DIAMOND_SWORD, 1)
    };
    private static final Item[] ironArmorSet = new Item[]{
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS
    };
    private static final Item[] beds = ItemHelper.BED;
    private static Task getToolSet;
    private static final Task getBed = TaskCatalogue.getItemTask("bed", 1);
    private static final Task getFood = new CollectFoodTask(100);
    private static final Task sleepThroughNight = new SleepThroughNightTask();
    private static final Task equipShield = new EquipArmorTask(Items.SHIELD);
    private static Task equipArmorSet;
    private static boolean isTaskNotFinished(AltoClef mod, Task task){
        return task != null && task.isActive() && !task.isFinished(mod);
    }
    private static String debugStateName;
    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        boolean hasWoodToolSet = mod.getItemStorage().hasItem(woodToolSet);
        boolean hasStoneToolSet = mod.getItemStorage().hasItem(stoneToolSet);
        boolean hasIronToolSet = mod.getItemStorage().hasItem(ironToolSet);
        boolean hasBed = mod.getItemStorage().hasItem(beds);
        boolean hasShield = StorageHelper.isArmorEquipped(mod, Items.SHIELD);
        boolean hasIronArmorSet = StorageHelper.isArmorEquippedAll(mod, ironArmorSet);
        boolean hasDiamondToolSet = mod.getItemStorage().hasItem(diamondToolSet);
        if (hasBed && WorldHelper.canSleep()){
            setDebugState("Sleeping through night");
            return sleepThroughNight;
        }
        if (isTaskNotFinished(mod, getToolSet)){
            setDebugState(debugStateName);
            return getToolSet;
        }
        if (isTaskNotFinished(mod, equipShield)){
            setDebugState(debugStateName);
            return equipShield;
        }
        if (isTaskNotFinished(mod, equipArmorSet)){
            setDebugState(debugStateName);
            return equipArmorSet;
        }
        if (isTaskNotFinished(mod, getBed)){
            setDebugState(debugStateName);
            return getBed;
        }
        if (isTaskNotFinished(mod, getFood)){
            setDebugState(debugStateName);
            return getFood;
        }
        if (!hasWoodToolSet){
            debugStateName = "Getting wood tool set";
            getToolSet = TaskCatalogue.getSquashedItemTask(woodToolSet);
            return getToolSet;
        }
        if (!hasStoneToolSet){
            debugStateName = "Getting stone tool set";
            getToolSet = TaskCatalogue.getSquashedItemTask(stoneToolSet);
            return getToolSet;
        }
        if (!hasBed){
            debugStateName = "Getting bed";
            return getBed;
        }
        if (!mod.getFoodChain().hasFood()){
            debugStateName = "Getting food";
            return getFood;
        }
        if (!hasIronToolSet){
            debugStateName = "Getting iron tool set";
            getToolSet = TaskCatalogue.getSquashedItemTask(ironToolSet);
            return getToolSet;
        }
        if (!hasShield){
            debugStateName = "Getting shield";
            return equipShield;
        }
        if (!hasIronArmorSet){
            debugStateName = "Getting and equipping iron armor set";
            equipArmorSet = new EquipArmorTask(ironArmorSet);
            return equipArmorSet;
        }
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SelfCareTask;
    }

    @Override
    protected String toDebugString() {
        return "Caring self";
    }
}
