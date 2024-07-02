package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChunkLoadEvent;
import adris.altoclef.eventbus.events.ChunkUnloadEvent;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC>=11800
import net.minecraft.network.packet.s2c.play.ChunkData;
//#else
//$$ import net.minecraft.world.biome.source.BiomeArray;
//#endif

import java.util.function.Consumer;

@Mixin(ClientChunkManager.class)
public class LoadChunkMixin {

    /**
     * Loads a chunk from a packet and executes necessary actions.
     *
     *
     * @param x        The x-coordinate of the chunk.
     * @param z        The z-coordinate of the chunk.
     * @param buf      The packet containing the chunk data.
     * @param nbt      The NBT compound of the chunk.
     * @param consumer A consumer for visiting block entities in the chunk.
     * @param ci       The callback info returnable object.
     */
    @Inject(
            method = "loadChunkFromPacket",
            at = @At("RETURN")
    )
    //#if MC>=11800
    private void onLoadChunk(int x, int z, PacketByteBuf buf, NbtCompound nbt, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> cir) {
        // Publish a ChunkLoadEvent with the return value of the method as the argument
        EventBus.publish(new ChunkLoadEvent(cir.getReturnValue()));
    }
    //#else
    //$$  private void onLoadChunk(int x, int z, BiomeArray biomes, PacketByteBuf buf, NbtCompound tag, int verticalStripBitmask, boolean complete, CallbackInfoReturnable<WorldChunk> cir) {
    //$$      EventBus.publish(new ChunkLoadEvent(cir.getReturnValue()));
    //$$  };
    //#endif

    /**
     * Publishes a ChunkUnloadEvent when a chunk is unloaded.
     *
     * @param pos The position of the unloaded chunk.
     * @param ci  The callback info object.
     */
    @Inject(
            method = "unload",
            at = @At("TAIL")
    )
    //#if MC > 12001
    private void onChunkUnload(ChunkPos pos, CallbackInfo ci) {
        EventBus.publish(new ChunkUnloadEvent(pos));
    }
    //#else
    //$$ private void onChunkUnload(int x, int z, CallbackInfo ci) {
    //$$     EventBus.publish(new ChunkUnloadEvent(new ChunkPos(x,z)));
    //$$ }
    //#endif
}
