package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class TippedArrowRecipe extends CustomRecipe {
	public TippedArrowRecipe(CraftingBookCategory craftingBookCategory) {
		super(craftingBookCategory);
	}

	public boolean matches(CraftingInput craftingInput, Level level) {
		if (craftingInput.width() == 3 && craftingInput.height() == 3 && craftingInput.ingredientCount() == 9) {
			for (int i = 0; i < craftingInput.height(); i++) {
				for (int j = 0; j < craftingInput.width(); j++) {
					ItemStack itemStack = craftingInput.getItem(j, i);
					if (itemStack.isEmpty()) {
						return false;
					}

					if (j == 1 && i == 1) {
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

	public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
		ItemStack itemStack = craftingInput.getItem(1, 1);
		if (!itemStack.is(Items.LINGERING_POTION)) {
			return ItemStack.EMPTY;
		} else {
			ItemStack itemStack2 = new ItemStack(Items.TIPPED_ARROW, 8);
			itemStack2.set(DataComponents.POTION_CONTENTS, itemStack.get(DataComponents.POTION_CONTENTS));
			return itemStack2;
		}
	}

	@Override
	public RecipeSerializer<TippedArrowRecipe> getSerializer() {
		return RecipeSerializer.TIPPED_ARROW;
	}
}
