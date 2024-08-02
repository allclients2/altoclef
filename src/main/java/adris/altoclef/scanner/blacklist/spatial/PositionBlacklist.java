package adris.altoclef.scanner.blacklist.spatial;

import adris.altoclef.scanner.blacklist.spatial.entry.IBlacklistPosEntry;
import adris.altoclef.util.math.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PositionBlacklist<T> {

    protected final HashMap<T, Pair<Long, IBlacklistPosEntry<T>>> blacklists = new HashMap<>();
    private final HashMap<T, Integer> avoidScoreCache = new HashMap<>();

    public void setBlacklist(T position, IBlacklistPosEntry<T> entry) {
        blacklists.put(position, new Pair<>(System.currentTimeMillis(), entry));
        clearCache();
    }

    public Pair<Long, IBlacklistPosEntry<T>> getBlacklist(T position) {
        return blacklists.get(position);
    }

    public int getAvoidScore(T position) {
        if (avoidScoreCache.containsKey(position)) {
            return avoidScoreCache.get(position);
        } else { // Recalculate

            int avoidScore = 0;
            for (Pair<Long, IBlacklistPosEntry<T>> entry : blacklists.values()) {
                avoidScore += entry.getRight().avoidScore(position);
            }

            avoidScoreCache.put(position, avoidScore);
            return avoidScore;
        }
    }

    public void clearBlacklists() {
        blacklists.clear();
        clearCache();
    }

    public void clearOldBlacklists(long maxTimeMillis) {
        Iterator<Map.Entry<T, Pair<Long, IBlacklistPosEntry<T>>>> iterator = blacklists.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<T, Pair<Long, IBlacklistPosEntry<T>>> entry = iterator.next();
            long creationTime = entry.getValue().getLeft();

            if ((creationTime + maxTimeMillis) < System.currentTimeMillis()) {
                iterator.remove();
            }
        }
        clearCache();
    }

    protected void clearCache() {
        avoidScoreCache.clear();
    }
}