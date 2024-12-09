package adris.altoclef.multiversion.recipemanager;

import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.recipe.Recipe;
//#if MC>12001
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.RecipeEntry;
//#endif

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

//this will rely on the crafting book 1.21.2+
public record WrappedRecipeEntry(
        Identifier id,
        //#if MC >= 12103
        NetworkRecipeId networkId,
        RecipeDisplayEntry displayEntry
        //#else
        //$$ Recipe<?> value
        //#endif
) {

    //#if MC >= 12103
    //$$ public RecipeEntry<?> asRecipe() {
    //$$     return new RecipeEntry<Recipe<?>>(RegistryKey.of(RegistryKeys.RECIPE, id), value);
    //$$ }
    //#elseif MC > 12001
    //$$ public RecipeEntry<?> asRecipe() {
    //$$     return new RecipeEntry<Recipe<?>>(RegistryKey.of(RegistryKeys.RECIPE, id), value);
    //$$ }
    //#else
    //$$ public Recipe<?> asRecipe() {
    //$$     return value;
    //$$ }
    //#endif

}
