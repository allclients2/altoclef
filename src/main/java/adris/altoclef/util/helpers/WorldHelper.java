package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.mixins.ClientConnectionAccessor;
import adris.altoclef.mixins.EntityAccessor;
import adris.altoclef.multiversion.MethodWrapper;
import adris.altoclef.multiversion.WorldBoundsVer;
import adris.altoclef.util.Dimension;
import baritone.api.BaritoneAPI;
import baritone.pathing.movement.CalculationContext;
import baritone.pathing.movement.MovementHelper;
import baritone.process.MineProcess;
import baritone.utils.BlockStateInterface;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

//#if MC >= 11802
import net.minecraft.registry.entry.RegistryEntry;
//#else
//$$ import net.minecraft.util.registry.Registry;
//$$ import net.minecraft.util.registry.RegistryKey;
//#endif

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Super useful helper functions for getting information about the world.
 */
public abstract class WorldHelper {

    public static final int WORLD_CEILING_Y = WorldBoundsVer.WORLD_CEILING_Y;
    public static final int WORLD_FLOOR_Y = WorldBoundsVer.WORLD_FLOOR_Y;

    /**
     * Get the number of in-game ticks the game/world has been active for.
     */
    public static int getTicks() {
        ClientConnection con = Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getConnection();
        return ((ClientConnectionAccessor) con).getTicks();
    }

