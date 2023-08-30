package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

public abstract class CustomRecipe implements CraftingRecipe {
	private final CraftingBookCategory category;

	public CustomRecipe(CraftingBookCategory craftingBookCategory) {
		this.category = craftingBookCategory;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return ItemStack.EMPTY;
	}

	@Override
	public CraftingBookCategory category() {
		return this.category;
	}
}
