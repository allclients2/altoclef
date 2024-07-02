package adris.altoclef.multiversion;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.HashMap;
import java.util.Map;

public class BiomeVer {

    // 1.18 biomes and pre-1.18 biomes
    private enum BiomeType {
        BADLANDS,
        BAMBOO_JUNGLE,
        BASALT_DELTAS,
        BEACH,
        BIRCH_FOREST,
        COLD_OCEAN,
        CRIMSON_FOREST,
        DARK_FOREST,
        DEEP_COLD_OCEAN,
        DEEP_FROZEN_OCEAN,
        DEEP_LUKEWARM_OCEAN,
        DEEP_OCEAN,
        DESERT,
        DRIPSTONE_CAVES,
        END_BARRENS,
        END_HIGHLANDS,
        END_MIDLANDS,
        ERODED_BADLANDS,
        FLOWER_FOREST,
        FOREST,
        FROZEN_OCEAN,
        FROZEN_PEAKS,
        FROZEN_RIVER,
        GROVE,
        ICE_SPIKES,
        JAGGED_PEAKS,
        JUNGLE,
        LUKEWARM_OCEAN,
        LUSH_CAVES,
        MEADOW,
        MUSHROOM_FIELDS,
        NETHER_WASTES,
        OCEAN,
        OLD_GROWTH_BIRCH_FOREST,
        OLD_GROWTH_PINE_TAIGA,
        OLD_GROWTH_SPRUCE_TAIGA,
        PLAINS,
        RIVER,
        SAVANNA,
        SAVANNA_PLATEAU,
        SMALL_END_ISLANDS,
        SNOWY_BEACH,
        SNOWY_PLAINS,
        SNOWY_SLOPES,
        SNOWY_TAIGA,
        SOUL_SAND_VALLEY,
        SPARSE_JUNGLE,
        STONY_PEAKS,
        STONY_SHORE,
        SUNFLOWER_PLAINS,
        SWAMP,
        TAIGA,
        THE_END,
        THE_VOID,
        WARM_OCEAN,
        WARPED_FOREST,
        WINDSWEPT_FOREST,
        WINDSWEPT_GRAVELLY_HILLS,
        WINDSWEPT_HILLS,
        WINDSWEPT_SAVANNA,
        WOODED_BADLANDS,
    }

