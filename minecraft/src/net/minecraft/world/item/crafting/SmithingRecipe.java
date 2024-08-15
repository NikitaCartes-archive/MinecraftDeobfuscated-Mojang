package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public interface SmithingRecipe extends Recipe<SmithingRecipeInput> {
	@Override
	default RecipeType<?> getType() {
		return RecipeType.SMITHING;
	}

	@Override
	default boolean canCraftInDimensions(int i, int j) {
		return i >= 3 && j >= 1;
	}

	@Override
	default ItemStack getCategoryIconItem() {
		return new ItemStack(Blocks.SMITHING_TABLE);
	}

	default boolean matches(SmithingRecipeInput smithingRecipeInput, Level level) {
		return this.isTemplateIngredient(smithingRecipeInput.template())
			&& this.isBaseIngredient(smithingRecipeInput.base())
			&& this.isAdditionIngredient(smithingRecipeInput.addition());
	}

	boolean isTemplateIngredient(ItemStack itemStack);

	boolean isBaseIngredient(ItemStack itemStack);

	boolean isAdditionIngredient(ItemStack itemStack);
}
