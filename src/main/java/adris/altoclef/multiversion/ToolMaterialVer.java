package adris.altoclef.multiversion;

import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;

public class ToolMaterialVer {

    public static int getMiningLevel(ToolItem item) {
        return getMiningLevel(item.getMaterial());
    }

    public static int getMiningLevel(ToolMaterial material) {
        return switch (material) {
            case ToolMaterials.WOOD, ToolMaterials.GOLD -> 0;
            case ToolMaterials.STONE -> 1;
            case ToolMaterials.IRON -> 2;
            case ToolMaterials.DIAMOND -> 3;
            case ToolMaterials.NETHERITE -> 4;
            default -> throw new IllegalStateException("Unexpected material: " + material);
        };
    }

}
