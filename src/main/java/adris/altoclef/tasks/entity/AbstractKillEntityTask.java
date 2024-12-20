package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.control.LookAtPos;
import adris.altoclef.multiversion.EntityVer;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Weapons;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;

/**
 * Attacks an entity, but the target entity must be specified.
 */
public abstract class AbstractKillEntityTask extends AbstractDoToEntityTask {
    private static final double OTHER_FORCE_FIELD_RANGE = 2;

    // Not the "striking" distance, but the "ok we're close enough, lower our guard for other mobs and focus on this one" range.
    private static final double CONSIDER_COMBAT_RANGE = 10;

    protected AbstractKillEntityTask() {
        this(CONSIDER_COMBAT_RANGE, OTHER_FORCE_FIELD_RANGE);
    }

    protected AbstractKillEntityTask(double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    protected AbstractKillEntityTask(double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    public static boolean equipWeapon(AltoClef mod) {
        Weapons.Weapon bestWeaponWrapper = Weapons.getBestWeapon(mod);
        Item equippedWeapon = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()).getItem();
        if (bestWeaponWrapper != null && bestWeaponWrapper.WeaponItem != equippedWeapon) {
            mod.getSlotHandler().forceEquipItem(bestWeaponWrapper.WeaponItem);
            return true;
        }
        return false;
    }

    @Override
    protected Task onEntityInteract(AltoClef mod, Entity entity) {
        // Equip weapon
        if (!equipWeapon(mod)) {
            float hitProg = mod.getPlayer().getAttackCooldownProgress(0);
            if (hitProg >= 1 && (mod.getPlayer().isOnGround() || mod.getPlayer().getVelocity().getY() < 0 || mod.getPlayer().isTouchingWater())) {
                LookHelper.lookAt(mod, EntityVer.getEyePos(entity), false);
                mod.getControllerExtras().attack(entity);
            } else {
                LookAtPos.lookAtPos(mod, EntityVer.getEyePos(entity)); // Look at them
                LookAtPos.updatePosLook(mod);
            }
        }
        return null;
    }
}