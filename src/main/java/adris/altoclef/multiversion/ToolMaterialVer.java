package adris.altoclef.multiversion;

import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;

public class ToolMaterialVer {

    public static int getMiningLevel(ToolItem item) {
        return getMiningLevel(item.getMaterial());
    }

    public static int getMiningLevel(ToolMaterial material) {
        return switch ((ToolMaterials) material) {
            case WOOD, GOLD -> 0;
            case STONE -> 1;
            case IRON -> 2;
            case DIAMOND -> 3;
            case NETHERITE -> 4;
        };
    }

}
