package adris.altoclef.scanner.blacklist.spatial.entry;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public record BlacklistRangeBlockPosEntry(Vec3d avoidCenter, int maxScore, double maxRange) implements IBlacklistPosEntry<BlockPos> {
    @Override
    public int avoidScore(BlockPos pos) {
        if (avoidCenter.isInRange(pos.toCenterPos(), maxRange)) {
            return (int) ((1 - (avoidCenter.distanceTo(pos.toCenterPos()) / maxRange)) * maxScore);
        } else {
            return 0;
        }
    }
}
