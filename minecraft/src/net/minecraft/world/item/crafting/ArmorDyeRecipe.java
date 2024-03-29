package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;

public class ArmorDyeRecipe extends CustomRecipe {
	public ArmorDyeRecipe(CraftingBookCategory craftingBookCategory) {
		super(craftingBookCategory);
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		ItemStack itemStack = ItemStack.EMPTY;
		List<ItemStack> list = Lists.<ItemStack>newArrayList();

		for(int i = 0; i < craftingContainer.getContainerSize(); ++i) {
			ItemStack itemStack2 = craftingContainer.getItem(i);
			if (!itemStack2.isEmpty()) {
				if (itemStack2.is(ItemTags.DYEABLE)) {
					if (!itemStack.isEmpty()) {
						return false;
					}

					itemStack = itemStack2;
				} else {
					if (!(itemStack2.getItem() instanceof DyeItem)) {
						return false;
					}

					list.add(itemStack2);
				}
			}
		}

		return !itemStack.isEmpty() && !list.isEmpty();
	}

	public ItemStack assemble(CraftingContainer craftingContainer, HolderLookup.Provider provider) {
		List<DyeItem> list = Lists.<DyeItem>newArrayList();
		ItemStack itemStack = ItemStack.EMPTY;

		for(int i = 0; i < craftingContainer.getContainerSize(); ++i) {
			ItemStack itemStack2 = craftingContainer.getItem(i);
			if (!itemStack2.isEmpty()) {
				if (itemStack2.is(ItemTags.DYEABLE)) {
					if (!itemStack.isEmpty()) {
						return ItemStack.EMPTY;
					}

					itemStack = itemStack2.copy();
				} else {
					Item var8 = itemStack2.getItem();
					if (!(var8 instanceof DyeItem)) {
						return ItemStack.EMPTY;
					}

					DyeItem dyeItem = (DyeItem)var8;
					list.add(dyeItem);
				}
			}
		}

		return !itemStack.isEmpty() && !list.isEmpty() ? DyedItemColor.applyDyes(itemStack, list) : ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.ARMOR_DYE;
	}
}
