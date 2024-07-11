package adris.altoclef.commands;

import java.util.*;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.commandsystem.ItemList;
import adris.altoclef.tasks.construction.BranchMiningTask;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.publictypes.OreType;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static adris.altoclef.util.helpers.ItemHelper.MATERIAL_DATA;
import static adris.altoclef.util.helpers.ItemHelper.OreBlockData;

public class BranchMineCommand extends Command {

    public BranchMineCommand() throws CommandException {
        super("branchmine", "Branch mine from the current direction", new Arg(ItemList.class, MATERIAL_DATA.keySet().toString().toLowerCase()));
    }

    private static void OnResourceDoesNotExist(AltoClef mod, String resource) {
        Debug.logInternal("\"" + resource + "\" is not a catalogued ores. Can't get it yet, sorry!", MessagePriority.OPTIONAL);
        Debug.logInternal("List of available items: ", MessagePriority.OPTIONAL);
        for (OreType key : MATERIAL_DATA.keySet()) {
            Debug.logInternal("	\"" + key.name() + "\"", MessagePriority.OPTIONAL);
        }
    }

    private void GetItems(AltoClef mod, OreType oreType) {
        if (oreType == null) {
            Debug.logInternal("Must specify an ore type!");
            finish();
            return;
        }

        final BlockPos currentPlayerPos = mod.getPlayer().getBlockPos();

        final List<OreBlockData> blocksToMineData = Arrays.asList(MATERIAL_DATA.get(oreType).oreBlocks);
        final OreBlockData finalTargetData = getOreBlockData(currentPlayerPos, blocksToMineData);

        if (finalTargetData == null) {
            Debug.logWarning("Failed to get `finalTargetData`! blocksToMineData: " + blocksToMineData);
            return;
        }

        final BlockPos homePos = new BlockPos(currentPlayerPos.getX(), finalTargetData.distribution.optimalHeight, currentPlayerPos.getZ());
        mod.runUserTask(new BranchMiningTask(
                homePos,
                mod.getPlayer().getMovementDirection(),
                finalTargetData.oreBlock
        ), this::finish);
    }

    private static OreBlockData getOreBlockData(BlockPos currentPlayerPos, List<OreBlockData> blocksToMineData) {
        final int currentYPos = currentPlayerPos.getY();

        OreBlockData finalTargetData = null;
        int bestTargetDiff = 2147483647;

        for (OreBlockData oreBlockData : blocksToMineData) {
            final int targetDiff = Math.abs(oreBlockData.distribution.optimalHeight - currentYPos);
            if (bestTargetDiff > targetDiff) {
                bestTargetDiff = targetDiff;
                finalTargetData = oreBlockData;
            }
        }

        /*
        if (finalTargetData == null) {
            throw new RuntimeException("Failed to get finalTargetData, resulting in `finalTargetData` being null!");
        }
        */
        return finalTargetData;
    }

    //	@Override
    //    protected void call(AltoClef mod, ArgParser parser) {
    //
    //		mod.runUserTask(new BranchMiningTask(
    //				mod.getPlayer().getBlockPos(),
    //				mod.getPlayer().getMovementDirection(),
    //				Blocks.REDSTONE_ORE
    //				), this::finish);
    //    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        final String[] args = parser.getArgUnits();

        if (args.length == 0) {
            Debug.logWarning("No oretype specified!");
            return;
        }

        final String name = args[0];
        for (OreType oreType : OreType.values()) {
            if (Objects.equals(oreType.name().toLowerCase(), name)) {
                GetItems(mod, oreType);
                return;
            }
        };
        Debug.logWarning("Oretype named `" + name + "` is not valid!");
    }
}
