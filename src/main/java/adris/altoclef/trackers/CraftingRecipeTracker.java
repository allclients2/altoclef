package adris.altoclef.trackers;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.multiversion.recipemanager.RecipeManagerWrapper;
import adris.altoclef.multiversion.recipemanager.WrappedRecipeEntry;
import adris.altoclef.util.RecipeTarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.context.ContextParameterMap;

import java.lang.reflect.Field;
import java.util.*;

// TODO remove those ugly "ensureUpdate" statements, realistically we only need to update only upon joining a world
public class CraftingRecipeTracker extends Tracker{


    private final HashMap<Item, List<adris.altoclef.util.CraftingRecipe>> itemRecipeMap = new HashMap<>();
    private final HashMap<adris.altoclef.util.CraftingRecipe, ItemStack> recipeResultMap = new HashMap<>();

    private boolean shouldRebuild;

    public CraftingRecipeTracker(TrackerManager manager) {
        super(manager);
        shouldRebuild = true;
    }

    public List<adris.altoclef.util.CraftingRecipe> getRecipeForItem(Item item) {
        ensureUpdated();

        if (!hasRecipeForItem(item)) {
            Debug.logWarning("trying to access recipe for unknown item: "+item);
            return null;
        }

        return itemRecipeMap.get(item);
    }

    public adris.altoclef.util.CraftingRecipe getFirstRecipeForItem(Item item) {
        ensureUpdated();

        if (!hasRecipeForItem(item)) {
            Debug.logWarning("trying to access recipe for unknown item: " + item);
            return null;
        }

        return itemRecipeMap.get(item).get(0);
    }

    public List<RecipeTarget> getRecipeTarget(Item item, int targetCount) {
        ensureUpdated();

        List<RecipeTarget> targets = new ArrayList<>();
        for (adris.altoclef.util.CraftingRecipe recipe : getRecipeForItem(item)) {
            targets.add(new RecipeTarget(item, targetCount, recipe));
        }

        return targets;
    }

    public RecipeTarget getFirstRecipeTarget(Item item, int targetCount) {
        ensureUpdated();

        return new RecipeTarget(item, targetCount, getFirstRecipeForItem(item));
    }

    public boolean hasRecipeForItem(Item item) {
        ensureUpdated();
        return itemRecipeMap.containsKey(item);
    }

    public ItemStack getRecipeResult(adris.altoclef.util.CraftingRecipe recipe) {
        ensureUpdated();

        if (!hasRecipe(recipe)) {
            Debug.logWarning("Trying to get result for unknown recipe: "+recipe);
            return null;
        }
        ItemStack result = recipeResultMap.get(recipe);

        return new ItemStack(result.getItem(), result.getCount());
    }

    public boolean hasRecipe(adris.altoclef.util.CraftingRecipe recipe) {
        ensureUpdated();
        return recipeResultMap.containsKey(recipe);
    }

    @Override
    protected void updateState() {
        if (!shouldRebuild) return;

        // rebuild once we are in game
        if (!AltoClef.inGame()) return;

        ClientPlayNetworkHandler networkHandler =  MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) return;

        RecipeManagerWrapper recipeManager = RecipeManagerWrapper.of(networkHandler.getRecipeManager());

        for (WrappedRecipeEntry recipe : recipeManager.values()) {
            //TODO: version this, didn't because not too sure if this is necessary in new version?
            //#if MC >= 12103
            // if (recipe.displayEntry().category()) continue;
            //#else
            //$$ if (!(recipe.value() instanceof net.minecraft.recipe.CraftingRecipe craftingRecipe)) continue;
            //$$ if (craftingRecipe instanceof SpecialCraftingRecipe) continue;
            //#endif

            //#if MC >= 12103
            ItemStack result = getResultUsingReflection(recipe.displayEntry().getStacks(new ContextParameterMap.Builder().build()));
            //#elseif MC >= 11903
            //$$ // the arguments shouldn't be used, we can just pass null
            //$$ ItemStack result = new ItemStack(craftingRecipe.getResult(null).getItem(), craftingRecipe.getResult(null).getCount());
            //#else
            //$$ ItemStack result = new ItemStack(craftingRecipe.getOutput().getItem(), craftingRecipe.getOutput().getCount());
            //#endif

            //#if MC >= 12103
            Item[][] altoclefRecipeItems = getShapedCraftingRecipe(craftingRecipe.getIngredientPlacement().getIngredients());
            //#else
            //$$ Item[][] altoclefRecipeItems = getShapedCraftingRecipe(craftingRecipe.getIngredients());
            //#endif

            adris.altoclef.util.CraftingRecipe altoclefRecipe = adris.altoclef.util.CraftingRecipe.newShapedRecipe(altoclefRecipeItems, result.getCount());

            if (itemRecipeMap.containsKey(result.getItem())) {
                itemRecipeMap.get(result.getItem()).add(altoclefRecipe);
            } else {
                List<adris.altoclef.util.CraftingRecipe> recipes = new ArrayList<>();
                recipes.add(altoclefRecipe);

                itemRecipeMap.put(result.getItem(), recipes);
            }

            recipeResultMap.put(altoclefRecipe, result);
        }

        itemRecipeMap.replaceAll((k,v) -> Collections.unmodifiableList(v));

        shouldRebuild = false;
    }

    // TODO adjust for small recipes
    // it is always shaped, but that doesn't matter for shapeless
    // the second dimension of the array is for different types of items (eq. logs)
    //#if MC >= 12103
    private static Item[][] getShapedCraftingRecipe(List<Ingredient> ingredients) {
        Item[][] result = new Item[9][];
        for (int x = 0; x < ingredients.size(); x++) {
            List<RegistryEntry<Item>> matchingItems = ingredients.get(x).getMatchingItems();
            Item[] items = new Item[matchingItems.size()];
            for (int i = 0; i < matchingItems.size(); i++) {
                items[i] = matchingItems.get(i).value();
            }
            result[x] = matchingItems.isEmpty() ? null : new Item[]{items[0]}; // FIXME this is so stupid, but TaskCatalogue is kinda setup this way, so it would require a rewrite to allow for multiple resource :')
        }
        return result;
    }
    //#else
    //$$ private static Item[][] getShapedCraftingRecipe(List<Ingredient> ingredients) {
    //$$     Item[][] result = new Item[9][];
    //$$     for (int x = 0; x < ingredients.size(); x++) {
    //$$         ItemStack[] stacks = ingredients.get(x).getMatchingStacks();
    //$$         Item[] items = new Item[stacks.length];
    //$$         for (int i = 0; i < stacks.length; i++) {
    //$$             ItemStack stack = stacks[i];
    //$$             if (stack.getCount() > 1) {
    //$$                 throw new IllegalStateException("recipe requires more than one item in a slot? (ingredients: " + ingredients + ")");
    //$$             }
    //$$             items[i] = stack.getItem();
    //$$         }
    //$$         result[x] = (stacks.length == 0) ? null : new Item[]{items[0]}; // FIXME this is so stupid, but TaskCatalogue is kinda setup this way, so it would require a rewrite to allow for multiple resource :')
    //$$     }
    //$$     return result;
    //$$ }
    //#endif

    @Override
    protected void reset() {
       shouldRebuild = true;
       itemRecipeMap.clear();
       recipeResultMap.clear();
    }

    @Override
    protected boolean isDirty() {
        return shouldRebuild;
    }
}
