package net.minecraft.world.item.crafting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class CampfireCookingRecipe extends AbstractCookingRecipe {
	public CampfireCookingRecipe(ResourceLocation resourceLocation, String string, Ingredient ingredient, ItemStack itemStack, float f, int i) {
		super(RecipeType.CAMPFIRE_COOKING, resourceLocation, string, ingredient, itemStack, f, i);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.CAMPFIRE);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.CAMPFIRE_COOKING_RECIPE;
	}
}
