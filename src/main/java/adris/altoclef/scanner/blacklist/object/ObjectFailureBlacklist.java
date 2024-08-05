package adris.altoclef.scanner.blacklist.object;

import java.util.HashMap;

/**
 * Sometimes we will try to access something and fail TOO many times.
 * <p>
 * This lets us know that an object is possible unreachable, so we blacklist them.
 * Where `T` is the class for the object
 */
public class ObjectFailureBlacklist<T> {

    // Position -> FailurePositionData
    private final HashMap<T, FailurePositionData> failureMap = new HashMap<>();

    public void objectFailed(T object, int maxFailuresAllowed) {
        final FailurePositionData failureData;
        if (failureMap.containsKey(object)) {
            failureData = failureMap.get(object);
        } else {
            failureData = new FailurePositionData();
        }
        failureData.failureNum++;
        if (failureData.failureNum >= maxFailuresAllowed) {
            failureData.blacklisted = true;
        }
        failureMap.put(object, failureData);
    }

    public boolean isBlacklisted(T object) {
        if (failureMap.containsKey(object)) {
            return failureMap.get(object).blacklisted;
        }
        return false;
    }

    public void clearBlacklists() {
        failureMap.clear();
    }

    private static class FailurePositionData {
        boolean blacklisted = false;
        int failureNum = 0;
    };
}
