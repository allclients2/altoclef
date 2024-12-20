package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.multiversion.PlayerVer;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.BlockHelper;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class KillEnderDragonWithBedsTask extends Task {
    private final WaitForDragonAndPearlTask whenNotPerchingTask;
    TimerGame placeBedTimer = new TimerGame(0.6);
    TimerGame waiTimer = new TimerGame(0.3);
    TimerGame waitBeforePlaceTimer = new TimerGame(0.5);
    boolean waited = false;
    double prevDist = 100;
    private BlockPos endPortalTop;
    private Task freePortalTopTask = null;
    private Task placeObsidianTask = null;
    private boolean dragonDead = false;

    public KillEnderDragonWithBedsTask() {
        whenNotPerchingTask = new WaitForDragonAndPearlTask();
    }

    public static BlockPos locateExitPortalTop(AltoClef mod) {
        if (!mod.getChunkTracker().isChunkLoaded(new BlockPos(0, 64, 0))) return null;
        int height = WorldHelper.getGroundHeight(mod, 0, 0, Blocks.BEDROCK);
        if (height != -1) return new BlockPos(0, height, 0);
        return null;
    }

    @Override
    protected void onStart(AltoClef mod) {
        // do not block our view
        mod.getBehaviour().avoidBlockPlacing((pos) -> pos.getZ() == 0 && Math.abs(pos.getX()) < 5);
    }

    @Override
    protected Task onTick(AltoClef mod) {
        /*
            If dragon is perching:
                If we're not in position (XZ):
                    Get in position (XZ)
                If there's no bed:
                    If we can't "reach" the top of the pillar:
                        Jump
                    Place a bed
                If the dragon's head hitbox is close enough to the bed:
                    Right click the bed
            Else:
                // Perform "Default Wander" mode and avoid dragon breath.
         */
        if (endPortalTop == null) {
            endPortalTop = locateExitPortalTop(mod);
            if (endPortalTop != null) {
                whenNotPerchingTask.setExitPortalTop(endPortalTop);
            }
        }

        if (endPortalTop == null) {
            setDebugState("Searching for end portal top.");
            return new GetToXZTask(0, 0);
        }

        BlockPos obsidianTarget = endPortalTop.up().offset(Direction.NORTH);
        if (!mod.getWorld().getBlockState(obsidianTarget).getBlock().equals(Blocks.OBSIDIAN)) {
            if (WorldHelper.inRangeXZ(mod.getPlayer().getPos(), new Vec3d(0, 0, 0), 10)) {
                if (placeObsidianTask == null) {
                    placeObsidianTask = new PlaceBlockTask(obsidianTarget, Blocks.OBSIDIAN);
                }
                return placeObsidianTask;
            } else {
                return new GetToXZTask(0, 0);
            }
        }
        BlockState stateAtPortal = mod.getWorld().getBlockState(endPortalTop.up());
        if (!stateAtPortal.isAir() && !stateAtPortal.getBlock().equals(Blocks.FIRE) &&
                !Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.BED)).toList().contains(stateAtPortal.getBlock())) {

            if (freePortalTopTask == null) {
                freePortalTopTask = new DestroyBlockTask(endPortalTop.up());
            }
            return freePortalTopTask;
        }


        if (dragonDead) {
            setDebugState("Waiting for overworld portal to spawn.");
            return new GetToBlockTask(endPortalTop.down(4).west());
        }

        if (!mod.getEntityTracker().entityFound(EnderDragonEntity.class) || dragonDead) {
            setDebugState("No dragon found.");

            if (!WorldHelper.inRangeXZ(mod.getPlayer(), endPortalTop, 1)) {
                setDebugState("Going to end portal top at" + endPortalTop.toString() + ".");
                return new GetToBlockTask(endPortalTop);
            }
        }
        List<EnderDragonEntity> dragons = mod.getEntityTracker().getTrackedEntities(EnderDragonEntity.class);
        for (EnderDragonEntity dragon : dragons) {
            Phase dragonPhase = dragon.getPhaseManager().getCurrent();

            if (dragonPhase.getType() == PhaseType.DYING) {
                Debug.logMessage("Dragon is dead.");
                if (mod.getPlayer().getPitch(1) != -90) {
                    PlayerVer.setHeadPitch(mod, -90);
                }
                dragonDead = true;
                return null;
            }

            boolean perching = dragonPhase.getType() == PhaseType.LANDING || dragonPhase.isSittingOrHovering() || dragonPhase.getType() == PhaseType.LANDING_APPROACH;
            if (dragon.getY() < endPortalTop.getY() + 2) {
                // Dragon is already perched.
                perching = false;
            }
            Debug.logMessage(dragonPhase.getType() + " : " + dragonPhase.isSittingOrHovering());
            whenNotPerchingTask.setPerchState(perching);
            // When the dragon is not perching...
            if (whenNotPerchingTask.isActive() && !whenNotPerchingTask.isFinished(mod)) {
                setDebugState("Dragon not perching, performing special behavior...");
                return whenNotPerchingTask;
            }
            if (perching) {
                return performOneCycle(mod, dragon);
            }
        }
        mod.getFoodChain().shouldStop(false);
        // Start our "Not perching task"
        return whenNotPerchingTask;
    }

    private Task performOneCycle(AltoClef mod, EnderDragonEntity dragon) {
        mod.getFoodChain().shouldStop(true);
        if (mod.getInputControls().isHeldDown(Input.SNEAK)) {
            mod.getInputControls().release(Input.SNEAK);
        }
        // do not let shield fuck up our moment :3
        mod.getSlotHandler().forceEquipItemToOffhand(Items.AIR);

        BlockPos endPortalTop = KillEnderDragonWithBedsTask.locateExitPortalTop(mod).up();
        BlockPos obsidian = null;
        Direction dir = null;

        for (Direction direction : new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}) {
            if (mod.getWorld().getBlockState(endPortalTop.offset(direction)).getBlock().equals(Blocks.OBSIDIAN)) {
                obsidian = endPortalTop.offset(direction);
                dir = direction.getOpposite();
                break;
            }
        }

        if (dir == null) {
            Debug.logMessage("no obisidan? :(");
            return null;
        }

        Direction offsetDir = dir.getAxis() == Direction.Axis.X ? Direction.SOUTH : Direction.WEST;
        BlockPos targetBlock = endPortalTop.down(3).offset(offsetDir, 3).offset(dir);

        double d = distanceIgnoreY(WorldHelper.toVec3d(targetBlock), mod.getPlayer().getPos());
        if (d > 0.7 || mod.getPlayer().getBlockPos().down().getY() > endPortalTop.getY() - 4) {
            Debug.logMessage(d + "");
            return new GetToBlockTask(targetBlock);
        } else if (!waited) {
            waited = true;
            waitBeforePlaceTimer.reset();
        }
        if (!waitBeforePlaceTimer.elapsed()) {
            Debug.logMessage(waitBeforePlaceTimer.getDuration() + " waiting...");
            return null;
        }

        LookHelper.lookAt(mod, obsidian, dir);

        BlockPos bedHead = BlockHelper.getBedHead(mod, endPortalTop);
        mod.getSlotHandler().forceEquipItem(ItemHelper.BED);

        if (bedHead == null) {
            if (placeBedTimer.elapsed() && Math.abs(dragon.getY() - endPortalTop.getY()) < 10) {
                mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                waiTimer.reset();
            }
            return null;
        }
        if (!waiTimer.elapsed()) {
            return null;
        }

        // most of these numbers were arbitrarily added through some testing, its possible not all of these cases need to be tested
        // it seems to work fairly well tho, so I would rather not touch it :p
        Vec3d dragonHeadPos = dragon.head.getBoundingBox().getCenter();
        Vec3d bedHeadPos = WorldHelper.toVec3d(bedHead);

        double dist = dragonHeadPos.distanceTo(bedHeadPos);
        double distXZ = distanceIgnoreY(dragonHeadPos, bedHeadPos);

        EnderDragonPart body = dragon.getBodyParts()[2];

        double destroyDistance = Math.abs(body.getBoundingBox().getMin(Direction.Axis.Y) - bedHeadPos.getY());
        boolean tooClose = destroyDistance < 1.1;
        boolean skip = destroyDistance > 3 && dist > 4.5 && distXZ > 2.5;

        Debug.logMessage(destroyDistance + " : " + dist + " : " + distXZ);

        if ((dist < 1.5 || (prevDist < distXZ && destroyDistance < 4 && prevDist < 2.9)) || (destroyDistance < 2 && dist < 4)
                || (destroyDistance < 1.7 && dist < 4.5) || tooClose || (destroyDistance < 2.4 && distXZ < 3.7) || (destroyDistance < 3.5 && distXZ < 2.4)) {

            if (!skip) {
                mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                placeBedTimer.reset();
            }
        }

        prevDist = distXZ;
        return null;
    }

    public double distanceIgnoreY(Vec3d vec, Vec3d vec1) {
        double d = vec.x - vec1.x;
        double f = vec.z - vec1.z;
        return Math.sqrt(d * d + f * f);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getFoodChain().shouldStop(false);
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return super.isFinished(mod);
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof KillEnderDragonWithBedsTask;
    }

    @Override
    protected String toDebugString() {
        return "Bedding the Ender Dragon";
    }
}