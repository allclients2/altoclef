package adris.altoclef.scanner.blacklist.spatial;

import adris.altoclef.AltoClef;
import adris.altoclef.scanner.blacklist.spatial.entry.ISpatialBlacklistType;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

/**
 * Sometimes we will try to access something and fail TOO many times.
 * <p>
 * This lets us know that a block is possible unreachable, so we increase its avoidance score.
 * Where `T` is the class for the position (BlockPos, Vec3d, Vec3f, etc...)
 */
public class BlockFailureBlacklist extends BlockBlacklist {

    // Position -> Failures
    private final HashMap<BlockPos, FailurePositionData> failureMap = new HashMap<>();

    public void positionFailed(AltoClef mod, BlockPos position, ISpatialBlacklistType<BlockPos> entry, int maxFailuresAllowed) {
        final FailurePositionData failureData;
        if (failureMap.containsKey(position)) {
            failureData = failureMap.get(position);
        } else {
            failureData = new FailurePositionData();
        }
        failureData.failureNum++;
        if (failureData.failureNum >= maxFailuresAllowed) {
            setBlacklist(mod, position, entry);
        }
        failureMap.put(position, failureData);
    }

    private static class FailurePositionData {
        int failureNum = 0;
        int maxScore = 25;
    };
}
