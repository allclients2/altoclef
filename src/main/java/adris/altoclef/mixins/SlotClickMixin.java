package adris.altoclef.mixins;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.SlotClickChangedEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ScreenHandler.class)
public class SlotClickMixin {

    //#if MC>11605
    @Inject(
            method = "internalOnSlotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;internalOnSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V")
    )
    private void slotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (AltoClef.INSTANCE == null || AltoClef.INSTANCE.getPlayer() == null)
            return;

        ScreenHandler screenHandler = AltoClef.INSTANCE.getPlayer().currentScreenHandler;

        //#if MC >= 11800
        DefaultedList<Slot> afterSlots = screenHandler.slots;
        //#else
        //$$ List<Slot> afterSlots = screenHandler.slots;
        //#endif

        List<ItemStack> beforeStacks = new ArrayList<>(afterSlots.size());
        for (Slot slot : afterSlots) {
            beforeStacks.add(slot.getStack().copy());
        }
        // Perform slot changes potentially
        screenHandler.onSlotClick(slotIndex, button, actionType, player);
        // Check for changes and alert
        for (int i = 0; i < beforeStacks.size(); ++i) {
            ItemStack before = beforeStacks.get(i);
            ItemStack after = afterSlots.get(i).getStack();
            if (!ItemStack.areEqual(before, after)) {
                adris.altoclef.util.slots.Slot slot = adris.altoclef.util.slots.Slot.getFromCurrentScreen(i);
                EventBus.publish(new SlotClickChangedEvent(slot, before, after));
            }
        }
    }
    //#endif

}
