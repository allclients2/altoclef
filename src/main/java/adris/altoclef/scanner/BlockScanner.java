package adris.altoclef.scanner;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockPlaceEvent;
import adris.altoclef.eventbus.events.DimensionChangedEvent;
import adris.altoclef.multiversion.WorldBoundsVer;
import adris.altoclef.scanner.blacklist.spatial.BlockFailureBlacklist;
import adris.altoclef.scanner.blacklist.spatial.entries.BlacklistBlockEntry;
import adris.altoclef.scanner.blacklist.spatial.entries.ISpatialBlacklistEntry;
import adris.altoclef.util.BoundedPriorityQueue;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.publicenums.Dimension;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.function.Predicate;

public class BlockScanner {

    private static final boolean LOG = false;
    private static final int RESCAN_TICK_DELAY = 4 * 20;
    private static final int CACHED_POSITIONS_PER_BLOCK = 50;
    private static final int DEFAULT_MAX_AVOID_SCORE = 30;
    private static final int OLD_BLACKLIST_REMOVE_SECOND = 1200;
    private static final int MAX_BLACKLISTS_SIZE = 1500;

    /*
    Glossary:
        DistAvoidScore - Given the fromPosition and a Block, it is the distance
            combined with that blocks AvoidScore for ranking when needing to access the
            most feasible block to access.
        AvoidScore - From the BlockBlacklist, it is a score that is given to a BlockPos ranking how dangerous it is. Rank will be removed when the BlockState is changed at that pos.
    */

    protected final AssociatedStateHandler<SpecificWorld, DimensionedScannerState> dimensionStates;

    private final AltoClef mod;
    private final TimerGame rescanTimer = new TimerGame(1);

    protected BlockFailureBlacklist blacklist;
    private HashMap<Block, HashSet<BlockPos>> trackedBlocks;
    private HashMap<Block, HashSet<BlockPos>> scannedBlocks;
    private HashMap<ChunkPos, Long> scannedChunks;

    // used while scanning
    private HashMap<Block, HashSet<BlockPos>> cachedScannedBlocks = new HashMap<>();
    private Dimension scanDimension = Dimension.OVERWORLD;
    private World scanWorld = null;

    private boolean scanning = false;
    private boolean forceStop = false;

    public BlockScanner(AltoClef mod) {
        this.mod = mod;

        this.dimensionStates = new AssociatedStateHandler<>() {
            @Override
            public DimensionedScannerState createState(SpecificWorld key) {
                Debug.logInternal("Created Scanner State for SpecificWorld: " + key);
                return new DimensionedScannerState(key.world);
            }

            @Override
            public void applyState(DimensionedScannerState state) {
                blacklist = state.blacklist;
                trackedBlocks = state.trackedBlocks;
                scannedBlocks = state.scannedBlocks;
                scannedChunks = state.scannedChunks;
            }
        };

        EventBus.subscribe(DimensionChangedEvent.class, event -> dimensionStates.updateKey(new SpecificWorld(event.world(), WorldHelper.getNetworkName())));
        EventBus.subscribe(BlockPlaceEvent.class, evt -> addBlock(evt.blockState.getBlock(), evt.blockPos));
    }


    private Vec3d getPlayerPos() {
        return mod.getPlayer().getPos().add(0, 0.6f, 0);
    }

    protected void directBlacklist(BlockPos pos, ISpatialBlacklistEntry<BlockPos> entry) {
        blacklist.setBlacklist(pos, entry);
    }

    public void addBlock(Block block, BlockPos pos) {
        if (!isBlockAtPosition(pos, block)) {
            Debug.logInternal("INVALID SET: " + block + " " + pos);
            return;
        }

        if (trackedBlocks.containsKey(block)) {
            trackedBlocks.get(block).add(pos);
        } else {
            HashSet<BlockPos> set = new HashSet<>();
            set.add(pos);

            trackedBlocks.put(block, set);
        }
    }