    private static final Map<RegistryKey<Biome>, BiomeType> versionMap = new HashMap<>() {
        {
            put(BiomeKeys.BADLANDS, BiomeType.BADLANDS);
            put(BiomeKeys.BAMBOO_JUNGLE, BiomeType.BAMBOO_JUNGLE);
            put(BiomeKeys.BASALT_DELTAS, BiomeType.BASALT_DELTAS);
            put(BiomeKeys.BEACH, BiomeType.BEACH);
            put(BiomeKeys.BIRCH_FOREST, BiomeType.BIRCH_FOREST);
            put(BiomeKeys.COLD_OCEAN, BiomeType.COLD_OCEAN);
            put(BiomeKeys.CRIMSON_FOREST, BiomeType.CRIMSON_FOREST);
            put(BiomeKeys.DARK_FOREST, BiomeType.DARK_FOREST);
            put(BiomeKeys.DEEP_COLD_OCEAN, BiomeType.DEEP_COLD_OCEAN);
            put(BiomeKeys.DEEP_FROZEN_OCEAN, BiomeType.DEEP_FROZEN_OCEAN);
            put(BiomeKeys.DEEP_LUKEWARM_OCEAN, BiomeType.DEEP_LUKEWARM_OCEAN);
            put(BiomeKeys.DEEP_OCEAN, BiomeType.DEEP_OCEAN);
            put(BiomeKeys.DESERT, BiomeType.DESERT);
            put(BiomeKeys.END_HIGHLANDS, BiomeType.END_HIGHLANDS);
            put(BiomeKeys.END_MIDLANDS, BiomeType.END_MIDLANDS);
            put(BiomeKeys.ERODED_BADLANDS, BiomeType.ERODED_BADLANDS);
            put(BiomeKeys.FLOWER_FOREST, BiomeType.FLOWER_FOREST);
            put(BiomeKeys.FOREST, BiomeType.FOREST);
            put(BiomeKeys.FROZEN_OCEAN, BiomeType.FROZEN_OCEAN);
            put(BiomeKeys.FROZEN_RIVER, BiomeType.FROZEN_RIVER);
            put(BiomeKeys.ICE_SPIKES, BiomeType.ICE_SPIKES);
            put(BiomeKeys.JUNGLE, BiomeType.JUNGLE);
            put(BiomeKeys.LUKEWARM_OCEAN, BiomeType.LUKEWARM_OCEAN);
            put(BiomeKeys.MUSHROOM_FIELDS, BiomeType.MUSHROOM_FIELDS);
            put(BiomeKeys.NETHER_WASTES, BiomeType.NETHER_WASTES);
            put(BiomeKeys.OCEAN, BiomeType.OCEAN);
            put(BiomeKeys.PLAINS, BiomeType.PLAINS);
            put(BiomeKeys.RIVER, BiomeType.RIVER);
            put(BiomeKeys.SAVANNA, BiomeType.SAVANNA);
            put(BiomeKeys.SAVANNA_PLATEAU, BiomeType.SAVANNA_PLATEAU);
            put(BiomeKeys.SMALL_END_ISLANDS, BiomeType.SMALL_END_ISLANDS);
            put(BiomeKeys.SNOWY_BEACH, BiomeType.SNOWY_BEACH);
            put(BiomeKeys.SNOWY_TAIGA, BiomeType.SNOWY_TAIGA);
            put(BiomeKeys.SUNFLOWER_PLAINS, BiomeType.SUNFLOWER_PLAINS);
            put(BiomeKeys.SWAMP, BiomeType.SWAMP);
            put(BiomeKeys.TAIGA, BiomeType.TAIGA);
            put(BiomeKeys.THE_END, BiomeType.THE_END);
            put(BiomeKeys.THE_VOID, BiomeType.THE_VOID);
            put(BiomeKeys.WARM_OCEAN, BiomeType.WARM_OCEAN);
            put(BiomeKeys.WARPED_FOREST, BiomeType.WARPED_FOREST);

            //#if MC>=11800
            put(BiomeKeys.SPARSE_JUNGLE, BiomeType.SPARSE_JUNGLE);
            put(BiomeKeys.STONY_PEAKS, BiomeType.STONY_PEAKS);
            put(BiomeKeys.STONY_SHORE, BiomeType.STONY_SHORE);
            put(BiomeKeys.SNOWY_PLAINS, BiomeType.SNOWY_PLAINS);
            put(BiomeKeys.SNOWY_SLOPES, BiomeType.SNOWY_SLOPES);
            put(BiomeKeys.OLD_GROWTH_BIRCH_FOREST, BiomeType.OLD_GROWTH_BIRCH_FOREST);
            put(BiomeKeys.OLD_GROWTH_PINE_TAIGA, BiomeType.OLD_GROWTH_PINE_TAIGA);
            put(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA, BiomeType.OLD_GROWTH_SPRUCE_TAIGA);
            put(BiomeKeys.LUSH_CAVES, BiomeType.LUSH_CAVES);
            put(BiomeKeys.MEADOW, BiomeType.MEADOW);
            put(BiomeKeys.JAGGED_PEAKS, BiomeType.JAGGED_PEAKS);
            put(BiomeKeys.GROVE, BiomeType.GROVE);
            put(BiomeKeys.FROZEN_PEAKS, BiomeType.FROZEN_PEAKS);
            put(BiomeKeys.DRIPSTONE_CAVES, BiomeType.DRIPSTONE_CAVES);
            put(BiomeKeys.WINDSWEPT_FOREST, BiomeType.WINDSWEPT_FOREST);
            put(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, BiomeType.WINDSWEPT_GRAVELLY_HILLS);
            put(BiomeKeys.WINDSWEPT_HILLS, BiomeType.WINDSWEPT_HILLS);
            put(BiomeKeys.WINDSWEPT_SAVANNA, BiomeType.WINDSWEPT_SAVANNA);
            put(BiomeKeys.WOODED_BADLANDS, BiomeType.WOODED_BADLANDS);
            //#endif
        }
    };

    private final RegistryKey<Biome> biomeType;

    public BiomeVer(RegistryKey<Biome> biomeRegistryKey) {
        this.biomeType = biomeRegistryKey;
    }
}
