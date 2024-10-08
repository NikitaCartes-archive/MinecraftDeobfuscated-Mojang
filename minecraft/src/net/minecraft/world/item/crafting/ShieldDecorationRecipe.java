package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class ShieldDecorationRecipe extends CustomRecipe {
	public ShieldDecorationRecipe(CraftingBookCategory craftingBookCategory) {
		super(craftingBookCategory);
	}

	public boolean matches(CraftingInput craftingInput, Level level) {
		if (craftingInput.ingredientCount() != 2) {
			return false;
		} else {
			boolean bl = false;
			boolean bl2 = false;

			for (int i = 0; i < craftingInput.size(); i++) {
				ItemStack itemStack = craftingInput.getItem(i);
				if (!itemStack.isEmpty()) {
					if (itemStack.getItem() instanceof BannerItem) {
						if (bl2) {
							return false;
						}

						bl2 = true;
					} else {
						if (!itemStack.is(Items.SHIELD)) {
							return false;
						}

						if (bl) {
							return false;
						}

						BannerPatternLayers bannerPatternLayers = itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
						if (!bannerPatternLayers.layers().isEmpty()) {
							return false;
						}

						bl = true;
					}
				}
			}

			return bl && bl2;
		}
	}

	public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
		ItemStack itemStack = ItemStack.EMPTY;
		ItemStack itemStack2 = ItemStack.EMPTY;

		for (int i = 0; i < craftingInput.size(); i++) {
			ItemStack itemStack3 = craftingInput.getItem(i);
			if (!itemStack3.isEmpty()) {
				if (itemStack3.getItem() instanceof BannerItem) {
					itemStack = itemStack3;
				} else if (itemStack3.is(Items.SHIELD)) {
					itemStack2 = itemStack3.copy();
				}
			}
		}

		if (itemStack2.isEmpty()) {
			return itemStack2;
		} else {
			itemStack2.set(DataComponents.BANNER_PATTERNS, itemStack.get(DataComponents.BANNER_PATTERNS));
			itemStack2.set(DataComponents.BASE_COLOR, ((BannerItem)itemStack.getItem()).getColor());
			return itemStack2;
		}
	}

	@Override
	public RecipeSerializer<ShieldDecorationRecipe> getSerializer() {
		return RecipeSerializer.SHIELD_DECORATION;
	}
}
