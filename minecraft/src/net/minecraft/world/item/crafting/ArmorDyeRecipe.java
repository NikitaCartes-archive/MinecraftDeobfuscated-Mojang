package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ArmorDyeRecipe extends CustomRecipe {
	public ArmorDyeRecipe(ResourceLocation resourceLocation) {
		super(resourceLocation);
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		ItemStack itemStack = ItemStack.EMPTY;
		List<ItemStack> list = Lists.<ItemStack>newArrayList();

		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack itemStack2 = craftingContainer.getItem(i);
			if (!itemStack2.isEmpty()) {
				if (itemStack2.getItem() instanceof DyeableLeatherItem) {
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

	public ItemStack assemble(CraftingContainer craftingContainer) {
		List<DyeItem> list = Lists.<DyeItem>newArrayList();
		ItemStack itemStack = ItemStack.EMPTY;

		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack itemStack2 = craftingContainer.getItem(i);
			if (!itemStack2.isEmpty()) {
				Item item = itemStack2.getItem();
				if (item instanceof DyeableLeatherItem) {
					if (!itemStack.isEmpty()) {
						return ItemStack.EMPTY;
					}

					itemStack = itemStack2.copy();
				} else {
					if (!(item instanceof DyeItem)) {
						return ItemStack.EMPTY;
					}

					list.add((DyeItem)item);
				}
			}
		}

		return !itemStack.isEmpty() && !list.isEmpty() ? DyeableLeatherItem.dyeArmor(itemStack, list) : ItemStack.EMPTY;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.ARMOR_DYE;
	}
}
