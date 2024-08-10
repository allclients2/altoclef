package adris.altoclef.tasks.construction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToYTask;
import adris.altoclef.tasks.inventory.EnsureFreeInventorySlotTask;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.Slot;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.entity.ItemEntity;

/*
 * Mining in a following structure:
                           X = Tunnel "Trunk"
                           B = Branch
                           S = Staircase
                           Y = Outpost (for supplies, etc.)
                           - = (Optional) 1×1 block tunnel
		                           
		          B-------B                 B-------B
		          B       B                 B       B
		          B       B                 B       B
		          B       B                 B       B
		          B       B                 B       B
		          B       B                 B       B
		          B       B       YYY       B       B
		X X X X X X X X X X X X X YYY X X X X X X X X X X X X
		          B       B       YYY       B       B
		          B       B        S        B       B
		          B       B        S        B       B
		          B       B        S        B       B
		          B       B        S        B       B
		          B-------B        S        B-------B
 */

public class BranchMiningTask extends Task implements ITaskRequiresGrounded {

    private final BlockPos startPos;
    private BlockPos checkpointPos = null;
    private final Direction startingDirection;
    private TunnelToMine prevTunnel = null;
    private final MovementProgressChecker progressChecker = new MovementProgressChecker();
    private final Task prepareForMiningTask = TaskCatalogue
            .getItemTask(new ItemTarget(Items.IRON_PICKAXE, 3));
    private int groundHeight = Integer.MIN_VALUE;
    private GetToYTask getToYTask = null;
    private DestroyBlockTask destroyOreTask = null;
    private GetToBlockTask getCloseToBlockTask = null;
    private final List<Block> blockTargets;
    private List<BlockPos> blocksToMine;
    private Block currentTarget;

    public BranchMiningTask(BlockPos homePos, Direction startingDirection, List<Block> blocksToMine) {
        if (!new HashSet<>(Arrays.asList(ItemHelper.ORES)).containsAll(blocksToMine))
            throw new IllegalStateException("Unexpected value: " + blocksToMine.toString() + ", expacted any of: " + ItemHelper.ORES.toString());
        startPos = homePos;
        this.startingDirection = startingDirection;
        blockTargets = blocksToMine;
    }

    public BranchMiningTask(BlockPos homePos, Direction startingDirection, Block blockToMine) {
        if (!Arrays.asList(ItemHelper.ORES).contains(blockToMine))
            throw new IllegalStateException("Unexpected value: " + blockToMine + ", expacted any of: " + ItemHelper.ORES.toString());
        startPos = homePos;
        this.startingDirection = startingDirection;
        List<Block> blockList = new ArrayList<>();
        blockList.add(blockToMine);
        blockTargets = blockList;
    }

    @Override
    protected void onStart(AltoClef mod) {
        // TODO Auto-generated method stub

    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            progressChecker.reset();
        }
        if (mod.getClientBaritone().getBuilderProcess().isActive()) {
            return null;
        }
        if (currentTarget != null) {
            Optional<ItemEntity> itemEntity = mod.getEntityTracker().getClosestItemDrop(ItemHelper.oreToDrop(currentTarget.asItem()));
            if (itemEntity.isPresent()) {
                setDebugState("Picking up the drop!");
                // Ensure our inventory is free if we're close
                boolean touching = mod.getEntityTracker().isCollidingWithPlayer(itemEntity.get());
                if (touching) {
                    if (mod.getItemStorage().getSlotsThatCanFitInPlayerInventory(itemEntity.get().getStack(), false).isEmpty()) {
                        return new EnsureFreeInventorySlotTask();
                    }
                } else {
                    setDebugState("Getting closer to the drop!");
                    getCloseToBlockTask = new GetToBlockTask(itemEntity.get().getBlockPos());
                }
            }
        }

