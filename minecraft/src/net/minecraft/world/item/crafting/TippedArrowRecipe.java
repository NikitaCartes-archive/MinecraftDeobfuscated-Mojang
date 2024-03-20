package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class TippedArrowRecipe extends CustomRecipe {
	public TippedArrowRecipe(CraftingBookCategory craftingBookCategory) {
		super(craftingBookCategory);
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		if (craftingContainer.getWidth() == 3 && craftingContainer.getHeight() == 3) {
			for (int i = 0; i < craftingContainer.getWidth(); i++) {
				for (int j = 0; j < craftingContainer.getHeight(); j++) {
					ItemStack itemStack = craftingContainer.getItem(i + j * craftingContainer.getWidth());
					if (itemStack.isEmpty()) {
						return false;
					}

					if (i == 1 && j == 1) {
						if (!itemStack.is(Items.LINGERING_POTION)) {
							return false;
						}
					} else if (!itemStack.is(Items.ARROW)) {
						return false;
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}

	public ItemStack assemble(CraftingContainer craftingContainer, HolderLookup.Provider provider) {
		ItemStack itemStack = craftingContainer.getItem(1 + craftingContainer.getWidth());
		if (!itemStack.is(Items.LINGERING_POTION)) {
			return ItemStack.EMPTY;
		} else {
			ItemStack itemStack2 = new ItemStack(Items.TIPPED_ARROW, 8);
			itemStack2.set(DataComponents.POTION_CONTENTS, itemStack.get(DataComponents.POTION_CONTENTS));
			return itemStack2;
		}
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i >= 2 && j >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.TIPPED_ARROW;
	}
}
