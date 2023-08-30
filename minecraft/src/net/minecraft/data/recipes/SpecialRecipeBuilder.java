package net.minecraft.data.recipes;

import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SpecialRecipeBuilder extends CraftingRecipeBuilder {
	final RecipeSerializer<?> serializer;

	public SpecialRecipeBuilder(RecipeSerializer<?> recipeSerializer) {
		this.serializer = recipeSerializer;
	}

	public static SpecialRecipeBuilder special(RecipeSerializer<? extends CraftingRecipe> recipeSerializer) {
		return new SpecialRecipeBuilder(recipeSerializer);
	}

	public void save(RecipeOutput recipeOutput, String string) {
		this.save(recipeOutput, new ResourceLocation(string));
	}

	public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
		recipeOutput.accept(new CraftingRecipeBuilder.CraftingResult(CraftingBookCategory.MISC) {
			@Override
			public RecipeSerializer<?> type() {
				return SpecialRecipeBuilder.this.serializer;
			}

			@Override
			public ResourceLocation id() {
				return resourceLocation;
			}

			@Nullable
			@Override
			public AdvancementHolder advancement() {
				return null;
			}
		});
	}
}
