package adris.altoclef.scanner;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.multiversion.MathUtilVer;
import adris.altoclef.scanner.blacklist.spatial.entries.BlacklistRangeBlockEntry;
import adris.altoclef.util.math.ArrayUtil;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

/**
 * Finds dangerous blocks and adds a blacklist to them.
 */
@SuppressWarnings("rawtypes")
public class DangerousBlockScanner {

    private static final boolean LOG = false;

    // Dangerous blocks
    private static final BlockBlacklist[] BLOCK_BLACKLISTS = new BlockBlacklist[] {
            //#if MC>=11900
            // Ancient city
            new BlockBlacklist(90, 35, new Block[] {
                    Blocks.SCULK_CATALYST, Blocks.SCULK_SENSOR, Blocks.SCULK_SHRIEKER
            }),
            //#endif

            //#if MC>=12100
            // Trail chamber
            new BlockBlacklist(60, 40, new Block[] {
                Blocks.TRIAL_SPAWNER, Blocks.VAULT
            }),
            //#endif
    };

    private record BlockBlacklist(int maxScore, int maxRange, Block[] blocks) {};

    private final TimerGame updateTimer = new TimerGame(1.5);

    private final AltoClef mod;
    public DangerousBlockScanner(AltoClef mod) {
        this.mod = mod;
    }

    public void scanDangerBlocks() {
        if (LOG) Debug.logInternal("Blacklisting dangerous blocks...");

        final BlockScanner blockScanner = mod.getBlockScanner();
        for (BlockBlacklist blacklist : BLOCK_BLACKLISTS) {
            blockScanner.getKnownLocations(Integer.MAX_VALUE, blacklist.blocks).forEach(
                    blockPos -> {
                        final Block block = mod.getWorld().getBlockState(blockPos).getBlock();
                        if (ArrayUtil.contains(blacklist.blocks, block)) { //TODO: Optimize this?
                            blockScanner.directBlacklist(
                                    blockPos,
                                    new BlacklistRangeBlockEntry(
                                            MathUtilVer.getCenter(blockPos),
                                            blacklist.maxScore,
                                            blacklist.maxRange
                                    )
                            );
                        }
                    }
            );
        }
    }


    public void tick() {
        if (mod.getModSettings().isShouldAvoidDangerousGoals() && updateTimer.elapsed() && AltoClef.inGame()) {
            updateTimer.reset();

            if (LOG) {
                Debug.logInternal("start Dbs scan ...");
                final long startTime = System.currentTimeMillis();
                scanDangerBlocks();
                Debug.logInternal("Dbs rescan time: " + (System.currentTimeMillis() - startTime) + " ms");
            } else {
                scanDangerBlocks();
            }
        }
    }
}
