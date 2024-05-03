package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;

public record SmithingRecipeInput(ItemStack template, ItemStack base, ItemStack addition) implements RecipeInput {
	@Override
	public ItemStack getItem(int i) {
		return switch (i) {
			case 0 -> this.template;
			case 1 -> this.base;
			case 2 -> this.addition;
			default -> throw new IllegalArgumentException("Recipe does not contain slot " + i);
		};
	}

	@Override
	public int size() {
		return 3;
	}

	@Override
	public boolean isEmpty() {
		return this.template.isEmpty() && this.base.isEmpty() && this.addition.isEmpty();
	}
}
