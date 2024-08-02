package adris.altoclef.scanner;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.events.BlockBreakingEvent;
import adris.altoclef.scanner.blacklist.spatial.entry.BlacklistRangeBlockPosEntry;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

/**
 * Finds dangerous blocks and adds a blacklist to them.
 */
@SuppressWarnings("rawtypes")
public class DangerousBlockScanner {

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

    private final TimerGame scanTimer = new TimerGame(3.5);

    private final AltoClef mod;
    public DangerousBlockScanner(AltoClef mod) {
        this.mod = mod;
    }

    public void tick() {
        if (scanTimer.elapsed() && mod.getTaskRunner().isActive() && AltoClef.inGame()) {
            scanTimer.reset();

            Debug.logInternal("Blacklisting dangerous blocks...");

            final BlockScanner blockScanner = mod.getBlockScanner();
            for (BlockBlacklist blacklist : BLOCK_BLACKLISTS) {
                blockScanner.getKnownLocations(Integer.MAX_VALUE, blacklist.blocks).forEach(
                    blockPos -> blockScanner.directBlacklist(
                        blockPos,
                        new BlacklistRangeBlockPosEntry(
                            blockPos.toCenterPos(),
                            blacklist.maxScore,
                            blacklist.maxRange
                        )
                    )
                );
            }
        }
    }
}
