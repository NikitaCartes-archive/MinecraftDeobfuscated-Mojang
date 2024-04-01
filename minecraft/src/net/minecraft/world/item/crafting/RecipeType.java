package net.minecraft.world.item.crafting;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public interface RecipeType<T extends Recipe<?>> {
	RecipeType<CraftingRecipe> CRAFTING = register("crafting");
	RecipeType<SmeltingRecipe> SMELTING = register("smelting");
	RecipeType<BlastingRecipe> BLASTING = register("blasting");
	RecipeType<SmokingRecipe> SMOKING = register("smoking");
	RecipeType<CampfireCookingRecipe> CAMPFIRE_COOKING = register("campfire_cooking");
	RecipeType<StonecutterRecipe> STONECUTTING = register("stonecutting");
	RecipeType<SmithingRecipe> SMITHING = register("smithing");
	RecipeType<PoisonousPotatoCutterRecipe> POISONOUS_POTATO_CUTTING = register("poisonous_potato_cutting");
	RecipeType<PotatoRefinementRecipe> POTATO_REFINEMENT = register("potato_refinement");

	static <T extends Recipe<?>> RecipeType<T> register(String string) {
		return Registry.register(BuiltInRegistries.RECIPE_TYPE, new ResourceLocation(string), new RecipeType<T>() {
			public String toString() {
				return string;
			}
		});
	}
}
