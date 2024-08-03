package adris.altoclef.scanner.blacklist.spatial;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.scanner.blacklist.spatial.entry.ISpatialBlacklistType;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class BlockBlacklist {

    protected Map<BlockPos, BlacklistBlockEntry> blacklists = new HashMap<>();

    public void setBlacklist(AltoClef mod, BlockPos position, ISpatialBlacklistType<BlockPos> entry) {
        blacklists.put(position, new BlacklistBlockEntry(entry, System.currentTimeMillis(), mod.getWorld().getBlockState(position)));
    }

    public BlacklistBlockEntry getBlacklist(BlockPos position) {
        return blacklists.get(position);
    }

    // in sync for if the cache is cleared unexpectedly during this methods use
    private final Map<BlockPos, Integer> avoidScoreCache = new HashMap<>();
    public synchronized int getAvoidScore(BlockPos position) {
        if (avoidScoreCache.containsKey(position)) {
            return avoidScoreCache.get(position);
        } else { // Recalculate
            int avoidScore = 0;
            for (BlacklistBlockEntry entry : blacklists.values()) {
                avoidScore += entry.entry.avoidScore(position);
            }
            avoidScoreCache.put(position, avoidScore);
            return avoidScore;
        }
    }

    public synchronized void filterBlackListItems(BiFunction<BlockPos, BlacklistBlockEntry, Boolean> shouldRemoveFunc) {
        boolean changeOccurred = false;

        HashMap<BlockPos, BlacklistBlockEntry> blacklistsTemp = new HashMap<>(blacklists.size());
        for (Map.Entry<BlockPos, BlacklistBlockEntry> entry : blacklists.entrySet()) {
            final BlockPos pos = entry.getKey();
            final BlacklistBlockEntry entry2 = entry.getValue();

            if (shouldRemoveFunc.apply(pos, entry2)) { // Remove by not putting it into the new blacklist
                changeOccurred = true;
            } else {
                blacklistsTemp.put(pos, entry2);
            }
        }
        blacklists = blacklistsTemp;

        if (changeOccurred) {
            clearCache();
        }
    }

    public synchronized void clearBlacklists() {
        blacklists.clear();
        clearCache();
    }

    private synchronized void clearCache() {
        avoidScoreCache.clear();
    }

    public static class BlacklistBlockEntry {
        public final ISpatialBlacklistType<BlockPos> entry;
        public final long createTime;
        public final BlockState state;

        public BlacklistBlockEntry(ISpatialBlacklistType<BlockPos> type, long createTime, BlockState state) {
            this.entry = type;
            this.createTime = createTime;
            this.state = state;
        }

        @Override
        public String toString() {
            return "BlacklistBlock{" + "createTime=" + createTime + ", block=" + state.getBlock() + '}';
        }
    }

}
