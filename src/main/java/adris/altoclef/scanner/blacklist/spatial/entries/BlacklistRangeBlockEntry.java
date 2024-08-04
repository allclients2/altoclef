package adris.altoclef.scanner.blacklist.spatial.entries;

import adris.altoclef.multiversion.MathUtilVer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public record BlacklistRangeBlockEntry(Vec3d avoidCenter, int maxScore, double maxRange) implements ISpatialBlacklistEntry<BlockPos> {
    @Override
    public int avoidScore(BlockPos pos) {
        if (avoidCenter.isInRange(MathUtilVer.getCenter(pos), maxRange)) {
            return (int) ((1 - (avoidCenter.distanceTo(MathUtilVer.getCenter(pos)) / maxRange)) * maxScore);
        } else {
            return 0;
        }
    }
}
