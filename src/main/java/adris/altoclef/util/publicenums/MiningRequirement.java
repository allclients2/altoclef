package adris.altoclef.util.publicenums;

import adris.altoclef.Debug;
import adris.altoclef.multiversion.ItemVer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum MiningRequirement implements Comparable<MiningRequirement> {
    HAND(Items.AIR),
    WOOD(Items.WOODEN_PICKAXE),
    STONE(Items.STONE_PICKAXE),
    IRON(Items.IRON_PICKAXE),
    DIAMOND(Items.DIAMOND_PICKAXE);

    private final Item _minPickaxe;

    MiningRequirement(Item minPickaxe) {
        _minPickaxe = minPickaxe;
    }

    // FIXME this doesnt work for cobwebs because they are broken with shears...
    public static MiningRequirement getMinimumRequirementForBlock(Block block) {
        if (block.getDefaultState().isToolRequired()) {
            for (MiningRequirement req : MiningRequirement.values()) {
                if (req == MiningRequirement.HAND) continue;
                Item pick = req.getMinimumPickaxe();
                if (ItemVer.isSuitableFor(pick, block.getDefaultState())) {
                    return req;
                }
            }
            if (block == Blocks.COBWEB) {
                return MiningRequirement.HAND;
            }
            Debug.logWarning("Failed to find ANY effective tool against: " + block + ". I assume netherite is not required anywhere, so something else probably went wrong.");
            return MiningRequirement.DIAMOND;
        }
        return MiningRequirement.HAND;
    }

    public Item getMinimumPickaxe() {
        return _minPickaxe;
    }

}
