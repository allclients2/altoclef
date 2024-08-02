package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class BlockHelper {
    public static boolean isSourceBlock(AltoClef mod, BlockPos pos, boolean onlyAcceptStill) {
        BlockState s = mod.getWorld().getBlockState(pos);
        if (s.getBlock() instanceof FluidBlock) {
            // Only accept still fluids.
            if (!s.getFluidState().isStill() && onlyAcceptStill) return false;
            int level = s.getFluidState().getLevel();
            // Ignore if there's liquid above, we can't tell if it's a source block or not.
            BlockState above = mod.getWorld().getBlockState(pos.up());
            if (above.getBlock() instanceof FluidBlock) return false;
            return level == 8;
        }
        return false;
    }

    /**
     * WARNING: this method checks if the block at the given position is a SOLID BLOCK
     * things like ice, dirtPaths, soulSand... don't count into this
     * if you just want to check if a block is solid use `BlockState.isSolid()`
     * (which includes more variety of blocks including the mentioned ones, signs, pressure plates...)
     * <p>
     * better method for blocks that can be walked on should be created instead
     */
    public static boolean isSolidBlock(AltoClef mod, BlockPos pos) {
        return mod.getWorld().getBlockState(pos).isSolidBlock(mod.getWorld(), pos);
    }

    /**
     * Get the "head" of a block with a bed, if the block is a bed.
     */
    public static BlockPos getBedHead(AltoClef mod, BlockPos posWithBed) {
        BlockState state = mod.getWorld().getBlockState(posWithBed);
        if (state.getBlock() instanceof BedBlock) {
            Direction facing = state.get(BedBlock.FACING);
            if (mod.getWorld().getBlockState(posWithBed).get(BedBlock.PART).equals(BedPart.HEAD)) {
                return posWithBed;
            }
            return posWithBed.offset(facing);
        }
        return null;
    }

    /**
     * Get the "foot" of a block with a bed, if the block is a bed.
     */
    public static BlockPos getBedFoot(AltoClef mod, BlockPos posWithBed) {
        BlockState state = mod.getWorld().getBlockState(posWithBed);
        if (state.getBlock() instanceof BedBlock) {
            Direction facing = state.get(BedBlock.FACING);
            if (mod.getWorld().getBlockState(posWithBed).get(BedBlock.PART).equals(BedPart.FOOT)) {
                return posWithBed;
            }
            return posWithBed.offset(facing.getOpposite());
        }
        return null;
    }

    // Get the left side of a chest, given a block pos.
    // Used to consistently identify whether a double chest is part of the same chest.
    public static BlockPos getChestLeft(AltoClef mod, BlockPos posWithChest) {
        BlockState state = mod.getWorld().getBlockState(posWithChest);
        if (state.getBlock() instanceof ChestBlock) {
            ChestType type = state.get(ChestBlock.CHEST_TYPE);
            if (type == ChestType.SINGLE || type == ChestType.LEFT) {
                return posWithChest;
            }
            Direction facing = state.get(ChestBlock.FACING);
            return posWithChest.offset(facing.rotateYCounterclockwise());
        }
        return null;
    }

    public static boolean isChestBig(AltoClef mod, BlockPos posWithChest) {
        BlockState state = mod.getWorld().getBlockState(posWithChest);
        if (state.getBlock() instanceof ChestBlock) {
            ChestType type = state.get(ChestBlock.CHEST_TYPE);
            return (type == ChestType.RIGHT || type == ChestType.LEFT);
        }
        return false;
    }

    public static boolean isBlockPosLoaded(World world, BlockPos pos) {
        return world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4);
    }

}


