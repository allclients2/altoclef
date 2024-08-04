package adris.altoclef.scanner.blacklist.spatial.entries;

import net.minecraft.util.math.BlockPos;

public record BlacklistBlockEntry(BlockPos avoidPos, int maxScore) implements ISpatialBlacklistEntry<BlockPos> {
    @Override
    public int avoidScore(BlockPos pos) {
        if (pos.equals(avoidPos)) {
            return maxScore;
        } else {
            return 0;
        }
    }
}
