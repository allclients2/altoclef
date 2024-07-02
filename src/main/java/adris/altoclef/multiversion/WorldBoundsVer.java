package adris.altoclef.multiversion;

public class WorldBoundsVer {
    public static final int WORLD_CEILING_Y = 255;

    // God bless 1.18!!
    //#if MC>=11800
    public static final int WORLD_FLOOR_Y = -64;
    //#else
    //$$ public static final int WORLD_FLOOR_Y = 0;
    //#endif
}
