package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

public class BookCloningRecipe extends CustomRecipe {
	public BookCloningRecipe(CraftingBookCategory craftingBookCategory) {
		super(craftingBookCategory);
	}

	public boolean matches(CraftingInput craftingInput, Level level) {
		if (craftingInput.ingredientCount() < 2) {
			return false;
		} else {
			boolean bl = false;
			boolean bl2 = false;

			for (int i = 0; i < craftingInput.size(); i++) {
				ItemStack itemStack = craftingInput.getItem(i);
				if (!itemStack.isEmpty()) {
					if (itemStack.is(Items.WRITTEN_BOOK)) {
						if (bl2) {
							return false;
						}

						bl2 = true;
					} else {
						if (!itemStack.is(Items.WRITABLE_BOOK)) {
							return false;
						}

						bl = true;
					}
				}
			}

			return bl2 && bl;
		}
	}

	public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
		int i = 0;
		ItemStack itemStack = ItemStack.EMPTY;

		for (int j = 0; j < craftingInput.size(); j++) {
			ItemStack itemStack2 = craftingInput.getItem(j);
			if (!itemStack2.isEmpty()) {
				if (itemStack2.is(Items.WRITTEN_BOOK)) {
					if (!itemStack.isEmpty()) {
						return ItemStack.EMPTY;
					}

					itemStack = itemStack2;
				} else {
					if (!itemStack2.is(Items.WRITABLE_BOOK)) {
						return ItemStack.EMPTY;
					}

					i++;
				}
			}
		}

		WrittenBookContent writtenBookContent = itemStack.get(DataComponents.WRITTEN_BOOK_CONTENT);
		if (!itemStack.isEmpty() && i >= 1 && writtenBookContent != null) {
			WrittenBookContent writtenBookContent2 = writtenBookContent.tryCraftCopy();
			if (writtenBookContent2 == null) {
				return ItemStack.EMPTY;
			} else {
				ItemStack itemStack3 = itemStack.copyWithCount(i);
				itemStack3.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenBookContent2);
				return itemStack3;
			}
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);

		for (int i = 0; i < nonNullList.size(); i++) {
			ItemStack itemStack = craftingInput.getItem(i);
			ItemStack itemStack2 = itemStack.getItem().getCraftingRemainder();
			if (!itemStack2.isEmpty()) {
				nonNullList.set(i, itemStack2);
			} else if (itemStack.getItem() instanceof WrittenBookItem) {
				nonNullList.set(i, itemStack.copyWithCount(1));
				break;
			}
		}

		return nonNullList;
	}

	@Override
	public RecipeSerializer<BookCloningRecipe> getSerializer() {
		return RecipeSerializer.BOOK_CLONING;
	}
}
