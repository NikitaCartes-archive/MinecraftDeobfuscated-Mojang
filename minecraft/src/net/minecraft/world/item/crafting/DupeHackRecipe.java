package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class DupeHackRecipe extends CustomRecipe {
	public DupeHackRecipe(ResourceLocation resourceLocation, CraftingBookCategory craftingBookCategory) {
		super(resourceLocation, craftingBookCategory);
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		boolean bl = false;
		ItemStack itemStack = ItemStack.EMPTY;

		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack itemStack2 = craftingContainer.getItem(i);
			if (!itemStack2.isEmpty()) {
				if (!bl && itemStack2.is(Items.DUPE_HACK)) {
					bl = true;
				} else {
					if (!itemStack.isEmpty()) {
						return false;
					}

					itemStack = itemStack2;
				}
			}
		}

		return !itemStack.isEmpty() && bl;
	}

	public ItemStack assembleRaw(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
		boolean bl = false;
		ItemStack itemStack = ItemStack.EMPTY;

		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack itemStack2 = craftingContainer.getItem(i);
			if (!itemStack2.isEmpty()) {
				if (!bl && itemStack2.is(Items.DUPE_HACK)) {
					bl = true;
				} else {
					if (!itemStack.isEmpty()) {
						return ItemStack.EMPTY;
					}

					itemStack = itemStack2;
				}
			}
		}

		if (!itemStack.isEmpty() && bl) {
			ItemStack itemStack3 = itemStack.copy();
			itemStack3.setCount(2);
			return itemStack3;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i >= 2 && j >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.DUPE_HACK;
	}
}
