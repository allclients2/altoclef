package adris.altoclef.multiversion;

import adris.altoclef.AltoClef;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class PlayerVer {

    public static PlayerInventory getInventory(AltoClef mod) {
        return getInventory(mod.getPlayer());
    }

    public static PlayerInventory getInventory(ClientPlayerEntity player) {
        //#if MC >= 11800
        return player.getInventory();
        //#else
        //$$ return player.inventory;
        //#endif
    }

    public static void setHeadPitch(AltoClef mod, float pitch) {
        //#if MC>=11800
        mod.getPlayer().setPitch(pitch);
        //#else
        //$$ mod.getPlayer().pitch = pitch;
        //#endif
    }

    public static void setHeadYaw(AltoClef mod, float yaw) {
        //#if MC>=11800
        mod.getPlayer().setYaw(yaw);
        //#else
        //$$ mod.getPlayer().headYaw = yaw;
        //#endif
    }

    public static int getOffHandIndex(PlayerInventory inventory) {
        //#if MC>=11800
        return PlayerInventory.OFF_HAND_SLOT;
        //#else
        //$$  return 40; //Don't think its changed...
        //#endif
    }


}
