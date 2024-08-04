package adris.altoclef.util.publicenums;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;


public enum Dimension {
    OVERWORLD(World.OVERWORLD),
    NETHER(World.NETHER),
    END(World.END);

    public final RegistryKey<World> registryKey;

    Dimension(RegistryKey<World> key) {
        registryKey = key;
    }

    public static Dimension dimensionFromWorldKey(RegistryKey<World> worldKey) {
        for (Dimension dimension : Dimension.values()) {
            if (worldKey.equals(dimension.registryKey)) { // how to compare?
                return dimension;
            }
        }
        return null;
    }
}