        if (getCloseToBlockTask != null && destroyOreTask != null && !destroyOreTask.isActive() && !getCloseToBlockTask.isFinished(mod)) {
            return getCloseToBlockTask;
        } else if (destroyOreTask != null && destroyOreTask.isActive() && !destroyOreTask.isFinished(mod)) {
            setDebugState("Mining ores found!");
            return destroyOreTask;
        } else if (blocksToMine != null && !blocksToMine.isEmpty()) {
            do {
                BlockPos blockPos = blocksToMine.get(0);
                if (!WorldHelper.isAir(mod, blockPos)) {
                    currentTarget = mod.getWorld().getBlockState(blockPos).getBlock();
                    if (Math.sqrt(mod.getPlayer().getBlockPos().getSquaredDistance(blockPos)) > 5) {
                        setDebugState("Getting closer to ore found!");
                        getCloseToBlockTask = new GetToBlockTask(blockPos);
                        return getCloseToBlockTask;
                    }
                    destroyOreTask = new DestroyBlockTask(blockPos);
                    blocksToMine.remove(blockPos);
                    setDebugState("Mining ores found!");
                    return destroyOreTask;
                }
                blocksToMine.remove(blockPos);
            } while (!blocksToMine.isEmpty());
        }
        if (prepareForMiningTask.isActive() && !prepareForMiningTask.isFinished(mod) && isNewPickaxeRequired(mod)) {
            if (!progressChecker.check(mod)) {
                final BlockPos playerPos = mod.getPlayer().getBlockPos();
                if (playerPos.getY() > groundHeight) {
                    progressChecker.reset();
                }
                if (groundHeight == Integer.MIN_VALUE) {
                    groundHeight = WorldHelper.getGroundHeight(mod, playerPos.getX(), playerPos.getZ());
                    getToYTask = new GetToYTask(groundHeight + 4);
                }
                if (!(getToYTask.isActive() || !getToYTask.isFinished(mod))) {
                    groundHeight = Integer.MIN_VALUE;
                    getToYTask = null;
                }
                return getToYTask;
            }
            if (mod.getClientBaritone().getBuilderProcess().isActive()) {
                mod.getClientBaritone().getBuilderProcess().onLostControl();
            }
            return prepareForMiningTask;
        }
        if (!mod.getClientBaritone().getBuilderProcess().isActive()) {
            TunnelToMine tunnel;
            getCloseToBlockTask = null;


            if (prevTunnel != null && wasCleared(mod, prevTunnel)) {
                blocksToMine = getBlocksNextToTunnel(mod, prevTunnel);
            }

            if (checkpointPos != null) {
                BlockPos prevCheckpoint = checkpointPos;

                tunnel = new TunnelToMine(mod, prevCheckpoint, 2, 1, 3, startingDirection);
                if (prevTunnel != null && wasCleared(mod, prevTunnel) && wasCleared(mod, tunnel)) {
                    switch (startingDirection) {
                        case EAST, NORTH:
                        case WEST:
                            if (prevTunnel == null || prevTunnel.tunnelDirection == Direction.SOUTH)
                                tunnel = new TunnelToMine(mod, prevCheckpoint, 2, 1, 32, Direction.NORTH);
                            else {
                                tunnel = new TunnelToMine(mod, prevCheckpoint, 2, 1, 32, Direction.SOUTH);
                            }
                            break;
                        case SOUTH:
                            if (prevTunnel == null || prevTunnel.tunnelDirection == Direction.EAST)
                                tunnel = new TunnelToMine(mod, prevCheckpoint, 2, 1, 32, Direction.WEST);
                            else {
                                tunnel = new TunnelToMine(mod, prevCheckpoint, 2, 1, 32, Direction.EAST);
                            }
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + startingDirection);
                    }
                    if (prevTunnel != null) setDebugState("Mining " + tunnel.tunnelDirection + " branch!");

                    if (wasCleared(mod, tunnel)) {
                        switch (startingDirection) {
                            case EAST:
                                checkpointPos = prevCheckpoint.east(4);
                                break;
                            case WEST:
                                checkpointPos = prevCheckpoint.west(4);
                                break;
                            case NORTH:
                                checkpointPos = prevCheckpoint.north(4);
                                break;
                            case SOUTH:
                                checkpointPos = prevCheckpoint.south(4);
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + startingDirection);
                        }
                    }

                } else {
                    setDebugState("Mining main tunnel");
                    tunnel = new TunnelToMine(mod, prevCheckpoint, 2, 1, 3, startingDirection);
                }
                prevTunnel = tunnel;
            } else {
                switch (startingDirection) {
                    case EAST:
                        checkpointPos = startPos.east(4);
                        break;
                    case WEST:
                        checkpointPos = startPos.west(4);
                        break;
                    case NORTH:
                        checkpointPos = startPos.north(4);
                        break;
                    case SOUTH:
                        checkpointPos = startPos.south(4);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + startingDirection);
                }
                setDebugState("Mining main tunnel");
                tunnel = new TunnelToMine(mod, startPos, 2, 1, 5, startingDirection);
                prevTunnel = tunnel;
            }
            mod.getClientBaritone().getBuilderProcess().clearArea(tunnel.corner1, tunnel.corner2);
        } else if (!progressChecker.check(mod)) {
            progressChecker.reset();
            mod.getBehaviour().setBlockBreakAdditionalPenalty(3.0);
            mod.getClientBaritoneSettings().blockBreakAdditionalPenalty.value = 3.0;
            mod.getBehaviour().setBlockPlacePenalty(0.0);
            mod.getClientBaritoneSettings().blockPlacementPenalty.value = 0.0;
            mod.getClientBaritoneSettings().costHeuristic.value = 5.5;
        }
        return null;
    }

    protected static boolean isNewPickaxeRequired(AltoClef mod) {

        Item[] PICKAXES = new Item[]{
                Items.STONE_PICKAXE,
                Items.IRON_PICKAXE,
                Items.DIAMOND_PICKAXE,
                Items.NETHERITE_PICKAXE,
                Items.GOLDEN_PICKAXE};

        if (mod.getItemStorage().getSlotsWithItemScreen(
                Items.WOODEN_PICKAXE,
                Items.STONE_PICKAXE,
                Items.IRON_PICKAXE,
                Items.DIAMOND_PICKAXE,
                Items.NETHERITE_PICKAXE,
                Items.GOLDEN_PICKAXE
        ).isEmpty()) {
            return true;
        }

        for (Item pickaxe : PICKAXES) {
            if (!mod.getItemStorage().getSlotsWithItemScreen(pickaxe).isEmpty()) {
                for (Slot slot : mod.getItemStorage().getSlotsWithItemScreen(pickaxe)) {
                    if (
                            mod.getItemStorage().getItemStacksPlayerInventory(false).get(
                                    slot.getInventorySlot()
                            ).getDamage() < (pickaxe.getDefaultStack().getMaxDamage() * 0.4)
                    ) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    private boolean wasCleared(AltoClef mod, TunnelToMine tunnel) {
        int x1 = tunnel.corner1.getX();
        int y1 = tunnel.corner1.getY();
        int z1 = tunnel.corner1.getZ();

        int x2 = tunnel.corner2.getX();
        int y2 = tunnel.corner2.getY();
        int z2 = tunnel.corner2.getZ();

        // Swap coordinates if necessary to make sure x1 <= x2, y1 <= y2, and z1 <= z2
        if (x1 > x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 > y2) {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }
        if (z1 > z2) {
            int temp = z1;
            z1 = z2;
            z2 = temp;
        }

        // Check each block between pos1 and pos2 for air
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
//                	BlockState state = MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, y, z));
                    if (!WorldHelper.isAir(mod, new BlockPos(x, y, z))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private List<BlockPos> getBlocksNextToTunnel(AltoClef mod, TunnelToMine tunnel) {
        int x1 = tunnel.corner1.getX();
        int y1 = tunnel.corner1.getY();
        int z1 = tunnel.corner1.getZ();

        int x2 = tunnel.corner2.getX();
        int y2 = tunnel.corner2.getY();
        int z2 = tunnel.corner2.getZ();
        List<BlockPos> vain = new ArrayList<>();
        // Swap coordinates if necessary to make sure x1 <= x2, y1 <= y2, and z1 <= z2
        if (x1 > x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 > y2) {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }
        if (z1 > z2) {
            int temp = z1;
            z1 = z2;
            z2 = temp;
        }

        // Check each block between pos1 and pos2 for air
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    BlockPos currentBlock = new BlockPos(x, y, z);
                    for (Direction dir : Direction.values()) {
                        BlockPos blockPosToCheck = currentBlock.offset(dir);
                        Block blockToCheck = mod.getWorld().getBlockState(blockPosToCheck).getBlock();
                        if (
                                blockTargets.contains(blockToCheck)
                                        && !vain.contains(blockPosToCheck)
                                        && !mod.getBlockScanner().isUnreachable(blockPosToCheck)
                                        && WorldHelper.canBreak(mod, blockPosToCheck)
                        )
                        {
                            vain.add(blockPosToCheck);
                            vain.addAll(findAdjacentBlocksOfSameType(mod, blockPosToCheck, blockToCheck, vain));
                        }
                    }
                }
            }
        }

        return vain;
    }

    private List<BlockPos> findAdjacentBlocksOfSameType(AltoClef mod, BlockPos startPos, Block targetBlock, List<BlockPos> currVain) {
        List<BlockPos> connectedBlocks = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        BlockState targetBlockState = mod.getWorld().getBlockState(startPos);

        queue.add(startPos);
        visited.add(startPos);
        queue.addAll(currVain);
        visited.addAll(currVain);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            connectedBlocks.add(currentPos);

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = currentPos.offset(direction);
                if (visited.contains(neighborPos)) {
                    continue; // Skip already visited positions
                }

                BlockState neighborBlockState = mod.getWorld().getBlockState(neighborPos);
                if (neighborBlockState.getBlock() == targetBlockState.getBlock()
                        && !mod.getBlockScanner().isUnreachable(neighborPos)
                        && WorldHelper.canBreak(mod, neighborPos)
                ) {
                    queue.add(neighborPos);
                    visited.add(neighborPos);
                }
            }
        }

        return connectedBlocks;
    }


    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getClientBaritone().getBuilderProcess().onLostControl();
        mod.getClientBaritoneSettings().costHeuristic.reset();
        mod.getClientBaritoneSettings().blockPlacementPenalty.reset();
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof BranchMiningTask task) {
            return (task.startPos.equals(startPos));
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Branch mining! Y=" + startPos.getY();
    }

}

