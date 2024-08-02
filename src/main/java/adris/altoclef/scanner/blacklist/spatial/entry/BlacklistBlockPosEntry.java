package adris.altoclef.scanner.blacklist.spatial.entry;

import net.minecraft.util.math.BlockPos;

public record BlacklistBlockPosEntry(BlockPos avoidPos, int maxScore) implements IBlacklistPosEntry<BlockPos> {
    @Override
    public int avoidScore(BlockPos pos) {
        if (pos.equals(avoidPos)) {
            return maxScore;
        } else {
            return 0;
        }
    }
}
