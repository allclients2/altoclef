package adris.altoclef.multiversion.recipemanager;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RecipeManagerWrapper {

    private final RecipeManager recipeManager;

    public static RecipeManagerWrapper of(RecipeManager recipeManager) {
        if (recipeManager == null) return null;

        return new RecipeManagerWrapper(recipeManager);
    }


    private RecipeManagerWrapper(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    // must parse here because local recipe parsing functionality was removed in 1.21.2 :(
    // https://fabricmc.net/2024/10/14/1212.html
    // TODO: make sure this custom parser method works.
    public static Map<Identifier, Recipe<?>> loadRecipes() {
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        Map<Identifier, Recipe<?>> recipes = new TreeMap<>();

        resourceManager.findResources("recipes", path -> path.getPath().endsWith(".json")).forEach((id, resource) -> {
            try (var reader = resource.getReader()) {
                Recipe<?> recipe = Recipe.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
                recipes.put(id, recipe);
            } catch (IOException e) {
                System.err.println("failed to read recipe? (id: exception) -> " + id + ": " + e.getMessage());
            }
        });

        return recipes;
    }

    //#if MC>=12103
    public Collection<WrappedRecipeEntry> values() {
        return loadRecipes().entrySet().stream()
            .map(entry -> new WrappedRecipeEntry(entry.getKey(), entry.getValue()))
            .collect(Collectors.toSet());
    }
    //#elseif MC>12001
    //$$ public Collection<WrappedRecipeEntry> values() {
    //$$     return recipeManager.values().stream().map(r -> new WrappedRecipeEntry(r.id(),r.value())).collect(Collectors.toSet());
    //$$ }
    //#else
    //$$ public Collection<WrappedRecipeEntry> values() {
    //$$    List<WrappedRecipeEntry> result = new ArrayList<>();
    //$$    for (Identifier id : recipeManager.keys().toList()) {
    //$$        result.add(new WrappedRecipeEntry(id, recipeManager.get(id).get()));
    //$$    }
    //$$
    //$$    return result;
    //$$ }
    //#endif



}
