package adris.altoclef.multiversion;

import adris.altoclef.util.slots.PlayerSlot;
import net.minecraft.block.Block;

import java.util.Optional;

public class BlockVer {

    public static float getHardness(Block block) {
        //#if MC >= 11800
        return block.getHardness();
        //#else
        //$$ return block.getDefaultState().getHardness(null, null); // Pass null because the arguments aren't even used...
        //#endif
    }
}
