package adris.altoclef.scanner;

import adris.altoclef.scanner.blacklist.spatial.BlockFailureBlacklist;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;

public class DimensionedScannerStateSerializable implements Serializable {

    private static final int MAX_BLACKLISTS_SIZE = 1500;

    public final HashMap<Block, HashSet<BlockPos>> trackedBlocks;
    public final HashMap<Block, HashSet<BlockPos>> scannedBlocks;
    public final HashMap<ChunkPos, Long> scannedChunks;
    public final BlockFailureBlacklist blacklist;

    public DimensionedScannerStateSerializable(World world) {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new BlockFailureBlacklist(world, MAX_BLACKLISTS_SIZE));
    }

    private DimensionedScannerStateSerializable(
        HashMap<Block, HashSet<BlockPos>> trackedBlocks,
        HashMap<Block, HashSet<BlockPos>> scannedBlocks,
        HashMap<ChunkPos, Long> scannedChunks,
        BlockFailureBlacklist blacklist
    ) {
        this.scannedChunks = scannedChunks;
        this.scannedBlocks = scannedBlocks;
        this.trackedBlocks = trackedBlocks;
        this.blacklist = blacklist;
    }

    public ByteBuffer serialize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        oos.writeObject(trackedBlocks);
        oos.writeObject(scannedBlocks);
        oos.writeObject(scannedChunks);
        oos.writeObject(blacklist);

        oos.close();
        byte[] bytes = bos.toByteArray();

        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes).flip();
        return buffer;
    }

    // Deserialization
    public static DimensionedScannerStateSerializable deserialize(ByteBuffer buffer) throws IOException, ClassNotFoundException {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);

        HashMap<Block, HashSet<BlockPos>> trackedBlocks = (HashMap<Block, HashSet<BlockPos>>) ois.readObject();
        HashMap<Block, HashSet<BlockPos>> scannedBlocks = (HashMap<Block, HashSet<BlockPos>>) ois.readObject();
        HashMap<ChunkPos, Long> scannedChunks = (HashMap<ChunkPos, Long>) ois.readObject();
        BlockFailureBlacklist blacklist = (BlockFailureBlacklist) ois.readObject();

        ois.close();

        DimensionedScannerStateSerializable state = new DimensionedScannerStateSerializable(null); // Pass a suitable World instance here

        return state;
    }
}
