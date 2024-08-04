package adris.altoclef.eventbus.events;

import adris.altoclef.util.publicenums.Dimension;
import net.minecraft.world.World;

public record DimensionChangedEvent(Dimension dimension, World world) { }
