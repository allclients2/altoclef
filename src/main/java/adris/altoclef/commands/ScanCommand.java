package adris.altoclef.commands;

import adris.altoclef.AltoClef;

import adris.altoclef.Debug;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.multiversion.IdentifierVer;
import adris.altoclef.multiversion.RegistriesVer;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import adris.altoclef.scanner.BlockScanner;

import java.util.Objects;
import java.util.Optional;

public class ScanCommand extends Command {

    public ScanCommand() throws CommandException {
        super("scan", "Locates nearest block", new Arg<>(String.class, "block", "DIRT", 0));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        final String blockName = parser.get(String.class);
        Block block = null;

        try {
            final String blockNameTrimmed = ItemHelper.trimItemName(blockName);
            for (Block block2 : RegistriesVer.blockRegistry()) {
                if (ItemHelper.trimItemName(block2.getTranslationKey()).equals(blockNameTrimmed)) {
                    block = block2;
                }
            }
        } catch (Exception e) {
            Debug.logWarning("Search exception caught! (See logs)");
            e.printStackTrace();
            return;
        }

        if (block == null) {
            Debug.logWarning("Block specified: \"" + blockName + "\" is not valid.");
            return;
        }

        BlockScanner blockScanner = mod.getBlockScanner();

        Optional<BlockPos> scannedBlockPos = blockScanner.getNearestBlock(block, mod.getPlayer().getPos(), Integer.MAX_VALUE);

        if (scannedBlockPos.isPresent()) {
            Debug.logMessage("Found! Closest Block Location: " +  scannedBlockPos.get());
        } else {
            Debug.logMessage("No Blocks Found.");
        }
    }

}