package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.CustomBaritoneGoalTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.publicenums.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalRunAway;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;

public class DragonBreathTracker {
    private final HashSet<BlockPos> breathBlocks = new HashSet<>();

    public void updateBreath(AltoClef mod) {
        breathBlocks.clear();
        for (AreaEffectCloudEntity cloud : mod.getEntityTracker().getTrackedEntities(AreaEffectCloudEntity.class)) {
            for (BlockPos bad : WorldHelper.getBlocksTouchingBox(mod, cloud.getBoundingBox())) {
                breathBlocks.add(bad);
            }
        }
    }

    public boolean isTouchingDragonBreath(BlockPos pos) {
        return
                WorldHelper.getCurrentDimension() == Dimension.END && (
                        // Also check up and down because we could mine into it.
                        breathBlocks.contains(pos) ||
                        breathBlocks.contains(pos.up()) ||
                        breathBlocks.contains(pos.down())
                );
    }

    public Task getRunAwayTask() {
        return new RunAwayFromDragonsBreathTask();
    }

    private class RunAwayFromDragonsBreathTask extends CustomBaritoneGoalTask {

        @Override
        protected void onStart(AltoClef mod) {
            super.onStart(mod);
            mod.getBehaviour().push();
            mod.getBehaviour().setBlockPlacePenalty(Double.POSITIVE_INFINITY);
            // do NOT ever wander
            _checker = new MovementProgressChecker((int) Float.POSITIVE_INFINITY);
        }

        @Override
        protected void onStop(AltoClef mod, Task interruptTask) {
            super.onStop(mod, interruptTask);
            mod.getBehaviour().pop();
        }

        @Override
        protected Goal newGoal(AltoClef mod) {
            return new GoalRunAway(10, breathBlocks.toArray(BlockPos[]::new));
        }

        @Override
        protected boolean isEqual(Task other) {
            return other instanceof RunAwayFromDragonsBreathTask;
        }

        @Override
        protected String toDebugString() {
            return "ESCAPE Dragons Breath";
        }
    }
}
