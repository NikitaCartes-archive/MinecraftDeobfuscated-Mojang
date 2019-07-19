package net.minecraft.world.item.crafting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ShieldDecorationRecipe extends CustomRecipe {
	public ShieldDecorationRecipe(ResourceLocation resourceLocation) {
		super(resourceLocation);
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		ItemStack itemStack = ItemStack.EMPTY;
		ItemStack itemStack2 = ItemStack.EMPTY;

		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack itemStack3 = craftingContainer.getItem(i);
			if (!itemStack3.isEmpty()) {
				if (itemStack3.getItem() instanceof BannerItem) {
					if (!itemStack2.isEmpty()) {
						return false;
					}

					itemStack2 = itemStack3;
				} else {
					if (itemStack3.getItem() != Items.SHIELD) {
						return false;
					}

					if (!itemStack.isEmpty()) {
						return false;
					}

					if (itemStack3.getTagElement("BlockEntityTag") != null) {
						return false;
					}

					itemStack = itemStack3;
				}
			}
		}

		return !itemStack.isEmpty() && !itemStack2.isEmpty();
	}

	public ItemStack assemble(CraftingContainer craftingContainer) {
		ItemStack itemStack = ItemStack.EMPTY;
		ItemStack itemStack2 = ItemStack.EMPTY;

		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack itemStack3 = craftingContainer.getItem(i);
			if (!itemStack3.isEmpty()) {
				if (itemStack3.getItem() instanceof BannerItem) {
					itemStack = itemStack3;
				} else if (itemStack3.getItem() == Items.SHIELD) {
					itemStack2 = itemStack3.copy();
				}
			}
		}

		if (itemStack2.isEmpty()) {
			return itemStack2;
		} else {
			CompoundTag compoundTag = itemStack.getTagElement("BlockEntityTag");
			CompoundTag compoundTag2 = compoundTag == null ? new CompoundTag() : compoundTag.copy();
			compoundTag2.putInt("Base", ((BannerItem)itemStack.getItem()).getColor().getId());
			itemStack2.addTagElement("BlockEntityTag", compoundTag2);
			return itemStack2;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SHIELD_DECORATION;
	}
}
