package adris.altoclef.tasksystem.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.EscapeFromLavaTask;
import adris.altoclef.tasksystem.TaskRunner;
import baritone.api.utils.input.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

public class WorldSurvivalChain extends SingleTaskChain {

    private boolean _wasAvoidingDrowning;

    public WorldSurvivalChain(TaskRunner runner) {
        super(runner);
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {

    }

    @Override
    public float getPriority(AltoClef mod) {
        handleDrowning(mod);
        if (isInLavaOhShit(mod)) {
            setTask(new EscapeFromLavaTask());
            return 100;
        }
        return Float.NEGATIVE_INFINITY;
    }

    private void handleDrowning(AltoClef mod) {
        // Swim
        boolean avoidedDrowning = false;
        if (mod.getModSettings().shouldAvoidDrowning()) {
            if (!mod.getClientBaritone().getPathingBehavior().isPathing()) {
                if (mod.getPlayer().isTouchingWater() && mod.getPlayer().getAir() < mod.getPlayer().getMaxAir()) {
                    // Swim up!
                    MinecraftClient.getInstance().options.keyJump.setPressed(true);
                    //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.JUMP, true);
                    avoidedDrowning = true;
                    _wasAvoidingDrowning = true;
                }
            }
        }
        // Stop swimming up if we just swam.
        if (_wasAvoidingDrowning && !avoidedDrowning) {
            _wasAvoidingDrowning = false;
            MinecraftClient.getInstance().options.keyJump.setPressed(false);
            //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.JUMP, false);
        }
    }

    private boolean isInLavaOhShit(AltoClef mod) {
        return mod.getPlayer().isInLava() && !mod.getPlayer().hasStatusEffect(StatusEffects.FIRE_RESISTANCE);
    }

    @Override
    public String getName() {
        return "Misc World Survival Chain";
    }

    @Override
    public boolean isActive() {
        // Always check for survival.
        return true;
    }
}
