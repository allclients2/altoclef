package adris.altoclef.multiversion;

import adris.altoclef.AltoClef;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;


//#if MC<11800
//$$ import net.minecraft.tag.BlockTags;
//$$ import net.minecraft.util.math.MathHelper;
//#endif

public class EntityVer {

    public EntityVer() {
    }

    // FIXME this should be possible with mappings, right?
    @Pattern
    public static Iterable<ItemStack> getItemsEquipped(LivingEntity entity) {
        // Wait these are the same methods?, Oh never mind they are barely different.
        //#if MC >= 12005
        return entity.getEquippedItems();
        //#else
        //$$ return entity.getItemsEquipped();
        //#endif
    }

    @Pattern
    public boolean isSuitableFor(Item item, BlockState state) {
        //#if MC >= 12005
        return item.getDefaultStack().isSuitableFor(state);
        //#else
        //$$ return item.isSuitableFor(state);
        //#endif
    }

    //FIXME: I don't know if this is even accurate..
    private static double squaredAttackRange(MobEntity attacker, LivingEntity target) {
        return (double) (attacker.getWidth() * 2.0F * attacker.getWidth() * 2.0F + target.getWidth());
    }

    @Pattern
    public static boolean isInAttackRange(MobEntity entity, LivingEntity victim) {
        //#if MC >= 12001
        return entity.isInAttackRange(victim);
        //#elseif MC >= 11800
        //$$ return entity.isInRange(victim, Math.sqrt(entity.squaredAttackRange(victim)));
        //#else
        //$$ return entity.isInRange(victim, Math.sqrt(squaredAttackRange(entity, victim)));
        //#endif
    }

    public static World getWorld(Entity entity) {
        //#if MC>11700
        return entity.getWorld();
        //#else
        //$$ return entity.world;
        //#endif
    }

    // Method entity.getEyePos() was added in minecraft 1.17
    // So simple, Was thinking if it would be more performant returning the "manual" pre 1.17 way even if after 1.17, so we don't add to the stack.
    public static Vec3d getEyePos(Entity entity) {
        //#if MC >= 11700
        return entity.getEyePos();
        //#else
        //$$ return new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
        //#endif
    }

    public static boolean inPowderedSnow(Entity entity) {
        //#if MC >= 11800
        return entity.inPowderSnow;
        //#else
        //$$ return false; //Wasn't even added in the game yet...
        //#endif
    }

    // CTRL + C
    // CTRL + V
    // My problems saved!
    //
    //#if MC < 11800
    //$$ private static BlockPos getLandingPosCopied(Entity entity) {
    //$$      int i = MathHelper.floor(entity.getPos().x);
    //$$      int j = MathHelper.floor(entity.getPos().y - 0.20000000298023224);
    //$$      int k = MathHelper.floor(entity.getPos().z);
    //$$      BlockPos blockPos = new BlockPos(i, j, k);
    //$$      if (entity.world.getBlockState(blockPos).isAir()) {
    //$$          BlockPos blockPos2 = blockPos.down();
    //$$          BlockState blockState = entity.world.getBlockState(blockPos2);
    //$$          Block block = blockState.getBlock();
    //$$          if (block.isIn(BlockTags.FENCES) || block.isIn(BlockTags.WALLS) || block instanceof FenceGateBlock) {
    //$$              return blockPos2;
    //$$          }
    //$$      }
    //$$      return blockPos;
    //$$  }
    //#endif


}