/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;

public interface RecipeType<T extends Recipe<?>> {
    public static final RecipeType<CraftingRecipe> CRAFTING = RecipeType.register("crafting");
    public static final RecipeType<SmeltingRecipe> SMELTING = RecipeType.register("smelting");
    public static final RecipeType<BlastingRecipe> BLASTING = RecipeType.register("blasting");
    public static final RecipeType<SmokingRecipe> SMOKING = RecipeType.register("smoking");
    public static final RecipeType<CampfireCookingRecipe> CAMPFIRE_COOKING = RecipeType.register("campfire_cooking");
    public static final RecipeType<StonecutterRecipe> STONECUTTING = RecipeType.register("stonecutting");
    public static final RecipeType<UpgradeRecipe> SMITHING = RecipeType.register("smithing");

    public static <T extends Recipe<?>> RecipeType<T> register(final String string) {
        return Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(string), new RecipeType<T>(){

            public String toString() {
                return string;
            }
        });
    }

    default public <C extends Container> Optional<T> tryMatch(Recipe<C> recipe, Level level, C container) {
        return recipe.matches(container, level) ? Optional.of(recipe) : Optional.empty();
    }
}

