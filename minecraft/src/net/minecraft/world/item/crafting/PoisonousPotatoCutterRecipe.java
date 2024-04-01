package net.minecraft.world.item.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class PoisonousPotatoCutterRecipe extends SingleItemRecipe {
	public PoisonousPotatoCutterRecipe(String string, Ingredient ingredient, ItemStack itemStack) {
		super(RecipeType.POISONOUS_POTATO_CUTTING, RecipeSerializer.POISONOUS_POTATO_CUTTER_RECIPE, string, ingredient, itemStack);
	}

	@Override
	public boolean matches(Container container, Level level) {
		return this.ingredient.test(container.getItem(0));
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Blocks.POISONOUS_POTATO_CUTTER);
	}
}
