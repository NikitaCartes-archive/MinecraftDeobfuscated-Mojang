package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;

public class ArmorDyeRecipe extends CustomRecipe {
	public ArmorDyeRecipe(CraftingBookCategory craftingBookCategory) {
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
					if (itemStack.is(ItemTags.DYEABLE)) {
						if (bl) {
							return false;
						}

						bl = true;
					} else {
						if (!(itemStack.getItem() instanceof DyeItem)) {
							return false;
						}

						bl2 = true;
					}
				}
			}

			return bl2 && bl;
		}
	}

	public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
		List<DyeItem> list = new ArrayList();
		ItemStack itemStack = ItemStack.EMPTY;

		for (int i = 0; i < craftingInput.size(); i++) {
			ItemStack itemStack2 = craftingInput.getItem(i);
			if (!itemStack2.isEmpty()) {
				if (itemStack2.is(ItemTags.DYEABLE)) {
					if (!itemStack.isEmpty()) {
						return ItemStack.EMPTY;
					}

					itemStack = itemStack2.copy();
				} else {
					if (!(itemStack2.getItem() instanceof DyeItem dyeItem)) {
						return ItemStack.EMPTY;
					}

					list.add(dyeItem);
				}
			}
		}

		return !itemStack.isEmpty() && !list.isEmpty() ? DyedItemColor.applyDyes(itemStack, list) : ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<ArmorDyeRecipe> getSerializer() {
		return RecipeSerializer.ARMOR_DYE;
	}
}
