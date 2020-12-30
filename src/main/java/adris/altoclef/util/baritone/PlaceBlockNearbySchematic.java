package adris.altoclef.util.baritone;

import baritone.api.schematic.AbstractSchematic;
import baritone.api.schematic.ISchematic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class PlaceBlockNearbySchematic extends AbstractSchematic {

    private static final int RANGE = 10;

    private boolean _done;
    private final Block[] _blockToPlace;

    private BlockPos _targetPos;
    private BlockState _targetPlace;

    private BlockPos _origin;

    private boolean _skipIfAlreadyThere;

    public PlaceBlockNearbySchematic(BlockPos origin, Block[] blocksToPlace, boolean skipIfAlreadyThere) {
        super(RANGE, RANGE, RANGE);
        _origin = origin;
        _blockToPlace = blocksToPlace;
        _done = false;
        _targetPos = null;
        _targetPlace = null;
        _skipIfAlreadyThere = skipIfAlreadyThere;
    }
    public PlaceBlockNearbySchematic(BlockPos origin, Block[] blocksToPlace) {
        this(origin, blocksToPlace, true);
    }

    public PlaceBlockNearbySchematic(BlockPos origin, Block blockToPlace) {
        this(origin, new Block[] {blockToPlace});
    }

    public void reset() {
        _targetPos = null;
    }

    public boolean foundSpot() {
        return _targetPos != null;
    }
    public BlockPos getFoundSpot() {
        return _targetPos;
    }

    // No restrictions.
    //@Override
    //public boolean inSchematic(int x, int y, int z, BlockState currentState) {
    //    return true;
    //}

    @Override
    public BlockState desiredState(int x, int y, int z, BlockState blockState, List<BlockState> list) {
        // If a block already exists there, place it.
        if (_skipIfAlreadyThere && blockIsTarget(blockState.getBlock())) {
            System.out.println("PlaceBlockNearbySchematic (already exists)");
            _targetPlace = blockState;
            _targetPos = _origin.add(new BlockPos(x, y, z));
        }
        boolean isDone = (_targetPos != null);
        if (isDone) {
            if (_targetPos.getX() == x + _origin.getX() && _targetPos.getY() == y + _origin.getY() && _targetPos.getZ() == z + _origin.getZ()) {
                return _targetPlace;
            }
            return blockState;
        }
        //System.out.print("oof: [");
        for (BlockState possible : list) {
            //System.out.print(possible.getBlock().getTranslationKey() + " ");
            if (blockIsTarget(possible.getBlock())) {
                System.out.print("PlaceBlockNearbySchematic  ( FOUND! )");
                _targetPos = _origin.add(new BlockPos(x, y, z));
                _targetPlace = possible;
                return possible;
            }
        }
        //System.out.println("] ( :(((((( )");
        return blockState;
    }


    private boolean blockIsTarget(Block block) {
        for (Block check : _blockToPlace) {
            if (check.is(block)) return true;
        }
        return false;
    }
}
