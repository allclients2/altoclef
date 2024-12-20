package adris.altoclef.scanner.blacklist.spatial.entries;

// Where `T` is the class for the position (BlockPos, Vec3d, Vec3f, etc...)
public interface ISpatialBlacklistEntry<T> {

    /**
     * Try to keep this method (`getAvoidanceScore`) performant, as it will be called pretty often..
     */
    int avoidScore(T pos);

    /**
     * Get Maximum avoidance score by this entry.
     */
    int maxScore();
}
