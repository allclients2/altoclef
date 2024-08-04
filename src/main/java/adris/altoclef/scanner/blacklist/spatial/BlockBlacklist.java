package adris.altoclef.scanner.blacklist.spatial;

import adris.altoclef.Debug;
import adris.altoclef.scanner.blacklist.spatial.entries.ISpatialBlacklistEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.BiFunction;

// WARNING: This only supports ONE DIMENSION per instance of this class.
// Prefer to create a blacklist for each dimension.

public class BlockBlacklist {
    private final int maxBlacklistSize;
    protected final World blacklistWorld;
    protected LinkedHashMap<BlockPos, BlacklistBlockEntry> blacklists = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<BlockPos, BlacklistBlockEntry> eldest) {
            return size() > maxBlacklistSize;
        }
    };

    public BlockBlacklist(World world, int maxBlacklistSize) {
        this.blacklistWorld = world;
        this.maxBlacklistSize = maxBlacklistSize;
    }

    public void setBlacklist(BlockPos position, ISpatialBlacklistEntry<BlockPos> entry) {
        if (blacklistWorld != null)
            blacklists.put(position, new BlacklistBlockEntry(entry, System.currentTimeMillis(), blacklistWorld.getBlockState(position)));
    }

    public BlacklistBlockEntry getBlacklist(BlockPos position) {
        return blacklists.get(position);
    }

    // in sync for if the cache is cleared unexpectedly during this methods use
    public synchronized int getAvoidScore(BlockPos position) {
        int avoidScore = 0;
        for (BlacklistBlockEntry entry : blacklists.values()) {
            avoidScore += entry.entry.avoidScore(position);
        }
        return avoidScore;
    }

    public synchronized void filterBlackListItems(BiFunction<BlockPos, BlacklistBlockEntry, Boolean> shouldRemoveFunc) {
        blacklists.entrySet().removeIf(entry -> shouldRemoveFunc.apply(entry.getKey(), entry.getValue()));
    }

    public void updateBlacklist(long maxBlacklistTimeMillis) {
        //Debug.logMessage("blacklist size: " + blacklists.size() + " timeout: " + maxBlacklistTimeMillis);
        if (blacklistWorld != null)
            filterBlackListItems(
                    (blockPos, item) -> {
                        final Block block = blacklistWorld.getBlockState(blockPos).getBlock();
                        if (block != Blocks.VOID_AIR && item.state.getBlock() != block) { // VOID_AIR means it's just not rendered
                            return true; // clear out changed block states
                        }
                        return (item.createTime + maxBlacklistTimeMillis) < System.currentTimeMillis(); // clear out old blacklists
                    }
            );
    }

    public synchronized void clearBlacklists() {
        blacklists.clear();
    }


    public static class BlacklistBlockEntry {
        public final ISpatialBlacklistEntry<BlockPos> entry;
        public final long createTime;
        public final BlockState state;

        public BlacklistBlockEntry(ISpatialBlacklistEntry<BlockPos> type, long createTime, BlockState state) {
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