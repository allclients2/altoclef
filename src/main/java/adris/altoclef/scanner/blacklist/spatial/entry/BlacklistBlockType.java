package adris.altoclef.scanner.blacklist.spatial.entry;

import net.minecraft.util.math.BlockPos;

public record BlacklistBlockType(BlockPos avoidPos, int maxScore) implements ISpatialBlacklistType<BlockPos> {
    @Override
    public int avoidScore(BlockPos pos) {
        if (pos.equals(avoidPos)) {
            return maxScore;
        } else {
            return 0;
        }
    }
}
