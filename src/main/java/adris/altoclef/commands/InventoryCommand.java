package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.multiversion.PlayerVer;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class InventoryCommand extends Command {
    public InventoryCommand() throws CommandException {
        super("inventory", "Prints the bot's inventory OR returns how many of an item the bot has", new Arg<>(String.class, "item", null, 1));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String item = parser.get(String.class);
        if (item == null) {
            // Print inventory
            // Get item counts
            HashMap<String, Integer> counts = new HashMap<>();
            for (int i = 0; i < PlayerVer.getInventory(mod).size(); ++i) {
                ItemStack stack = PlayerVer.getInventory(mod).getStack(i);
                if (!stack.isEmpty()) {
                    String name = ItemHelper.stripItemName(stack.getItem());
                    if (!counts.containsKey(name)) counts.put(name, 0);
                    counts.put(name, counts.get(name) + stack.getCount());
                }
            }
            // Print
            Debug.logInternal("INVENTORY: ", MessagePriority.OPTIONAL);
            for (String name : counts.keySet()) {
                Debug.logInternal(name + " : " + counts.get(name), MessagePriority.OPTIONAL);
            }
            Debug.logInternal("(inventory list sent) ", MessagePriority.OPTIONAL);
        } else {
            // Print item quantity
            Item[] matches = TaskCatalogue.getItemMatches(item);
            if (matches == null || matches.length == 0) {
                Debug.logWarning("Item \"" + item + "\" is not catalogued/recognized.");
                finish();
                return;
            }
            int count = mod.getItemStorage().getItemCount(matches);
            if (count == 0) {
                Debug.logInternal(item + " COUNT: (none)");
            } else {
                Debug.logInternal(item + " COUNT: " + count);
            }
        }
        finish();
    }
}