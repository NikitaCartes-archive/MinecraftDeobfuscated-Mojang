package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class BannerDuplicateRecipe extends CustomRecipe {
	public BannerDuplicateRecipe(CraftingBookCategory craftingBookCategory) {
		super(craftingBookCategory);
	}

	public boolean matches(CraftingInput craftingInput, Level level) {
		DyeColor dyeColor = null;
		ItemStack itemStack = null;
		ItemStack itemStack2 = null;

		for (int i = 0; i < craftingInput.size(); i++) {
			ItemStack itemStack3 = craftingInput.getItem(i);
			if (!itemStack3.isEmpty()) {
				Item item = itemStack3.getItem();
				if (!(item instanceof BannerItem)) {
					return false;
				}

				BannerItem bannerItem = (BannerItem)item;
				if (dyeColor == null) {
					dyeColor = bannerItem.getColor();
				} else if (dyeColor != bannerItem.getColor()) {
					return false;
				}

				int j = itemStack3.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().size();
				if (j > 6) {
					return false;
				}

				if (j > 0) {
					if (itemStack != null) {
						return false;
					}

					itemStack = itemStack3;
				} else {
					if (itemStack2 != null) {
						return false;
					}

					itemStack2 = itemStack3;
				}
			}
		}

		return itemStack != null && itemStack2 != null;
	}

	public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
		for (int i = 0; i < craftingInput.size(); i++) {
			ItemStack itemStack = craftingInput.getItem(i);
			if (!itemStack.isEmpty()) {
				int j = itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().size();
				if (j > 0 && j <= 6) {
					return itemStack.copyWithCount(1);
				}
			}
		}

		return ItemStack.EMPTY;
	}

	public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);

		for (int i = 0; i < nonNullList.size(); i++) {
			ItemStack itemStack = craftingInput.getItem(i);
			if (!itemStack.isEmpty()) {
				if (itemStack.getItem().hasCraftingRemainingItem()) {
					nonNullList.set(i, new ItemStack(itemStack.getItem().getCraftingRemainingItem()));
				} else if (!itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().isEmpty()) {
					nonNullList.set(i, itemStack.copyWithCount(1));
				}
			}
		}

		return nonNullList;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.BANNER_DUPLICATE;
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= 2;
	}
}
