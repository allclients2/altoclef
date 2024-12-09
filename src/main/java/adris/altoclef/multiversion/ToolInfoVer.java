package adris.altoclef.multiversion;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;

public class ToolInfoVer {
    public static float getMiningSpeed(Item item) {
        return item.getComponents().get(DataComponentTypes.TOOL).defaultMiningSpeed();
    }
}
