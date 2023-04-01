package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract class CustomRecipe implements CraftingRecipe {
	private final ResourceLocation id;
	private final CraftingBookCategory category;

	public CustomRecipe(ResourceLocation resourceLocation, CraftingBookCategory craftingBookCategory) {
		this.id = resourceLocation;
		this.category = craftingBookCategory;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public ItemStack getResultItemRaw(RegistryAccess registryAccess) {
		return ItemStack.EMPTY;
	}

	@Override
	public CraftingBookCategory category() {
		return this.category;
	}
}
