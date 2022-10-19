/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

public class SpecialRecipeBuilder
extends CraftingRecipeBuilder {
    final RecipeSerializer<?> serializer;

    public SpecialRecipeBuilder(RecipeSerializer<?> recipeSerializer) {
        this.serializer = recipeSerializer;
    }

    public static SpecialRecipeBuilder special(RecipeSerializer<? extends CraftingRecipe> recipeSerializer) {
        return new SpecialRecipeBuilder(recipeSerializer);
    }

    public void save(Consumer<FinishedRecipe> consumer, final String string) {
        consumer.accept(new CraftingRecipeBuilder.CraftingResult(CraftingBookCategory.MISC){

            @Override
            public RecipeSerializer<?> getType() {
                return SpecialRecipeBuilder.this.serializer;
            }

            @Override
            public ResourceLocation getId() {
                return new ResourceLocation(string);
            }

            @Override
            @Nullable
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Override
            public ResourceLocation getAdvancementId() {
                return new ResourceLocation("");
            }
        });
    }
}

