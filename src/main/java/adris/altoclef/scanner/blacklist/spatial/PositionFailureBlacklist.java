package adris.altoclef.scanner.blacklist.spatial;

import adris.altoclef.scanner.blacklist.spatial.entry.IBlacklistPosEntry;

import java.util.HashMap;

/**
 * Sometimes we will try to access something and fail TOO many times.
 * <p>
 * This lets us know that a block is possible unreachable, so we increase its avoidance score.
 * Where `T` is the class for the position (BlockPos, Vec3d, Vec3f, etc...)
 */
public class PositionFailureBlacklist<T> extends PositionBlacklist<T> {

    // Position -> Failures
    private final HashMap<T, FailurePositionData> failureMap = new HashMap<>();

    public void positionFailed(T position, IBlacklistPosEntry<T> entry, int maxFailuresAllowed) {
        final FailurePositionData failureData;
        if (failureMap.containsKey(position)) {
            failureData = failureMap.get(position);
        } else {
            failureData = new FailurePositionData();
        }
        failureData.failureNum++;
        if (failureData.failureNum >= maxFailuresAllowed) {
            setBlacklist(position, entry);
        }
        failureMap.put(position, failureData);
    }

    private static class FailurePositionData {
        int failureNum = 0;
        int maxScore = 25;
    };
}