    //TODO replace four with config <-- I don't think this is necessary
    public void requestBlockUnreachable(BlockPos pos) {
        requestBlockUnreachable(pos, 4);
    }

    public void requestBlockUnreachable(BlockPos pos, int allowedFailures) {
        requestBlockUnreachable(pos, allowedFailures, 50);
    }

    public void requestBlockUnreachable(BlockPos pos, int allowedFailures, int maxScore) {
        blacklist.positionFailed(pos, new BlacklistBlockEntry(pos, maxScore), allowedFailures);
    }

    public boolean isUnreachable(BlockPos pos) {
        return isUnreachable(pos, DEFAULT_MAX_AVOID_SCORE);
    }

    // Where `maxScore` is the maximum tolerable avoid score.
    public boolean isUnreachable(BlockPos pos, int maxScore) {
        return (blacklist.getAvoidScore(pos) > maxScore) || mod.getModSettings().isBlockPosBlacklisted(pos);
    }

    public List<BlockPos> getKnownLocations(int maxScore, Block... blocks) {
        List<BlockPos> locations = new LinkedList<>();

        for (Block block : blocks) {
            if (!trackedBlocks.containsKey(block)) continue;
            locations.addAll(trackedBlocks.get(block));
        }
        locations.removeIf(blockPos -> isUnreachable(blockPos, maxScore));

        return locations;
    }

    public List<BlockPos> getKnownLocations(Block... blocks) {
        return getKnownLocations(DEFAULT_MAX_AVOID_SCORE, blocks);
    }

    public boolean anyFound(int maxScore, Block... blocks) {
        return anyFound((block) -> true, maxScore, blocks);
    }
    public boolean anyFound(Predicate<BlockPos> isValidTest, int maxScore, Block... blocks) {
        for (Block block : blocks) {
            if (!trackedBlocks.containsKey(block)) continue;

            for (BlockPos pos : trackedBlocks.get(block)) {
                if (isValidTest.test(pos) && mod.getWorld().getBlockState(pos).getBlock().equals(block) && !this.isUnreachable(pos, maxScore))
                    return true;
            }
        }

        return false;
    }
    public boolean anyFound(Block... blocks) {
        return anyFound((block) -> true, DEFAULT_MAX_AVOID_SCORE, blocks);
    }
    public boolean anyFound(Predicate<BlockPos> isValidTest, Block... blocks) {
        return anyFound(isValidTest, DEFAULT_MAX_AVOID_SCORE, blocks);
    }

    public Optional<BlockPos> getNearestWithinScore(Vec3d fromPos, int maxScore, Block... blocks) {
        return getNearestWithinScore(fromPos, maxScore, (p) -> true, blocks);
    }
    public Optional<BlockPos> getNearestWithinScore(Vec3d fromPos, int maxScore, Predicate<BlockPos> isValidTest, Block... blocks) {
        final Optional<BlockSearchResult> searchResultOptional = searchNearestBlock(fromPos, isValidTest, blocks);
        if (searchResultOptional.isPresent()) {
            if (maxScore > searchResultOptional.get().distAvoidScore) {
                return Optional.of(searchResultOptional.get().blockPos);
            }
        }
        return Optional.empty();
    }

    public Optional<BlockPos> getNearestBlockOfTypes(Block... blocks) {
        return getNearestBlockOfTypes(getPlayerPos(), blocks);
    }
    public Optional<BlockPos> getNearestBlockOfTypes(Vec3d pos, Block... blocks) {
        return getNearestBlockOfTypes(pos, p -> true, blocks);
    }
    public Optional<BlockPos> getNearestBlockOfTypes(Predicate<BlockPos> isValidTest, Block... blocks) {
        return getNearestBlockOfTypes(getPlayerPos(), isValidTest, blocks);
    }
    public Optional<BlockPos> getNearestBlockOfTypes(Vec3d fromPos, Predicate<BlockPos> isValidTest, Block... blocks) {
        return searchNearestBlock(fromPos, isValidTest, blocks).map(result -> result.blockPos);
    }