class TunnelToMine {
    public final BlockPos corner1;
    public final BlockPos corner2;
    public final Direction tunnelDirection;

    public TunnelToMine(AltoClef mod, BlockPos startPos, int height, int width, int depth, Direction enumFacing) {
        height--;
        width--;
        tunnelDirection = enumFacing;
        int addition = ((width % 2 == 0) ? 0 : 1);
        switch (enumFacing) {
            case EAST:
                corner1 = new BlockPos(startPos.getX(), startPos.getY(), startPos.getZ() - width / 2);
//	            corner2 = new BlockPos(startPos.getX() + depth, startPos.getY() + height, startPos.getZ() + width / 2 + addition);
                corner2 = getSafeTunnelCorner2(
                        mod, corner1,
                        new BlockPos(startPos.getX() + depth, startPos.getY() + height, startPos.getZ() + width / 2 + addition),
                        enumFacing
                );
                break;
            case WEST:
                corner1 = new BlockPos(startPos.getX(), startPos.getY(), startPos.getZ() + width / 2 + addition);
//	            corner2 = new BlockPos(startPos.getX() - depth, startPos.getY() + height, startPos.getZ() - width / 2);
                corner2 = getSafeTunnelCorner2(
                        mod, corner1,
                        new BlockPos(startPos.getX() - depth, startPos.getY() + height, startPos.getZ() - width / 2),
                        enumFacing
                );
                break;
            case NORTH:
                corner1 = new BlockPos(startPos.getX() - width / 2, startPos.getY(), startPos.getZ());
//	            corner2 = new BlockPos(startPos.getX() + width / 2 + addition, startPos.getY() + height, startPos.getZ() - depth);
                corner2 = getSafeTunnelCorner2(
                        mod, corner1,
                        new BlockPos(startPos.getX() + width / 2 + addition, startPos.getY() + height, startPos.getZ() - depth),
                        enumFacing
                );
                break;
            case SOUTH:
                corner1 = new BlockPos(startPos.getX() + width / 2 + addition, startPos.getY(), startPos.getZ());
//	            corner2 = new BlockPos(startPos.getX() - width / 2, startPos.getY() + height, startPos.getZ() + depth);
                corner2 = getSafeTunnelCorner2(
                        mod, corner1,
                        new BlockPos(startPos.getX() - width / 2, startPos.getY() + height, startPos.getZ() + depth),
                        enumFacing
                );
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + enumFacing);
        }
    }

    private static BlockPos getSafeTunnelCorner2(AltoClef mod, BlockPos corner1, BlockPos corner2, Direction direction) {
        int x1 = corner1.getX();
        int y1 = corner1.getY();
        int z1 = corner1.getZ();

        int x2 = corner2.getX();
        int y2 = corner2.getY();
        int z2 = corner2.getZ();

        double closestDistance = Double.MAX_VALUE;
        BlockPos closest = corner2;

        // Swap coordinates if necessary to make sure x1 <= x2, y1 <= y2, and z1 <= z2
        if (x1 > x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 > y2) {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }
        if (z1 > z2) {
            int temp = z1;
            z1 = z2;
            z2 = temp;
        }
        // Check each block between pos1 and pos2
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    for (Direction neighborDirection : Direction.values()) {
                        BlockPos neighborPos = pos.offset(neighborDirection);
                        if (mod.getWorld().getBlockState(neighborPos).getBlock() instanceof FluidBlock) {
                            double distance = pos.getSquaredDistance(corner1);
                            if (distance < closestDistance) {
                                closest = pos.offset(direction.getOpposite());
                                closestDistance = distance;
                            }
                        } else if (mod.getWorld().getBlockState(neighborPos).getBlock() instanceof FallingBlock) {
                            for (int i = 0; i < mod.getWorld().getHeight(); i++) {
                                if (mod.getWorld().getBlockState(neighborPos).getBlock() instanceof FallingBlock)
                                    continue;
                                if (!(mod.getWorld().getBlockState(neighborPos.up(i)).getBlock() instanceof FluidBlock))
                                    break;
                                if (mod.getWorld().getBlockState(neighborPos.up(i)).getBlock() instanceof FluidBlock) {
                                    double distance = pos.getSquaredDistance(corner1);
                                    if (distance < closestDistance) {
                                        closest = pos.offset(direction.getOpposite());
                                        closestDistance = distance;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return closest;
    }
}

