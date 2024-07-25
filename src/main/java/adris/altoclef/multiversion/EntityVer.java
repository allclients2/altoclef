package adris.altoclef.multiversion;

import adris.altoclef.mixins.PortalManagerAccessor;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

//#if MC <= 12006
import adris.altoclef.mixins.EntityAccessor;
//#endif

public class EntityVer {

    @Pattern
    public static boolean isInNetherPortal(Entity entity) {
        //#if MC <= 12006
        //$$ return ((EntityAccessor)entity).isInNetherPortal();
        //#else
        return (entity.portalManager != null && ((PortalManagerAccessor)entity.portalManager).accessPortal() instanceof NetherPortalBlock && entity.portalManager.isInPortal())
                || entity.getPortalCooldown() > 0;
        //#endif
    }

    @Pattern
    public int getPortalCooldown(Entity entity) {
        //#if MC >= 12001
        return entity.getPortalCooldown();
        //#else
        //$$ return ((EntityAccessor) entity).getPortalCooldown();
        //#endif
    }


    @Pattern
    public BlockPos getLandingPos(Entity entity) {
        //#if MC >= 11701
        return entity.getSteppingPos();
        //#else
        //$$ return ((adris.altoclef.mixins.EntityAccessor) entity).invokeGetLandingPos();
        //#endif
    }

    @Pattern
    private static ChunkPos getChunkPos(Entity entity) {
        //#if MC >= 11701
        return entity.getChunkPos();
        //#else
        //$$ return new ChunkPos(entity.getBlockPos());
        //#endif
    }

    // For MobDefenseChain, Threats that are of high priority...
    public static final List<Class<? extends Monster>> immediateThreat = Arrays.asList(
            WitherEntity.class,
            EndermanEntity.class,
            BlazeEntity.class,
            WitherSkeletonEntity.class,
            HoglinEntity.class,
            ZoglinEntity.class,
            PiglinBruteEntity.class,
            VindicatorEntity.class

            //#if MC >= 11900
            , WardenEntity.class
            //#endif

            // #if MC < BETA_17003
            // $$ HerobrineSubject.class,
            // #endif
    );

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
        //#if MC>11800
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

}
