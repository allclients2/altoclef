package adris.altoclef.multiversion;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class MathUtilVer {

    public static final float PI = 3.1415926535F;

    public static double getDistance(BlockPos blockPos, Vec3d vec3d) {
        return Math.sqrt(getDistanceSquared(blockPos, vec3d));
    }
    public static double getDistanceSquared(BlockPos blockPos, Vec3d vec3d) {
        return vec3d.distanceTo(new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5));
    }

    public static Vec3i viAdd(Vec3i a, Vec3i b) {
        return new Vec3i(a.getX() + b.getX(), a.getY() + b.getY(), a.getZ() + b.getZ());
    }

    public static Vec3d getCenter(BlockPos blockPos) {
        return Vec3d.ofCenter(blockPos);
    }

    public static Direction dirFromVec(int x, int y, int z) {
        //#if MC >= 12103
        return Direction.fromVector(x, y, z, null);
        //#else
        //$$ return Direction.fromVector(x, y, z);
        //#endif
    }
}