    public Optional<BlockPos> getNearestBlock(Block block, Vec3d fromPos) {
        return getNearestBlock(block, fromPos, DEFAULT_MAX_AVOID_SCORE);
    }
    public Optional<BlockPos> getNearestBlock(Block block, Vec3d fromPos, int maxScore) {
        return getNearestBlock(block, (pos) -> true,  fromPos, maxScore);
    }
    public Optional<BlockPos> getNearestBlock(Block block, Predicate<BlockPos> isValidTest, Vec3d fromPos, int maxScore) {
        return searchNearestBlock(fromPos, isValidTest, block)
            .filter(result -> result.distAvoidScore < maxScore)
            .map(BlockSearchResult::blockPos);
    }


    // TODO: rename this method and `DistAvoidScore` because it sounds stupid.
    // Returns the `DistAvoidScore` for `checkPos` which should be used for distance comparisons.
    protected double calculateDistAvoidScore(Vec3d fromPos, BlockPos checkPos) {
        final int avoidScore = blacklist.getAvoidScore(checkPos);

        // To get a `DistAvoidScore`:
        // Add the avoidance score to the real distance to trick the system into thinking it's much further than it is for blocks that are REALLY DANGEROUS (high avoid score).
        // Example:
        //  Distance: 30 blocks
        //  Avoid score: 120
        //  distAvoidScore = 120 + 30
        //  distAvoidScore = 150
        return BaritoneHelper.calculateGenericHeuristic(fromPos, WorldHelper.toVec3d(checkPos)) + avoidScore;
    }

    public boolean anyFoundWithinDistance(double distance, Block... blocks) {
        return anyFoundWithinDistance(mod.getPlayer().getPos().add(0, 0.6f, 0), distance, blocks);
    }
    public boolean anyFoundWithinDistance(Vec3d pos, double distance, Block... blocks) {
        Optional<BlockPos> blockPos = getNearestBlockOfTypes(blocks);
        return blockPos.map(value -> value.isWithinDistance(pos, distance)).orElse(false);
    }

    // Returns the block of the least DistAvoidScore
    public double bestBlockScore(Block... blocks) {
        return bestBlockScore(mod.getPlayer().getPos().add(0, 0.6f, 0), blocks);
    }
    public double bestBlockScore(Vec3d pos, Block... blocks) {
        return searchNearestBlock(pos, (p) -> true, blocks)
                .map(result -> result.distAvoidScore)
                .orElse(Double.POSITIVE_INFINITY);
    }

    // Internal searching methods
    private Optional<BlockSearchResult> searchNearestBlock(Vec3d fromPos, Predicate<BlockPos> isValidTest, Block block) {
        BlockPos bestPos = null;
        double bestScore = Double.POSITIVE_INFINITY;

        if (!trackedBlocks.containsKey(block)) {
            return Optional.empty();
        }

        for (BlockPos pos : trackedBlocks.get(block)) {
            // Ensure the block is there (can change upon rescan), and is valid.
            if (!mod.getWorld().getBlockState(pos).getBlock().equals(block) || !isValidTest.test(pos)) continue;

            final double distAvoidScore = calculateDistAvoidScore(fromPos, pos);

            if (distAvoidScore < bestScore) {
                bestScore = distAvoidScore;
                bestPos = pos;
            }
        }
        return bestPos != null ? Optional.of(new BlockSearchResult(bestPos, bestScore)) : Optional.empty();
    }

