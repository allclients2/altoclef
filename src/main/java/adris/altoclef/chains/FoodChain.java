package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Settings;
import adris.altoclef.multiversion.FoodComponentWrapper;
import adris.altoclef.multiversion.ItemVer;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasks.speedrun.DragonBreathTracker;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.publicenums.Dimension;
import adris.altoclef.util.helpers.*;
import adris.altoclef.util.slots.PlayerSlot;
import baritone.api.utils.input.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FoodChain extends SingleTaskChain {
    private static FoodChainConfig config;
    private static boolean hasFood;

    static {
        ConfigHelper.loadConfig("altoclef/food_chain_settings.json", FoodChainConfig::new, FoodChainConfig.class, newConfig -> config = newConfig);
    }

    private final DragonBreathTracker dragonBreathTracker = new DragonBreathTracker();
    private boolean isTryingToEat = false;
    private boolean requestFillup = false;
    private boolean needsFood = false;
    private Optional<Item> cachedPerfectFood = Optional.empty();
    private boolean shouldStop = false;

    public FoodChain(TaskRunner runner) {
        super(runner);
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        // Nothing.
    }

    private void startEat(AltoClef mod, Item food) {
        //Debug.logInternal("EATING " + toUse.getTranslationKey() + " : " + test);
        isTryingToEat = true;
        requestFillup = true;
        mod.getSlotHandler().forceEquipItem(new Item[]{food}, true); //"true" because it's food
        mod.getInputControls().hold(Input.CLICK_RIGHT);
        mod.getExtraBaritoneSettings().setInteractionPaused(true);
    }

    private void stopEat(AltoClef mod) {
        if (isTryingToEat) {
            if (mod.getItemStorage().hasItem(Items.SHIELD) || mod.getItemStorage().hasItemInOffhand(Items.SHIELD)) {
                if (StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT).getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else {
                    isTryingToEat = false;
                    requestFillup = false;
                }
            } else {
                isTryingToEat = false;
                requestFillup = false;
            }
            mod.getInputControls().release(Input.CLICK_RIGHT);
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
        }
    }

    public boolean isTryingToEat() {
        return isTryingToEat;
    }

    @Override
    public float getPriority(AltoClef mod) {
        if (WorldHelper.isInNetherPortal(mod)) {
            stopEat(mod);
            return Float.NEGATIVE_INFINITY;
        }
        // do not interrupt defending from mobs by eating
        if (mod.getMobDefenseChain().isPuttingOutFire()
                || mod.getMobDefenseChain().isShielding()
                || mod.getPlayer().isBlocking()
                || mod.getMobDefenseChain().isDoingAcrobatics()
                || !mod.getEntityTracker().getHostiles().isEmpty() && mod.getPlayer().getHealth() < 4
        ) {
            stopEat(mod);
            return Float.NEGATIVE_INFINITY;
        }
        dragonBreathTracker.updateBreath(mod);
        for (BlockPos playerIn : WorldHelper.getBlocksTouchingPlayer(mod)) {
            if (dragonBreathTracker.isTouchingDragonBreath(playerIn)) {
                stopEat(mod);
                return Float.NEGATIVE_INFINITY;
            }
        }
        if (!mod.getModSettings().isAutoEat()) {
            stopEat(mod);
            return Float.NEGATIVE_INFINITY;
        }

        // do NOT eat while in lava if we are escaping it (spaghetti code dependencies go brrrr)
        if (mod.getPlayer().isInLava()) {
            stopEat(mod);
            return Float.NEGATIVE_INFINITY;
        }

        /*
        - Eats if:
        - We're hungry and have food that fits
            - We're low on health and maybe a little bit hungry
            - We're very low on health and are even slightly hungry
        - We're kind of hungry and have food that fits perfectly
         */

        // We're in danger, don't eat now!!
        if (!mod.getMLGBucketChain().doneMLG() || mod.getMLGBucketChain().isFalling(mod) ||
                mod.getPlayer().isBlocking() || shouldStop) {
            stopEat(mod);
            return Float.NEGATIVE_INFINITY;
        }
        Pair<Integer, Optional<Item>> calculation = calculateFood(mod);

        int foodLevel = mod.getPlayer().getHungerManager().getFoodLevel();
        int _cachedFoodScore = calculation.getLeft();
        cachedPerfectFood = calculation.getRight();
        hasFood = _cachedFoodScore > 0;

        // If we requested a fillup but we're full, stop.
        if (requestFillup && foodLevel >= 20) {
            requestFillup = false;
        }


        // If we no longer have food, we no longer can eat.
        if (!hasFood) {
            requestFillup = false;
        }

        //FIXME should check if currently fighting
        if (hasFood && (needsToEat() || requestFillup) && cachedPerfectFood.isPresent() &&
                !mod.getMLGBucketChain().isChorusFruiting() && !mod.getPlayer().isBlocking()/* &&
                !areEnemiesNearby(mod)*/) {

            Item toUse = cachedPerfectFood.get();

            // Make sure we're not facing a container
            if (!LookHelper.tryAvoidingInteractable(mod)) {
                return Float.NEGATIVE_INFINITY;
            }
            startEat(mod, toUse);
        } else {
            stopEat(mod);
        }

        Settings settings = mod.getModSettings();


        if (needsFood || _cachedFoodScore < settings.getMinimumFoodAllowed()) {
            needsFood = _cachedFoodScore < settings.getFoodUnitsToCollect();

            // Only collect if we don't have enough food.
            // If the user inputs invalid settings, the bot would get stuck here.
            if (_cachedFoodScore < settings.getFoodUnitsToCollect()) {
                setTask(new CollectFoodTask(settings.getFoodUnitsToCollect()));
                return 55f;
            }
            //Else do next check
        }

        // healthLeft where 0 is dead and 1 is full health
        float healthLeft = (mod.getPlayer().getHealth() / mod.getPlayer().getMaxHealth());

        // Auto get food if we REALLY NEED it for regen
        if (
            WorldHelper.getCurrentDimension() == Dimension.OVERWORLD &&
            settings.isautoCollectFoodForRegen() &&
            !hasFood && // No food
            healthLeft < 0.65 && // Is low health
            foodLevel < 20 && // Is hungry
            !mod.getPlayer().isDead() && // Not dead
            !mod.IsNight() // Can't find any animals right?
        ) {
            //Then we eat to have regen.
            float damage = (1 - healthLeft); //Health Diff from Max Health
            setTask(new CollectFoodTask(Math.round(0.8 * damage * mod.getPlayer().getMaxHealth())));
            return 65f * damage; //Priority will be near the multiplier if health is very low.
        }


        // Food eating is handled asynchronously.
        return Float.NEGATIVE_INFINITY;
    }

    //TODO: Ask MobDefenseChain instead
    private boolean areEnemiesNearby(AltoClef mod) {
        for (Entity entity : mod.getEntityTracker().getCloseEntities()) {
            if (entity instanceof HostileEntity hostile && hostile.distanceTo(mod.getPlayer()) < (isTryingToEat?14:7)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isActive() {
        // We're always checking for food.
        return true;
    }

    @Override
    public String getName() {
        return "Food";
    }

    @Override
    protected void onStop(AltoClef mod) {
        super.onStop(mod);
        stopEat(mod);
    }

    public boolean needsToEat() {
        if (!hasFood() || shouldStop) {
            return false;
        }

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        int foodLevel = player.getHungerManager().getFoodLevel();
        float health = player.getHealth();

        if (foodLevel >= 20) {
            // We can't eat, were full.
            return false;
        }

        if (health <= 10) {
            return true;
        }

        //Debug.logMessage("FOOD: " + foodLevel + " -- HEALTH: " + health);

        // Eat if we're desperate/need to heal ASAP
        if (player.isOnFire() || player.hasStatusEffect(StatusEffects.WITHER) || health < config.alwaysEatWhenWitherOrFireAndHealthBelow) {
            return true;
        } else if (foodLevel > config.alwaysEatWhenBelowHunger) {
            if (health < config.alwaysEatWhenBelowHealth) {
                return true;
            }
        } else {
            // We have half hunger
            return true;
        }


        // Eat if we're  units hungry and we have a perfect fit.
        if (foodLevel < config.alwaysEatWhenBelowHungerAndPerfectFit && cachedPerfectFood.isPresent()) {
            int need = 20 - foodLevel;
            Item best = cachedPerfectFood.get();

            int fills = (ItemVer.getFoodComponent(best) != null) ? ItemVer.getFoodComponent(best).getHunger() : -1;
            return fills == need;
        }

        return false;
    }

    public Pair<Integer, Optional<Item>> calculateFood(AltoClef mod) {
        Item bestFood = null;
        double bestFoodScore = Double.NEGATIVE_INFINITY;
        int foodTotal = 0;
        ClientPlayerEntity player = mod.getPlayer();
        float health = player != null ? player.getHealth() : 20;
        //float toHeal = player != null? 20 - player.getHealth() : 0;
        float hunger = player != null ? player.getHungerManager().getFoodLevel() : 20;
        float saturation = player != null ? player.getHungerManager().getSaturationLevel() : 20;
        // Get best food item + calculate food total
        for (ItemStack stack : mod.getItemStorage().getItemStacksPlayerInventory(true)) {
            if (ItemVer.isFood(stack)) {
                // Ignore protected items
            	// if we ignore protected foods system gets stuck
            	// if (!ItemHelper.canThrowAwayStack(mod, stack)) continue;

                // Ignore spider eyes
                if (stack.getItem() == Items.SPIDER_EYE) {
                    continue;
                }

                FoodComponentWrapper food = ItemVer.getFoodComponent(stack.getItem());

                assert food != null;
                float hungerIfEaten = Math.min(hunger + food.getHunger(), 20);
                float saturationIfEaten = Math.min(hungerIfEaten, saturation + food.getSaturationModifier());
                float gainedSaturation = (saturationIfEaten - saturation);
                float gainedHunger = (hungerIfEaten - hunger);
                float hungerNotFilled = 20 - hungerIfEaten;

                float saturationWasted = food.getSaturationModifier() - gainedSaturation;
                float hungerWasted = food.getHunger() - gainedHunger;

                boolean prioritizeSaturation = health < config.prioritizeSaturationWhenBelowHealth;
                float saturationGoodScore = prioritizeSaturation ? gainedSaturation * config.foodPickPrioritizeSaturationSaturationMultiplier : gainedSaturation;
                float saturationLossPenalty = prioritizeSaturation ? 0 : saturationWasted * config.foodPickSaturationWastePenaltyMultiplier;
                float hungerLossPenalty = hungerWasted * config.foodPickHungerWastePenaltyMultiplier;
                float hungerNotFilledPenalty = hungerNotFilled * config.foodPickHungerNotFilledPenaltyMultiplier;

                float score = saturationGoodScore - saturationLossPenalty - hungerLossPenalty - hungerNotFilledPenalty;

                if (stack.getItem() == Items.ROTTEN_FLESH) {
                    score -= config.foodPickRottenFleshPenalty;
                }
                if (score > bestFoodScore) {
                    bestFoodScore = score;
                    bestFood = stack.getItem();
                }

                foodTotal += Objects.requireNonNull(ItemVer.getFoodComponent(stack.getItem())).getHunger() * stack.getCount();
            }
        }

        return new Pair<>(foodTotal, Optional.ofNullable(bestFood));
    }

    // If we need to eat like, NOW.
    public boolean needsToEatCritical() {
        return false;
    }

    public boolean hasFood() {
        return hasFood;
    }

    public void shouldStop(boolean shouldStopInput) {
        shouldStop = shouldStopInput;
    }

    public boolean isShouldStop() {
        return shouldStop;
    }

    static class FoodChainConfig {
        public int alwaysEatWhenWitherOrFireAndHealthBelow = 6;
        public int alwaysEatWhenBelowHunger = 10;
        public int alwaysEatWhenBelowHealth = 14;
        public int alwaysEatWhenBelowHungerAndPerfectFit = 20 - 5;
        public int prioritizeSaturationWhenBelowHealth = 8;
        public float foodPickPrioritizeSaturationSaturationMultiplier = 8;
        public float foodPickSaturationWastePenaltyMultiplier = 1;
        public float foodPickHungerWastePenaltyMultiplier = 2;
        public float foodPickHungerNotFilledPenaltyMultiplier = 1;
        public float foodPickRottenFleshPenalty = 100;
        public float runDontEatMaxHealth = 3;
        public int runDontEatMaxHunger = 3;
        public int canTankHitsAndEatArmor = 15;
        public int canTankHitsAndEatMaxHunger = 3;
    }
}
