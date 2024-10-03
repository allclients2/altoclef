package adris.altoclef.util;

import adris.altoclef.util.publicenums.Dimension;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public record DimensionedZone(Dimension dimension, String networkName, BlockPos pos1, BlockPos pos2) {
    public boolean isIncludedInZone(Dimension dimension2, String networkName2, BlockPos pos) {
        if (dimension != dimension2) return false;
        if (!Objects.equals(networkName, networkName2)) return false;

        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());

        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return (minX <= pos.getX() && pos.getX() <= maxX) &&
                (minY <= pos.getY() && pos.getY() <= maxY) &&
                (minZ <= pos.getZ() && pos.getZ() <= maxZ);
    }
}