    private Optional<BlockSearchResult> searchNearestBlock(Vec3d fromPos, Predicate<BlockPos> isValidTest, Block... blocks) {
        BlockSearchResult bestResult = null;

        for (Block block : blocks) {
            Optional<BlockSearchResult> searchResultOptional = searchNearestBlock(fromPos, isValidTest, block);

            if (searchResultOptional.isPresent()) {
                BlockSearchResult searchResult = searchResultOptional.get();
                if (bestResult == null || (bestResult.distAvoidScore > searchResult.distAvoidScore)) {
                    bestResult = searchResult;
                }
            }
        }

        return (bestResult != null) ? Optional.of(bestResult) : Optional.empty();
    }

    public record BlockSearchResult(BlockPos blockPos, double distAvoidScore) {};


    // Checks if 'pos' one of 'blocks' block
    // Returns false if incorrect or undetermined/unsure
    public boolean isBlockAtPosition(BlockPos pos, Block... blocks) {
        if (isUnreachable(pos)) {
            return false;
        }

        if (!mod.getChunkTracker().isChunkLoaded(pos)) {
            return false;
        }

        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return false;
        }
        try {
            for (Block block : blocks) {
                if (world.isAir(pos) && WorldHelper.isAir(block)) {
                    return true;
                }
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() == block) {
                    return true;
                }
            }
            return false;
        } catch (NullPointerException e) {
            // Probably out of chunk. This means we can't judge its state.
            return false;
        }
    }

    public void resetScanProgress() {
        rescanTimer.forceElapse();
        forceStop = true;
    }

    public void clearStates() {
        dimensionStates.resetCurrent();
    }

    public void tick() {
        if (mod.getWorld() == null || mod.getPlayer() == null) return;

        // Be maximally aware of the closest blocks around you
        scanCloseBlocks();

        // Rescan
        if (rescanTimer.elapsed() && !scanning) {

            final Dimension currentDimension = WorldHelper.getCurrentDimension();
            if (scanDimension != currentDimension || mod.getWorld() != scanWorld) {
                if (LOG) {
                    Debug.logMessage("BlockScanner: new dimension or world detected, resetting data!");
                }
                resetScanProgress();
                scanWorld = mod.getWorld();
                scanDimension = currentDimension;
                dimensionStates.updateKey(new SpecificWorld(mod.getWorld(), WorldHelper.getNetworkName()));
                return;
            }

            cachedScannedBlocks = new HashMap<>(scannedBlocks.size());
            for (Map.Entry<Block, HashSet<BlockPos>> entry : scannedBlocks.entrySet()) {
                cachedScannedBlocks.put(entry.getKey(), (HashSet<BlockPos>) entry.getValue().clone());
            }

            if (LOG) {
                Debug.logMessage("Updating BlockScanner.. size: " + trackedBlocks.size() + " : " + cachedScannedBlocks.size());
            }

            scanning = true;
            forceStop = false;
            new Thread(() -> {
                try {
                    rescan(Integer.MAX_VALUE, Integer.MAX_VALUE);
                    updateBlacklist(); // update blacklist after rescan
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    rescanTimer.reset();
                    scanning = false;
                }
            }).start();
        }
    }

    private void scanCloseBlocks() {
        for (Map.Entry<Block, HashSet<BlockPos>> entry : cachedScannedBlocks.entrySet()) {
            if (!trackedBlocks.containsKey(entry.getKey())) {
                trackedBlocks.put(entry.getKey(), new HashSet<>());
            }
            trackedBlocks.get(entry.getKey()).clear();

            trackedBlocks.get(entry.getKey()).addAll(entry.getValue());
        }

        HashMap<Block, HashSet<BlockPos>> map = new HashMap<>();

        BlockPos pos = mod.getPlayer().getBlockPos();
        World world = mod.getWorld();

        for (int x = pos.getX() - 8; x <= pos.getX() + 8; x++) {
            for (int y = pos.getY() - 8; y < pos.getY() + 8; y++) {
                for (int z = pos.getZ() - 8; z <= pos.getZ() + 8; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(p);
                    if (world.getBlockState(p).isAir()) continue;

                    Block block = state.getBlock();

                    if (map.containsKey(block)) {
                        map.get(block).add(p);
                    } else {
                        HashSet<BlockPos> set = new HashSet<>();
                        set.add(p);
                        map.put(block, set);
                    }
                }
            }
        }

        for (Map.Entry<Block, HashSet<BlockPos>> entry : map.entrySet()) {
            getFirstToClosePositions(entry.getValue(),mod.getPlayer().getPos());

            if (!trackedBlocks.containsKey(entry.getKey())) {
                trackedBlocks.put(entry.getKey(), new HashSet<>());
            }

            trackedBlocks.get(entry.getKey()).addAll(entry.getValue());
        }
    }

    private void rescan(int maxCount, int cutOffRadius) {
        long ms = System.currentTimeMillis();

        ChunkPos playerChunkPos = new ChunkPos(mod.getPlayer().getBlockPos());
        Vec3d playerPos = mod.getPlayer().getPos();

        HashSet<ChunkPos> visited = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.add(new Node(playerChunkPos, 0));

        while (!queue.isEmpty() && visited.size() < maxCount && !forceStop) {
            Node node = queue.poll();

            if (node.distance > cutOffRadius || visited.contains(node.pos) || !mod.getWorld().getChunkManager().isChunkLoaded(node.pos.x, node.pos.z))
                continue;

            boolean isPriorityChunk = getChunkDist(node.pos, playerChunkPos) <= 2;
            if (!isPriorityChunk && scannedChunks.containsKey(node.pos) && mod.getWorld().getTime() - scannedChunks.get(node.pos) < RESCAN_TICK_DELAY)
                continue;

            visited.add(node.pos);
            scanChunk(node.pos, playerChunkPos);

            queue.add(new Node(new ChunkPos(node.pos.x + 1, node.pos.z + 1), node.distance + 1));
            queue.add(new Node(new ChunkPos(node.pos.x - 1, node.pos.z + 1), node.distance + 1));
            queue.add(new Node(new ChunkPos(node.pos.x - 1, node.pos.z - 1), node.distance + 1));
            queue.add(new Node(new ChunkPos(node.pos.x + 1, node.pos.z - 1), node.distance + 1));
        }
        if (forceStop) {
            // reset again, might have changed some values from the time forceStop was called
            resetScanProgress();
            forceStop = false;
            return;
        }

        for (Iterator<ChunkPos> iterator = scannedChunks.keySet().iterator(); iterator.hasNext(); ) {
            ChunkPos pos = iterator.next();
            int distance = getChunkDist(pos, playerChunkPos);

            if (distance > cutOffRadius) {
                iterator.remove();
            }
        }

        // trim sets just incase
        for (HashSet<BlockPos> set : scannedBlocks.values()) {
            if (set.size() >= CACHED_POSITIONS_PER_BLOCK) {
                getFirstToClosePositions(set, playerPos);
            }
        }

        if (LOG) {
            Debug.logMessage("Rescanned in: " + (System.currentTimeMillis() - ms) + " ms; visited: " + visited.size() + " chunks");
        }
    }

    private int getChunkDist(ChunkPos pos1, ChunkPos pos2) {
        return Math.abs(pos1.x - pos2.x) + Math.abs(pos1.z - pos2.z);
    }

    private void updateBlacklist() {
        blacklist.updateBlacklist(OLD_BLACKLIST_REMOVE_SECOND * 1000);
    }


    // Trims the Set to only the first closest positions to the player?
    // Note I made this only use calculateGenericHeuristic instead of DistAvoidScore because of the lag.
    private void getFirstToClosePositions(HashSet<BlockPos> set, Vec3d playerPos) {
        Map<BlockPos, Double> scoreCache = new HashMap<>();
        Queue<BlockPos> queue = new BoundedPriorityQueue<>(CACHED_POSITIONS_PER_BLOCK,
            Comparator.comparingDouble(pos ->
                scoreCache.computeIfAbsent(pos, p -> -BaritoneHelper.calculateGenericHeuristic(playerPos, WorldHelper.toVec3d(p)))
            )
        );
        queue.addAll(set);
        set.clear();
        set.addAll(queue);
    }

    /**
     * scans a chunk and adds block positions corresponding to a specific block in a list
     *
     * @param chunkPos position of the scanned chunk
     */
    private void scanChunk(ChunkPos chunkPos, ChunkPos playerChunkPos) {
        World world = mod.getWorld();
        WorldChunk chunk = mod.getWorld().getChunk(chunkPos.x, chunkPos.z);
        scannedChunks.put(chunkPos, world.getTime());

        boolean isPriorityChunk = getChunkDist(chunkPos, playerChunkPos) <= 2;

        for (int x = chunkPos.getStartX(); x <= chunkPos.getEndX(); x++) {
            for (int y = WorldBoundsVer.WORLD_FLOOR_Y; y < WorldBoundsVer.WORLD_CEILING_Y; y++) {
                for (int z = chunkPos.getStartZ(); z <= chunkPos.getEndZ(); z++) {
                    BlockPos p = new BlockPos(x, y, z);

                    BlockState state = chunk.getBlockState(p);
                    if (state.isAir()) continue;

                    Block block = state.getBlock();
                    if (scannedBlocks.containsKey(block)) {
                        HashSet<BlockPos> set = scannedBlocks.get(block);

                        if (set.size() > CACHED_POSITIONS_PER_BLOCK * 750 && !isPriorityChunk) continue;

                        set.add(p);
                    } else {
                        HashSet<BlockPos> set = new HashSet<>();
                        set.add(p);
                        scannedBlocks.put(block, set);
                    }
                }
            }
        }
    }

    private record Node(ChunkPos pos, int distance) {
    }

    // Associates a state with a key.
    // On updateKey the state associated with the key will be `applied`.
    // where `T` is state and `V` is the key
    protected abstract static class AssociatedStateHandler<V, T> {
        private final HashMap<V, T> genericMap = new HashMap<>();
        private V latestKey;

        public abstract T createState(V key);
        public abstract void applyState(T state);

        // returns the state associated with the `newKey`
        public void updateKey(V newKey) {
            if (latestKey != newKey) {
                latestKey = newKey;
                if (genericMap.containsKey(newKey)) {
                    applyState(genericMap.get(newKey));
                } else {
                    T newState = createState(newKey);
                    genericMap.put(newKey, newState);
                    applyState(newState);
                }
            }
        }

        public void resetKey(V key) {
            if (key.equals(latestKey)) {
                applyState(createState(key));
            } else {
                genericMap.remove(key);
            }
        }

        public V getLatestKey() {
            return latestKey;
        }

        public void resetCurrent() {
            resetKey(latestKey);
        }
    }

    protected record SpecificWorld(World world, String networkName) {
        @Override
        public int hashCode() {
            return Objects.hash(Dimension.dimensionFromWorldKey(world.getRegistryKey()).name(), networkName);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SpecificWorld specificWorld) {
                return (specificWorld.world.getRegistryKey() == world.getRegistryKey()) &&
                    Objects.equals(networkName, specificWorld.networkName);
            }
            return false;
        }

        @Override
        public String toString() {
            return "SpecificWorld[" + world.getRegistryKey().getValue().toString() + " from " + networkName + "]";
        }
    }

    public static class DimensionedScannerState {
        public final HashMap<Block, HashSet<BlockPos>> trackedBlocks = new HashMap<>();
        public final HashMap<Block, HashSet<BlockPos>> scannedBlocks = new HashMap<>();
        public final HashMap<ChunkPos, Long> scannedChunks = new HashMap<>();
        public final BlockFailureBlacklist blacklist;


        public DimensionedScannerState(World world) {
            this.blacklist = new BlockFailureBlacklist(world, MAX_BLACKLISTS_SIZE);
        }
    }
}
