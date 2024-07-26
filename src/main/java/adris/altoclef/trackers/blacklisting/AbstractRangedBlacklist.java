package adris.altoclef.trackers.blacklisting;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

// Abstract class that blacklists positions
public abstract class AbstractPositionBlacklist<T, V extends Position> {

    private final Map<V, BlacklistEntry<V>> entries = new HashMap<>();

    protected abstract V getObjPos(T object);

    private record BlacklistEntry<V>(V Position, boolean isRanged, double range) {}

    public void blacklistPosition(V position, double range) {
        entries.put(position, new BlacklistEntry<>(position, false, range));

    }
}