    public static Vec3d toVec3d(BlockPos pos) {
        if (pos == null) return null;
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static Vec3d toVec3d(Vec3i pos) {
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vec3i toVec3i(Vec3d pos) {
        return new Vec3i((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());
    }

    public static BlockPos toBlockPos(Vec3d pos) {
        return new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());
    }

    public static double distanceXZSquared(Vec3d from, Vec3d to) {
        Vec3d delta = to.subtract(from);
        return (delta.x * delta.x) + (delta.z * delta.z);
    }

    public static double distanceXZ(Vec3d from, Vec3d to) {
        return Math.sqrt(distanceXZSquared(from, to));
    }


    // Loops over in a 3d box radius `radius` from the `pos`, if the block is found in `blocksSearch` then inputs through the `onBlockMatched`, which if `onBlockMatched` returns true, it will break the loop..
    public static void forBlocksWithinBoxRadiusOfPos(AltoClef mod, BlockPos pos, int radius, List<Block> blocksSearch, Function<BlockPos, Boolean> onBlockMatched) {
        searchLoop:
        for (int x = -radius; x < radius; x++) {
            for (int y = -radius; y < radius; y++) {
                for (int z = -radius; z < radius; z++) {
                    final BlockPos position = pos.add(x, y, z);
                    final Block block = mod.getWorld().getBlockState(position).getBlock();
                    if (blocksSearch.contains(block) && onBlockMatched.apply(position)) {
                        break searchLoop;
                    }
                }
            }
        }
    }

    public static boolean blocksWithinBoxRadiusOfPos(AltoClef mod, BlockPos pos, int radius, List<Block> blocksSearch) {
        AtomicBoolean found = new AtomicBoolean(false);
        forBlocksWithinBoxRadiusOfPos(mod, pos, radius, blocksSearch,
            (block -> {
                found.set(true);
                return true;
            })
        );
        return found.get();
    }

    public static boolean inRangeXZ(Vec3d from, Vec3d to, double range) {
        return distanceXZSquared(from, to) < range * range;
    }

    public static boolean inRangeXZ(BlockPos from, BlockPos to, double range) {
        return inRangeXZ(toVec3d(from), toVec3d(to), range);
    }

    public static boolean inRangeXZ(Entity entity, Vec3d to, double range) {
        return inRangeXZ(entity.getPos(), to, range);
    }

    public static boolean inRangeXZ(Entity entity, BlockPos to, double range) {
        return inRangeXZ(entity, toVec3d(to), range);
    }

    public static boolean inRangeXZ(Entity entity, Entity to, double range) {
        return inRangeXZ(entity, to.getPos(), range);
    }

    public static Dimension getCurrentDimension() {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return Dimension.OVERWORLD;

        //#if MC>=11904
        if (world.getDimension().ultrawarm()) return Dimension.NETHER;
        if (world.getDimension().natural()) return Dimension.OVERWORLD;
        //#else
        //$$ if (world.getDimension().isUltrawarm()) return Dimension.NETHER;
        //$$ if (world.getDimension().isNatural()) return Dimension.OVERWORLD;
        //#endif

        return Dimension.END;
    }

    public static boolean isVulnurable(AltoClef mod) {
        int armor = mod.getPlayer().getArmor();
        float health = mod.getPlayer().getHealth();
        if (armor <= 15 && health < 3) return true;
        if (armor < 10 && health < 10) return true;
        return armor < 5 && health < 18;
    }

    public static boolean isDangerous(AltoClef mod, BlockPos pos) {
        Iterable<Entity> entities = mod.getWorld().getEntities();
        for (Entity entity : entities) {
            if (entity instanceof HostileEntity) {
                if (!mod.getBlockScanner().isUnreachable(pos)) {
                    if (
                            mod.getPlayer().squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) > 4 &&
                                    pos.isWithinDistance(entity.getPos(), 30)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static int getGroundHeight(AltoClef mod, int x, int z) {
        for (int y = WORLD_CEILING_Y; y >= WORLD_FLOOR_Y; --y) {
            BlockPos check = new BlockPos(x, y, z);
            if (BlockHelper.isSolidBlock(mod, check)) return y;
        }
        return -1;
    }

    public static BlockPos getADesertTemple(AltoClef mod) {
        List<BlockPos> stonePressurePlates = mod.getBlockScanner().getKnownLocations(Blocks.STONE_PRESSURE_PLATE);
        if (!stonePressurePlates.isEmpty()) {
            for (BlockPos pos : stonePressurePlates) {
                if (mod.getWorld().getBlockState(pos).getBlock() == Blocks.STONE_PRESSURE_PLATE && // Duct tape
                        mod.getWorld().getBlockState(pos.down()).getBlock() == Blocks.CUT_SANDSTONE &&
                        mod.getWorld().getBlockState(pos.down(2)).getBlock() == Blocks.TNT) {
                    return pos;
                }
            }
        }
        return null;
    }

    public static boolean isUnopenedChest(AltoClef mod, BlockPos pos) {
        return mod.getItemStorage().getContainerAtPosition(pos).isEmpty();
    }

    public static int getGroundHeight(AltoClef mod, int x, int z, Block... groundBlocks) {
        Set<Block> possibleBlocks = new HashSet<>(Arrays.asList(groundBlocks));
        for (int y = WORLD_CEILING_Y; y >= WORLD_FLOOR_Y; --y) {
            BlockPos check = new BlockPos(x, y, z);
            if (possibleBlocks.contains(mod.getWorld().getBlockState(check).getBlock())) return y;

        }
        return -1;
    }

    public static boolean canBreak(AltoClef mod, BlockPos pos) {
        // JANK: Temporarily check if we can break WITHOUT paused interactions.
        // Not doing this creates bugs where we loop back and forth through the nether portal and stuff.
        boolean prevInteractionPaused = mod.getExtraBaritoneSettings().isInteractionPaused();
        mod.getExtraBaritoneSettings().setInteractionPaused(false);
        boolean result = mod.getWorld().getBlockState(pos).getHardness(mod.getWorld(), pos) >= 0
                && !mod.getExtraBaritoneSettings().shouldAvoidBreaking(pos)
                && MineProcess.plausibleToBreak(new CalculationContext(mod.getClientBaritone()), pos)
                && canReach(mod, pos);
        mod.getExtraBaritoneSettings().setInteractionPaused(prevInteractionPaused);
        return result;
    }

    public static boolean isInNetherPortal(AltoClef mod) {
        if (mod.getPlayer() == null)
            return false;
        return adris.altoclef.multiversion.entity.EntityHelper.isInNetherPortal(mod.getPlayer());
    }

    public static boolean dangerousToBreakIfRightAbove(AltoClef mod, BlockPos toBreak) {
        // There might be mumbo jumbo next to it, we fall and we get killed by lava or something.
        if (MovementHelper.avoidBreaking(mod.getClientBaritone().bsi, toBreak.getX(), toBreak.getY(), toBreak.getZ(), mod.getWorld().getBlockState(toBreak))) {
            return true;
        }
        // Fall down
        for (int dy = 1; dy <= toBreak.getY() - WORLD_FLOOR_Y; ++dy) {
            BlockPos check = toBreak.down(dy);
            BlockState s = mod.getWorld().getBlockState(check);
            boolean tooFarToFall = dy > mod.getClientBaritoneSettings().maxFallHeightNoWater.value;
            // Don't fall in lava
            if (MovementHelper.isLava(s))
                return true;
            // Always fall in water
            // TODO: If there's a 1 meter thick layer of water and then a massive drop below, the bot will think it is safe.
            if (MovementHelper.isWater(s))
                return true;
            // We hit ground, depends
            if (BlockHelper.isSolidBlock(mod, check)) {
                return tooFarToFall;
            }
        }
        // At this point we probably fall through the void, so not safe.
        return true;
    }

    public static boolean canPlace(AltoClef mod, BlockPos pos) {
        return !mod.getExtraBaritoneSettings().shouldAvoidPlacingAt(pos)
                && canReach(mod, pos);
    }


    public static boolean canReach(AltoClef mod, BlockPos pos) {
        if (mod.getModSettings().shouldAvoidOcean()) {
            // 45 is roughly the ocean floor. We add 2 just cause why not.
            // This > 47 can clearly cause a stuck bug.
            if (mod.getPlayer().getY() > 47 && mod.getChunkTracker().isChunkLoaded(pos) && isOcean(getBiomeAtBlockPos(mod.getWorld(), pos))) { // But if we stuck, add more oceans
                // Block is in an ocean biome. If it's below sea level...
                if (pos.getY() < 64 && getGroundHeight(mod, pos.getX(), pos.getZ(), Blocks.WATER) > pos.getY()) {
                    return false;
                }
            }
        }
        return !mod.getBlockScanner().isUnreachable(pos);
    }

    //#if MC>=11802
    static boolean isOcean(RegistryEntry<Biome> biomeRegistryKey) {
        return (biomeRegistryKey.matchesKey(BiomeKeys.OCEAN)
                || biomeRegistryKey.matchesKey(BiomeKeys.COLD_OCEAN)
                || biomeRegistryKey.matchesKey(BiomeKeys.DEEP_COLD_OCEAN)
                || biomeRegistryKey.matchesKey(BiomeKeys.DEEP_OCEAN)
                || biomeRegistryKey.matchesKey(BiomeKeys.DEEP_FROZEN_OCEAN)
                || biomeRegistryKey.matchesKey(BiomeKeys.DEEP_LUKEWARM_OCEAN)
                || biomeRegistryKey.matchesKey(BiomeKeys.LUKEWARM_OCEAN)
                || biomeRegistryKey.matchesKey(BiomeKeys.WARM_OCEAN)
                || biomeRegistryKey.matchesKey(BiomeKeys.FROZEN_OCEAN));
    }

    ;
    //#else
    //$$ private static final Set<RegistryKey<Biome>> OCEAN_BIOMES = new HashSet<>() {
    //$$     {
    //$$         add(BiomeKeys.OCEAN);
    //$$         add(BiomeKeys.COLD_OCEAN);
    //$$         add(BiomeKeys.DEEP_COLD_OCEAN);
    //$$         add(BiomeKeys.DEEP_OCEAN);
    //$$         add(BiomeKeys.DEEP_FROZEN_OCEAN);
    //$$         add(BiomeKeys.DEEP_LUKEWARM_OCEAN);
    //$$         add(BiomeKeys.LUKEWARM_OCEAN);
    //$$         add(BiomeKeys.WARM_OCEAN);
    //$$         add(BiomeKeys.FROZEN_OCEAN);
    //$$     }
    //$$ };
    //$$  public static boolean isOcean(RegistryKey<Biome> biomeRegistryKey) {
    //$$      return OCEAN_BIOMES.contains(biomeRegistryKey);
    //$$  }
    //#endif

    //#if MC>=11802
    public static RegistryEntry<Biome> getBiomeAtBlockPos(World world, BlockPos pos) {
        return world.getBiome(pos);
    }
    //#else
    //$$ public static RegistryKey<Biome> getBiomeAtBlockPos(World world, BlockPos pos) {
    //$$     Registry<Biome> biomeRegistry = world.getRegistryManager().get(Registry.BIOME_KEY);
    //$$     return biomeRegistry.getKey(world.getBiome(pos)).orElse(null);
    //$$ }
    //#endif


    public static boolean isAir(AltoClef mod, BlockPos pos) {
        return mod.getBlockScanner().isBlockAtPosition(pos, Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR);
        //return state.isAir() || isAir(state.getBlock());
    }

    public static boolean isAir(Block block) {
        return block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR;
    }

    public static boolean isInteractableBlock(AltoClef mod, BlockPos pos) {
        Block block = mod.getWorld().getBlockState(pos).getBlock();
        return (block instanceof ChestBlock
                || block instanceof EnderChestBlock
                || block instanceof CraftingTableBlock
                || block instanceof AbstractFurnaceBlock
                || block instanceof LoomBlock
                || block instanceof CartographyTableBlock
                || block instanceof EnchantingTableBlock
                || block instanceof RedstoneOreBlock
                || block instanceof BarrelBlock
        );
    }


    /**
     * Searches for the position of an end portal frame by averaging the known locations of the frames.
     * Returns the center position of the frames if enough frames are found, otherwise returns null.
     *
     * @param mod The AltoClef instance.
     * @return The position of the end portal frame, or null if not enough frames are found.
     */
    public static BlockPos doSimpleSearchForEndPortal(AltoClef mod) {
        List<BlockPos> frames = mod.getBlockScanner().getKnownLocations(Blocks.END_PORTAL_FRAME);
        if (frames.size() >= 12) {
            Vec3d average = frames.stream().reduce(Vec3d.ZERO, (accum, bpos) -> accum.add((int) Math.round(bpos.getX() + 0.5), (int) Math.round(bpos.getY() + 0.5), (int) Math.round(bpos.getZ() + 0.5)), Vec3d::add).multiply(1d / frames.size());
            return new BlockPos(new Vec3i((int) average.x, (int) average.y, (int) average.z));
        }
        return null;
    }

    // Simple check for if the center block of the end portal is present...
    public static boolean isEndPortalOpened(AltoClef mod, BlockPos endPortalCenter) {
        return endPortalCenter != null && mod.getBlockScanner().isBlockAtPosition(endPortalCenter, Blocks.END_PORTAL);
    }

    public static boolean isEndPortalFrameFilled(AltoClef mod, BlockPos pos) {
        if (mod.getChunkTracker().isChunkLoaded(pos)) {
            final BlockState blockState = mod.getWorld().getBlockState(pos);
            if (blockState.getBlock() == Blocks.END_PORTAL_FRAME) {
                return blockState.get(EndPortalFrameBlock.EYE);
            }
        }
        return false;
    }

    public static boolean isInsidePlayer(AltoClef mod, BlockPos pos) {
        return pos.isWithinDistance(mod.getPlayer().getPos(), 2);
    }

    public static Iterable<BlockPos> getBlocksTouchingPlayer(AltoClef mod) {
        return getBlocksTouchingBox(mod, mod.getPlayer().getBoundingBox());
    }

    public static Iterable<BlockPos> getBlocksTouchingBox(AltoClef mod, Box box) {
        BlockPos min = new BlockPos((int) box.minX, (int) box.minY, (int) box.minZ);
        BlockPos max = new BlockPos((int) box.maxX, (int) box.maxY, (int) box.maxZ);
        return scanRegion(mod, min, max);
    }

    public static Iterable<BlockPos> scanRegion(AltoClef mod, BlockPos start, BlockPos end) {
        return () -> new Iterator<>() {
            int x = start.getX(), y = start.getY(), z = start.getZ();

            @Override
            public boolean hasNext() {
                return y <= end.getY() && z <= end.getZ() && x <= end.getX();
            }

            @Override
            public BlockPos next() {
                BlockPos result = new BlockPos(x, y, z);
                ++x;
                if (x > end.getX()) {
                    x = start.getX();
                    ++z;
                    if (z > end.getZ()) {
                        z = start.getZ();
                        ++y;
                    }
                }
                return result;
            }
        };
    }

    public static boolean fallingBlockSafeToBreak(BlockPos pos) {
        BlockStateInterface bsi = new BlockStateInterface(BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext());
        World w = MinecraftClient.getInstance().world;
        assert w != null;
        while (isFallingBlock(pos)) {
            if (MovementHelper.avoidBreaking(bsi, pos.getX(), pos.getY(), pos.getZ(), w.getBlockState(pos)))
                return false;
            pos = pos.up();
        }
        return true;
    }

    public static boolean isFallingBlock(BlockPos pos) {
        World w = MinecraftClient.getInstance().world;
        assert w != null;
        return w.getBlockState(pos).getBlock() instanceof FallingBlock;
    }

    public static Entity getSpawnerEntity(AltoClef mod, BlockPos pos) {
        BlockState state = mod.getWorld().getBlockState(pos);
        if (state.getBlock() instanceof SpawnerBlock) {
            BlockEntity be = mod.getWorld().getBlockEntity(pos);
            if (be instanceof MobSpawnerBlockEntity blockEntity) {
                return MethodWrapper.getRenderedEntity(blockEntity.getLogic(), mod.getWorld(), pos);
            }
        }
        return null;
    }

    public static Vec3d getOverworldPosition(Vec3d pos) {
        if (getCurrentDimension() == Dimension.NETHER) {
            pos = pos.multiply(8.0, 1, 8.0);
        }
        return pos;
    }

    public static BlockPos getOverworldPosition(BlockPos pos) {
        if (getCurrentDimension() == Dimension.NETHER) {
            pos = new BlockPos(pos.getX() * 8, pos.getY(), pos.getZ() * 8);
        }
        return pos;
    }

    public static boolean isChest(AltoClef mod, BlockPos block) {
        Block b = mod.getWorld().getBlockState(block).getBlock();
        return isChest(b);
    }

    public static boolean isChest(Block b) {
        return b instanceof ChestBlock || b instanceof EnderChestBlock;
    }

    public static boolean isBlock(AltoClef mod, BlockPos pos, Block block) {
        return mod.getWorld().getBlockState(pos).getBlock() == block;
    }

    public static boolean canSleep() {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null) {
            // You can sleep during thunderstorms
            if (world.isThundering() && world.isRaining())
                return true;

            int time = getTimeOfDay();
            // https://minecraft.fandom.com/wiki/Daylight_cycle
            return 12542 <= time && time <= 23992;
        }

        return false;
    }

    public static int getTimeOfDay() {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null) {
            // You can sleep during thunderstorms
            return (int) (world.getTimeOfDay() % 24000);
        }
        return 0;
    }

    public static boolean isVulnerable(AltoClef mod) {
        int armor = mod.getPlayer().getArmor();
        float health = mod.getPlayer().getHealth();
        if (armor <= 15 && health < 3) return true;
        if (armor < 10 && health < 10) return true;
        return armor < 5 && health < 18;
    }

    public static boolean isSurroundedByHostiles(AltoClef mod) {
        List<LivingEntity> hostiles = mod.getEntityTracker().getHostiles();
        return isSurrounded(mod, hostiles);
    }

    // Function to check if the player is surrounded on two or more sides
    public static boolean isSurrounded(AltoClef mod, List<LivingEntity> entities) {

        BlockPos playerPos = mod.getPlayer().getBlockPos();

        // Minimum number of sides to consider the origin surrounded
        final int MIN_SIDES_TO_SURROUND = 2;

        // Count the number of unique sides based on angles
        List<Direction> uniqueSides = new ArrayList<Direction>();

        // Iterate through each point and calculate the angle with the origin
        for (Entity entity : entities) {
            if (!entity.isInRange(mod.getPlayer(), 8)) continue;
            BlockPos entityPos = entity.getBlockPos();
            double angle = calculateAngle(playerPos, entityPos);

            // Check if the angle is unique
            boolean isUnique = !uniqueSides.contains(getHorizontalDirectionFromYaw(angle));

            // If the angle is unique, increment the uniqueSides count
            if (isUnique) {
                uniqueSides.add(getHorizontalDirectionFromYaw(angle));
            }
        }

        // Check if the origin is surrounded on two or more sides
        return uniqueSides.size() >= MIN_SIDES_TO_SURROUND;
    }

    private static double calculateAngle(BlockPos origin, BlockPos target) {
        double translatedX = target.getX() - origin.getX();
        double translatedZ = target.getZ() - origin.getZ();
        double angleRad = Math.atan2(translatedZ, translatedX);
        double angleDeg = Math.toDegrees(angleRad);
        angleDeg -= 90;
        if (angleDeg < 0) {
            angleDeg += 360;
        }
        return angleDeg;
    }

    private static Direction getHorizontalDirectionFromYaw(double yaw) {
        yaw %= 360.0F;
        if (yaw < 0) {
            yaw += 360.0F;
        }

        if ((yaw >= 45 && yaw < 135) || (yaw >= -315 && yaw < -225)) {
            return Direction.WEST;
        } else if ((yaw >= 135 && yaw < 225) || (yaw >= -225 && yaw < -135)) {
            return Direction.NORTH;
        } else if ((yaw >= 225 && yaw < 315) || (yaw >= -135 && yaw < -45)) {
            return Direction.EAST;
        } else {
            return Direction.SOUTH;
        }
    }


}
