package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RepairItemRecipe extends CustomRecipe {
	public RepairItemRecipe(ResourceLocation resourceLocation) {
		super(resourceLocation);
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		List<ItemStack> list = Lists.<ItemStack>newArrayList();

		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack itemStack = craftingContainer.getItem(i);
			if (!itemStack.isEmpty()) {
				list.add(itemStack);
				if (list.size() > 1) {
					ItemStack itemStack2 = (ItemStack)list.get(0);
					if (itemStack.getItem() != itemStack2.getItem() || itemStack2.getCount() != 1 || itemStack.getCount() != 1 || !itemStack2.getItem().canBeDepleted()) {
						return false;
					}
				}
			}
		}

		return list.size() == 2;
	}

	public ItemStack assemble(CraftingContainer craftingContainer) {
		List<ItemStack> list = Lists.<ItemStack>newArrayList();

		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack itemStack = craftingContainer.getItem(i);
			if (!itemStack.isEmpty()) {
				list.add(itemStack);
				if (list.size() > 1) {
					ItemStack itemStack2 = (ItemStack)list.get(0);
					if (itemStack.getItem() != itemStack2.getItem() || itemStack2.getCount() != 1 || itemStack.getCount() != 1 || !itemStack2.getItem().canBeDepleted()) {
						return ItemStack.EMPTY;
					}
				}
			}
		}

		if (list.size() == 2) {
			ItemStack itemStack3 = (ItemStack)list.get(0);
			ItemStack itemStack = (ItemStack)list.get(1);
			if (itemStack3.getItem() == itemStack.getItem() && itemStack3.getCount() == 1 && itemStack.getCount() == 1 && itemStack3.getItem().canBeDepleted()) {
				Item item = itemStack3.getItem();
				int j = item.getMaxDamage() - itemStack3.getDamageValue();
				int k = item.getMaxDamage() - itemStack.getDamageValue();
				int l = j + k + item.getMaxDamage() * 5 / 100;
				int m = item.getMaxDamage() - l;
				if (m < 0) {
					m = 0;
				}

				ItemStack itemStack4 = new ItemStack(itemStack3.getItem());
				itemStack4.setDamageValue(m);
				return itemStack4;
			}
		}

		return ItemStack.EMPTY;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.REPAIR_ITEM;
	}
}
