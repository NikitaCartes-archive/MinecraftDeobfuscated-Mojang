package net.minecraft.world.item.crafting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;

public class BannerDuplicateRecipe extends CustomRecipe {
	public BannerDuplicateRecipe(ResourceLocation resourceLocation) {
		super(resourceLocation);
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		DyeColor dyeColor = null;
		ItemStack itemStack = null;
		ItemStack itemStack2 = null;

		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack itemStack3 = craftingContainer.getItem(i);
			Item item = itemStack3.getItem();
			if (item instanceof BannerItem) {
				BannerItem bannerItem = (BannerItem)item;
				if (dyeColor == null) {
					dyeColor = bannerItem.getColor();
				} else if (dyeColor != bannerItem.getColor()) {
					return false;
				}

				int j = BannerBlockEntity.getPatternCount(itemStack3);
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

	public ItemStack assemble(CraftingContainer craftingContainer) {
		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack itemStack = craftingContainer.getItem(i);
			if (!itemStack.isEmpty()) {
				int j = BannerBlockEntity.getPatternCount(itemStack);
				if (j > 0 && j <= 6) {
					ItemStack itemStack2 = itemStack.copy();
					itemStack2.setCount(1);
					return itemStack2;
				}
			}
		}

		return ItemStack.EMPTY;
	}

	public NonNullList<ItemStack> getRemainingItems(CraftingContainer craftingContainer) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingContainer.getContainerSize(), ItemStack.EMPTY);

		for (int i = 0; i < nonNullList.size(); i++) {
			ItemStack itemStack = craftingContainer.getItem(i);
			if (!itemStack.isEmpty()) {
				if (itemStack.getItem().hasCraftingRemainingItem()) {
					nonNullList.set(i, new ItemStack(itemStack.getItem().getCraftingRemainingItem()));
				} else if (itemStack.hasTag() && BannerBlockEntity.getPatternCount(itemStack) > 0) {
					ItemStack itemStack2 = itemStack.copy();
					itemStack2.setCount(1);
					nonNullList.set(i, itemStack2);
				}
			}
		}

		return nonNullList;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.BANNER_DUPLICATE;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= 2;
	}
}
