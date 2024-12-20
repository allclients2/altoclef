package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.multiversion.EntityVer;
import adris.altoclef.tasks.entity.AbstractDoToEntityTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.EntityHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import java.util.HashSet;
import java.util.Optional;

public class TradeWithPiglinsTask extends ResourceTask {

    // TODO: Settings? Custom parameter?
    private static final boolean AVOID_HOGLINS = true;
    private static final double HOGLIN_AVOID_TRADE_RADIUS = 64;
    // If we're too far away from a trading piglin, we risk deloading them and losing the trade.
    private static final double TRADING_PIGLIN_TOO_FAR_AWAY = 64 + 8;
    private final int goldBuffer;
    private final Task tradeTask = new PerformTradeWithPiglin();
    private Task goldTask = null;

    public TradeWithPiglinsTask(int goldBuffer, ItemTarget[] itemTargets) {
        super(itemTargets);
        this.goldBuffer = goldBuffer;
    }

    public TradeWithPiglinsTask(int goldBuffer, ItemTarget target) {
        super(target);
        this.goldBuffer = goldBuffer;
    }

    public TradeWithPiglinsTask(int goldBuffer, Item item, int targetCount) {
        super(item, targetCount);
        this.goldBuffer = goldBuffer;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {

    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // Collect gold if we don't have it.
        if (goldTask != null && goldTask.isActive() && !goldTask.isFinished(mod)) {
            setDebugState("Collecting gold");
            return goldTask;
        }
        if (!mod.getItemStorage().hasItem(Items.GOLD_INGOT)) {
            if (goldTask == null) goldTask = TaskCatalogue.getItemTask(Items.GOLD_INGOT, goldBuffer);
            return goldTask;
        }

        // If we have no piglin nearby, explore until we find piglin.
        if (!mod.getEntityTracker().entityFound(PiglinEntity.class)) {
            setDebugState("Wandering");
            return new TimeoutWanderTask(false);
        }

        // If we have a trading piglin that's too far away, get closer to it.

        // Find gold and trade with a piglin
        setDebugState("Trading with Piglin");
        return tradeTask;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof TradeWithPiglinsTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Trading with Piglins";
    }

    static class PerformTradeWithPiglin extends AbstractDoToEntityTask {

        private static final double PIGLIN_NEARBY_RADIUS = 10;
        private final TimerGame barterTimeout = new TimerGame(2);
        private final TimerGame intervalTimeout = new TimerGame(10);
        private final HashSet<Entity> blacklisted = new HashSet<>();
        private Entity currentlyBartering = null;

        public PerformTradeWithPiglin() {
            super(3);
        }

        @Override
        protected void onStart(AltoClef mod) {
            super.onStart(mod);

            mod.getBehaviour().push();

            // Don't throw away our gold lol
            mod.getBehaviour().addProtectedItems(Items.GOLD_INGOT);

            // Don't attack piglins unless we've blacklisted them.
            mod.getBehaviour().addForceFieldExclusion(entity -> {
                if (entity instanceof PiglinEntity) {
                    return !blacklisted.contains(entity);
                }
                return false;
            });
            //_blacklisted.clear();
        }

        @Override
        protected void onStop(AltoClef mod, Task interruptTask) {
            super.onStop(mod, interruptTask);
            mod.getBehaviour().pop();
        }

        @Override
        protected boolean isSubEqual(AbstractDoToEntityTask other) {
            return other instanceof PerformTradeWithPiglin;
        }

        @Override
        protected Task onEntityInteract(AltoClef mod, Entity entity) {

            // If we didn't run this in a while, we can retry bartering.
            if (intervalTimeout.elapsed()) {
                // We didn't interact for a while, continue bartering as usual.
                barterTimeout.reset();
                intervalTimeout.reset();
            }

            // We're trading so reset the barter timeout
            if (EntityHelper.isTradingPiglin(currentlyBartering)) {
                barterTimeout.reset();
            }

            // We're bartering a new entity.
            if (!entity.equals(currentlyBartering)) {
                currentlyBartering = entity;
                barterTimeout.reset();
            }

            if (barterTimeout.elapsed()) {
                // We failed bartering.
                Debug.logMessage("Failed bartering with current piglin, blacklisting.");
                blacklisted.add(currentlyBartering);
                barterTimeout.reset();
                currentlyBartering = null;
                return null;
            }

            if (AVOID_HOGLINS && currentlyBartering != null && !EntityHelper.isTradingPiglin(currentlyBartering)) {
                Optional<Entity> closestHoglin = mod.getEntityTracker().getClosestEntity(currentlyBartering.getPos(), HoglinEntity.class);
                if (closestHoglin.isPresent() && closestHoglin.get().isInRange(entity, HOGLIN_AVOID_TRADE_RADIUS)) {
                    Debug.logMessage("Aborting further trading because a hoglin showed up");
                    blacklisted.add(currentlyBartering);
                    barterTimeout.reset();
                    currentlyBartering = null;
                }
            }

            setDebugState("Trading with piglin");

            if (mod.getSlotHandler().forceEquipItem(Items.GOLD_INGOT)) {
                mod.getController().interactEntity(mod.getPlayer(), entity, Hand.MAIN_HAND);
                LookHelper.lookAt(mod, EntityVer.getEyePos(entity));
                intervalTimeout.reset();
            }
            return null;
        }

        @Override
        protected Optional<Entity> getEntityTarget(AltoClef mod) {
            // Ignore trading piglins
            Optional<Entity> found = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(),
                    entity -> {
                        if (blacklisted.contains(entity)
                                || EntityHelper.isTradingPiglin(entity)
                                || (entity instanceof LivingEntity && ((LivingEntity) entity).isBaby())
                                || (currentlyBartering != null && !entity.isInRange(currentlyBartering, PIGLIN_NEARBY_RADIUS))) {
                            return false;
                        }

                        if (AVOID_HOGLINS) {
                            // Avoid trading if hoglin is anywhere remotely nearby.
                            Optional<Entity> closestHoglin = mod.getEntityTracker().getClosestEntity(entity.getPos(), HoglinEntity.class);
                            return closestHoglin.isEmpty() || !closestHoglin.get().isInRange(entity, HOGLIN_AVOID_TRADE_RADIUS);
                        }
                        return true;
                    }, PiglinEntity.class
            );
            if (found.isEmpty()) {
                if (currentlyBartering != null && (blacklisted.contains(currentlyBartering) || !currentlyBartering.isAlive())) {
                    currentlyBartering = null;
                }
                found = Optional.ofNullable(currentlyBartering);
            }
            return found;
        }

        @Override
        protected String toDebugString() {
            return "Trading with piglin";
        }
    }

}
