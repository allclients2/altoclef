package adris.altoclef.multiversion;

import adris.altoclef.AltoClef;

// Some versions of baritone may not have `isCanWalkOnEndPortal` or other methods.
public class BaritoneVer {

    @Deprecated
    public static boolean isCanWalkOnEndPortal() {
        return isCanWalkOnEndPortal(AltoClef.INSTANCE);
    }

    public static boolean isCanWalkOnEndPortal(AltoClef mod) {
        try {
            return mod.getExtraBaritoneSettings().isCanWalkOnEndPortal();
        } catch (Exception ignored) {
            return false;
        }
    }

    @Deprecated
    public static void canWalkOnEndPortal(boolean value) {
        canWalkOnEndPortal(AltoClef.INSTANCE, value);
    }

    public static void canWalkOnEndPortal(AltoClef mod, boolean value) {
        try {
            mod.getExtraBaritoneSettings().canWalkOnEndPortal(value);
        } catch (Exception ignored) {}
    }
}
