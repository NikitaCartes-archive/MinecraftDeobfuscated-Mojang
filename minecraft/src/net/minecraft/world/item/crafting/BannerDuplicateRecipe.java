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
		if (craftingInput.ingredientCount() != 2) {
			return false;
		} else {
			DyeColor dyeColor = null;
			boolean bl = false;
			boolean bl2 = false;

			for (int i = 0; i < craftingInput.size(); i++) {
				ItemStack itemStack = craftingInput.getItem(i);
				if (!itemStack.isEmpty()) {
					Item item = itemStack.getItem();
					if (!(item instanceof BannerItem)) {
						return false;
					}

					BannerItem bannerItem = (BannerItem)item;
					if (dyeColor == null) {
						dyeColor = bannerItem.getColor();
					} else if (dyeColor != bannerItem.getColor()) {
						return false;
					}

					int j = itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().size();
					if (j > 6) {
						return false;
					}

					if (j > 0) {
						if (bl2) {
							return false;
						}

						bl2 = true;
					} else {
						if (bl) {
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

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);

		for (int i = 0; i < nonNullList.size(); i++) {
			ItemStack itemStack = craftingInput.getItem(i);
			if (!itemStack.isEmpty()) {
				ItemStack itemStack2 = itemStack.getItem().getCraftingRemainder();
				if (!itemStack2.isEmpty()) {
					nonNullList.set(i, itemStack2);
				} else if (!itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().isEmpty()) {
					nonNullList.set(i, itemStack.copyWithCount(1));
				}
			}
		}

		return nonNullList;
	}

	@Override
	public RecipeSerializer<BannerDuplicateRecipe> getSerializer() {
		return RecipeSerializer.BANNER_DUPLICATE;
	}
}
