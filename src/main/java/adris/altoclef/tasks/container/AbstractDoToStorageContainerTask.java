package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.block.InteractWithBlockTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerCache;
import adris.altoclef.trackers.storage.ContainerType;
import adris.altoclef.util.helpers.BlockHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * Opens a STORAGE container and does whatever you want inside of it
 */
public abstract class AbstractDoToStorageContainerTask extends Task {

    private ContainerType currentContainerType = null;

    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        Optional<BlockPos> containerTarget = getContainerTarget();

        // No container found
        if (containerTarget.isEmpty()) {
            setDebugState("Wandering");
            currentContainerType = null;
            return onSearchWander();
        }

        BlockPos targetPos = containerTarget.get();

        // We're open
        if (currentContainerType != null && ContainerType.screenHandlerMatches(currentContainerType)) {

            Optional<ContainerCache> cache = mod.getItemStorage().getContainerAtPosition(targetPos);
            if (cache.isPresent()) {
                return onContainerOpenSubtask(mod, cache.get());
            }
        }

        // Get to the container
        if (mod.getChunkTracker().isChunkLoaded(targetPos)) {
            Block type = mod.getWorld().getBlockState(targetPos).getBlock();
            currentContainerType = ContainerType.getFromBlock(type);
        }
        if (WorldHelper.isChest(mod, targetPos) && BlockHelper.isSolidBlock(mod, targetPos.up()) && WorldHelper.canBreak(mod, targetPos.up())) {
            setDebugState("Clearing block above chest");
            return new DestroyBlockTask(targetPos.up());
        }
        setDebugState("Opening container: " + targetPos.toShortString());
        return new InteractWithBlockTask(targetPos);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    protected abstract Optional<BlockPos> getContainerTarget();

    protected abstract Task onContainerOpenSubtask(AltoClef mod, ContainerCache containerCache);

    // Virtual
    // TODO: Interface this
    protected Task onSearchWander() {
        return new TimeoutWanderTask();
    }
}
