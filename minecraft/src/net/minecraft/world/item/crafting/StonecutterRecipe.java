package net.minecraft.world.item.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class StonecutterRecipe extends SingleItemRecipe {
	public StonecutterRecipe(String string, Ingredient ingredient, ItemStack itemStack) {
		super(RecipeType.STONECUTTING, RecipeSerializer.STONECUTTER, string, ingredient, itemStack);
	}

	@Override
	public boolean matches(Container container, Level level) {
		return this.ingredient.test(container.getItem(0));
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.STONECUTTER);
	}
}
