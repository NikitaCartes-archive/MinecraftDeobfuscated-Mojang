package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.CraftingBookCategory;

public abstract class CraftingRecipeBuilder {
	protected static CraftingBookCategory determineBookCategory(RecipeCategory recipeCategory) {
		return switch (recipeCategory) {
			case BUILDING_BLOCKS -> CraftingBookCategory.BUILDING;
			case TOOLS, COMBAT -> CraftingBookCategory.EQUIPMENT;
			case REDSTONE -> CraftingBookCategory.REDSTONE;
			default -> CraftingBookCategory.MISC;
		};
	}

	protected abstract static class CraftingResult implements FinishedRecipe {
		private final CraftingBookCategory category;

		protected CraftingResult(CraftingBookCategory craftingBookCategory) {
			this.category = craftingBookCategory;
		}

		@Override
		public void serializeRecipeData(JsonObject jsonObject) {
			jsonObject.addProperty("category", this.category.getSerializedName());
		}
	}
}
