package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.Level;

public class BookCloningRecipe extends CustomRecipe {
	public BookCloningRecipe(ResourceLocation resourceLocation) {
		super(resourceLocation);
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

		return !itemStack.isEmpty() && itemStack.hasTag() && i > 0;
	}

	public ItemStack assemble(CraftingContainer craftingContainer) {
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

		if (!itemStack.isEmpty() && itemStack.hasTag() && i >= 1 && WrittenBookItem.getGeneration(itemStack) < 2) {
			ItemStack itemStack3 = new ItemStack(Items.WRITTEN_BOOK, i);
			CompoundTag compoundTag = itemStack.getTag().copy();
			compoundTag.putInt("generation", WrittenBookItem.getGeneration(itemStack) + 1);
			itemStack3.setTag(compoundTag);
			return itemStack3;
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
				ItemStack itemStack2 = itemStack.copy();
				itemStack2.setCount(1);
				nonNullList.set(i, itemStack2);
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
