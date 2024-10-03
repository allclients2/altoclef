package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.Playground;
import adris.altoclef.Settings;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.util.DimensionedZone;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.util.math.BlockPos;

public class BlacklistCommand extends Command {

    public BlacklistCommand() throws CommandException {
        super("blacklist", "Use to blacklist an area for the block to not tamper or break. Current dimension and world specific.",
            new Arg<>(Integer.class, "x_start"),
            new Arg<>(Integer.class, "y_start"),
            new Arg<>(Integer.class, "z_start"),
            new Arg<>(Integer.class, "x_end"),
            new Arg<>(Integer.class, "y_end"),
            new Arg<>(Integer.class, "z_end")
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        var pos1 = new BlockPos(
                parser.get(Integer.class),
                parser.get(Integer.class),
                parser.get(Integer.class)
        );
        var pos2 = new BlockPos(
                parser.get(Integer.class),
                parser.get(Integer.class),
                parser.get(Integer.class)
        );

        mod.getModSettings().insertNewBlacklistedArea(
            new DimensionedZone(
                WorldHelper.getCurrentDimension(),
                WorldHelper.getNetworkName(),
                pos1,
                pos2
            )
        );

        Settings.save(mod.getModSettings()); // Save to file

        Debug.logMessage("Added zone to blacklist.");
    }
}