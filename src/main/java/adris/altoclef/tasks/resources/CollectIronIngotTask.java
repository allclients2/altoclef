package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.container.SmeltInFurnaceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.SmeltTarget;
import net.minecraft.item.Items;

public class CollectIronIngotTask extends ResourceTask {

    private final int count;

    public CollectIronIngotTask(int count) {
        super(Items.IRON_INGOT, count);
        this.count = count;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        mod.getBehaviour().push();
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        //#if MC>=11700
        return new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.IRON_INGOT, count), new ItemTarget(Items.RAW_IRON, count)));
        //#else
        //$$ return new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.IRON_INGOT, count), new ItemTarget(Items.IRON_ORE, count)));
        //#endif
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectIronIngotTask same && same.count == count;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + count + " iron.";
    }
}
