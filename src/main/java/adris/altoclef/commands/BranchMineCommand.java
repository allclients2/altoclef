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

import static adris.altoclef.util.helpers.ItemHelper.MATERIAL_DATA;
import static adris.altoclef.util.helpers.ItemHelper.OreBlockData;

public class BranchMineCommand extends Command {

    //private static Optional<Item> nameStringToItem(String name) {
    //		Identifier identifier = new Identifier(name);
    //		if (RegistriesVer.itemsRegistry().containsId(identifier)) {
    //			return Optional.of(RegistriesVer.itemsRegistry().get(identifier));
    //		} else {
    //			Debug.logWarning("Invalid item name:" + name);
    //			return Optional.empty();
    //		}
    //	}

    public BranchMineCommand() throws CommandException {
        super("branchmine", "Branch mine from the current direction", new Arg(ItemList.class, MATERIAL_DATA.keySet().toString()));
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
            Debug.logInternal("You must specify an ore type!");
            finish();
            return;
        }

        final BlockPos currentPlayerPos = mod.getPlayer().getBlockPos();

        final List<OreBlockData> blocksToMineData;
        final int currentYPos = currentPlayerPos.getY();
        if (!MATERIAL_DATA.containsKey(oreType)) {
            Debug.logInternal("Unexpected value: " + oreType.toString() + ", expected any of: " + MATERIAL_DATA.keySet(), MessagePriority.OPTIONAL);
            finish();
            return;
        } else {
            blocksToMineData = new ArrayList<>(Arrays.asList(MATERIAL_DATA.get(oreType).oreBlocks));
        }

        OreBlockData finalTargetData = null;
        int bestTargetDiff = (2 ^ 31 - 1);

        for (OreBlockData oreBlockData : blocksToMineData) {
            final int targetDiff = oreBlockData.distribution.optimalHeight;
            if (bestTargetDiff > Math.abs(targetDiff - currentYPos)) {
                bestTargetDiff = targetDiff;
                finalTargetData = oreBlockData;
            }
        }

        if (finalTargetData == null) {
            throw new RuntimeException("Failed to get finalTargetData, resulting in `finalTargetData` being null!");
        }

        final BlockPos homePos = new BlockPos(currentPlayerPos.getX(), finalTargetData.distribution.optimalHeight, currentPlayerPos.getZ());
        mod.runUserTask(new BranchMiningTask(
                homePos,
                mod.getPlayer().getMovementDirection(),
                finalTargetData.oreBlock
        ), this::finish);
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
        OreType items = parser.get(OreType.class);
        GetItems(mod, items);
    }
}
