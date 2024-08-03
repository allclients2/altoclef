package adris.altoclef.scanner;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.scanner.blacklist.spatial.entry.BlacklistRangeBlockType;
import adris.altoclef.util.math.ArrayUtil;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.Arrays;

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
            new BlockBlacklist(120, 35, new Block[] {
                    Blocks.SCULK, Blocks.SCULK_VEIN, Blocks.SCULK_CATALYST,
                    Blocks.SCULK_SENSOR, Blocks.SCULK_SHRIEKER, Blocks.DEEPSLATE_TILE_STAIRS
            }),
            //#endif

            //#if MC>=12100
            // Trail chamber
            new BlockBlacklist(80, 23, new Block[] {
                Blocks.TRIAL_SPAWNER,
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
        Debug.logInternal("Blacklisting dangerous blocks...");
        final BlockScanner blockScanner = mod.getBlockScanner();
        for (BlockBlacklist blacklist : BLOCK_BLACKLISTS) {
            blockScanner.getKnownLocations(Integer.MAX_VALUE, blacklist.blocks).forEach(
                    blockPos -> {
                        final Block block = mod.getWorld().getBlockState(blockPos).getBlock();
                        if (ArrayUtil.contains(blacklist.blocks, block)) { //TODO: Optimize this?
                            blockScanner.directBlacklist(
                                    blockPos,
                                    new BlacklistRangeBlockType(
                                            blockPos.toCenterPos(),
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
        if (updateTimer.elapsed() && AltoClef.inGame()) {
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
