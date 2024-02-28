package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

public class BookCloningRecipe extends CustomRecipe {
	public BookCloningRecipe(CraftingBookCategory craftingBookCategory) {
		super(craftingBookCategory);
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		int i = 0;
		ItemStack itemStack = ItemStack.EMPTY;

		for (int j = 0; j < craftingContainer.getContainerSize(); j++) {
			ItemStack itemStack2 = craftingContainer.getItem(j);
			if (!itemStack2.isEmpty()) {
				if (itemStack2.is(Items.WRITTEN_BOOK)) {
					if (!itemStack.isEmpty()) {
						return false;
					}

					itemStack = itemStack2;
				} else {
					if (!itemStack2.is(Items.WRITABLE_BOOK)) {
						return false;
					}

					i++;
				}
			}
		}

		return !itemStack.isEmpty() && i > 0;
	}

	public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
		int i = 0;
		ItemStack itemStack = ItemStack.EMPTY;

		for (int j = 0; j < craftingContainer.getContainerSize(); j++) {
			ItemStack itemStack2 = craftingContainer.getItem(j);
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

	public NonNullList<ItemStack> getRemainingItems(CraftingContainer craftingContainer) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingContainer.getContainerSize(), ItemStack.EMPTY);

		for (int i = 0; i < nonNullList.size(); i++) {
			ItemStack itemStack = craftingContainer.getItem(i);
			if (itemStack.getItem().hasCraftingRemainingItem()) {
				nonNullList.set(i, new ItemStack(itemStack.getItem().getCraftingRemainingItem()));
			} else if (itemStack.getItem() instanceof WrittenBookItem) {
				nonNullList.set(i, itemStack.copyWithCount(1));
				break;
			}
		}

		return nonNullList;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.BOOK_CLONING;
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i >= 3 && j >= 3;
	}
}
