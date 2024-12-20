package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.block.InteractWithBlockTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.inventory.EnsureFreeInventorySlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerType;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public class LootContainerTask extends Task {
    public final BlockPos chestPos;
    public final List<Item> targets = new ArrayList<>();
    private final Predicate<ItemStack> check;
    private boolean weDoneHere = false;

    public LootContainerTask(BlockPos chestPos, List<Item> items) {
        this.chestPos = chestPos;
        targets.addAll(items);
        check = x -> true;
    }

    public LootContainerTask(BlockPos chestPos, List<Item> items, Predicate<ItemStack> pred) {
        this.chestPos = chestPos;
        targets.addAll(items);
        check = pred;
    }

    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();
        for (Item item : targets) {
            if (!mod.getBehaviour().isProtected(item)) {
                mod.getBehaviour().addProtectedItems(item);
            }
        }
    }

    @Override
    protected Task onTick(AltoClef mod) {
        // break block above if disallows chest to be opened
        var obstructorPos = chestPos.add(0, 1, 0);
        BlockState state = mod.getWorld().getBlockState(obstructorPos);
        if (state.isOpaqueFullCube(mod.getWorld(), obstructorPos)) {
            return new DestroyBlockTask(obstructorPos);
        }
        if (!ContainerType.screenHandlerMatches(ContainerType.CHEST)) {
            setDebugState("Interact with container");
            return new InteractWithBlockTask(chestPos);
        }
        ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
        if (!cursor.isEmpty()) {
            Optional<Slot> toFit = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false);
            if (toFit.isPresent()) {
                setDebugState("Putting cursor in inventory");
                mod.getSlotHandler().clickSlot(toFit.get(), 0, SlotActionType.PICKUP);
                return null;
            } else {
                setDebugState("Ensuring space");
                return new EnsureFreeInventorySlotTask();
            }
        }
        Optional<Slot> optimal = getAMatchingSlot(mod);
        if (optimal.isEmpty()) {
            weDoneHere = true;
            return null;
        }
        setDebugState("Looting items: " + targets);
        mod.getSlotHandler().clickSlot(optimal.get(), 0, SlotActionType.PICKUP);
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task task) {
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // Try throwing away cursor slot if it's garbage
            garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
        } else {
            StorageHelper.closeScreen();
        }
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof LootContainerTask && targets.equals(((LootContainerTask) other).targets);
    }

    private Optional<Slot> getAMatchingSlot(AltoClef mod) {
        for (Item item : targets) {
            List<Slot> slots = mod.getItemStorage().getSlotsWithItemContainer(item);
            if (!slots.isEmpty()) for (Slot slot : slots) {
                if (check.test(StorageHelper.getItemStackInSlot(slot))) return Optional.of(slot);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return weDoneHere || (ContainerType.screenHandlerMatchesAny() &&
                getAMatchingSlot(mod).isEmpty());
    }

    @Override
    protected String toDebugString() {
        return "Looting a container";
    }
}
