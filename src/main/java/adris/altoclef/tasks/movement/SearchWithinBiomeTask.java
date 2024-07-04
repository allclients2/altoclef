package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import static adris.altoclef.util.helpers.WorldHelper.getBiomeAtBlockPos;

//#if MC>=11802
import net.minecraft.registry.entry.RegistryEntry;
//#endif

/**
 * Explores/Loads all chunks of a biome.
 */
public class SearchWithinBiomeTask extends SearchChunksExploreTask {

    private final RegistryKey<Biome> _toSearch;

    public SearchWithinBiomeTask(RegistryKey<Biome> toSearch) {
        _toSearch = toSearch;
    }

    //#if MC>=11802
    @Override
    protected boolean isChunkWithinSearchSpace(AltoClef mod, ChunkPos pos) {
        RegistryEntry<Biome> biome = getBiomeAtBlockPos(mod.getWorld(), pos.getStartPos().add(1, 1, 1));
        return biome.matchesKey(_toSearch);
    }
    //#else
    //$$  @Override
    //$$  protected boolean isChunkWithinSearchSpace(AltoClef mod, ChunkPos pos) {
    //$$      RegistryKey<Biome> biome = getBiomeAtBlockPos(mod.getWorld(), pos.getStartPos().add(1, 1, 1));
    //$$      return biome.equals(_toSearch);
    //$$  }
    //#endif

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof SearchWithinBiomeTask task) {
            return task._toSearch == _toSearch;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Searching within biome: " + _toSearch;
    }
}
