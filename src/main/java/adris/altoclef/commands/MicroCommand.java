package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.speedrun.maintasks.BeatMinecraftTask;
import adris.altoclef.tasks.speedrun.maintasks.BeatMinecraftTaskMicro;

public class MicroCommand extends Command {
    public MicroCommand() {
        super("micro", "Beats the game, using under 320 lines of code.");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.runUserTask(new BeatMinecraftTaskMicro(), this::finish);
    }
}