package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

public class SuspiciousStewRecipe extends CustomRecipe {
	public SuspiciousStewRecipe(ResourceLocation resourceLocation, CraftingBookCategory craftingBookCategory) {
		super(resourceLocation, craftingBookCategory);
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		boolean bl = false;
		boolean bl2 = false;
		boolean bl3 = false;
		boolean bl4 = false;

		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack itemStack = craftingContainer.getItem(i);
			if (!itemStack.isEmpty()) {
				if (itemStack.is(Blocks.BROWN_MUSHROOM.asItem()) && !bl3) {
					bl3 = true;
				} else if (itemStack.is(Blocks.RED_MUSHROOM.asItem()) && !bl2) {
					bl2 = true;
				} else if (itemStack.is(ItemTags.SMALL_FLOWERS) && !bl) {
					bl = true;
				} else {
					if (!itemStack.is(Items.BOWL) || bl4) {
						return false;
					}

					bl4 = true;
				}
			}
		}

		return bl && bl3 && bl2 && bl4;
	}

	public ItemStack assembleRaw(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
		ItemStack itemStack = new ItemStack(Items.SUSPICIOUS_STEW, 1);

		for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
			ItemStack itemStack2 = craftingContainer.getItem(i);
			if (!itemStack2.isEmpty()) {
				SuspiciousEffectHolder suspiciousEffectHolder = SuspiciousEffectHolder.tryGet(itemStack2.getItem());
				if (suspiciousEffectHolder != null) {
					SuspiciousStewItem.saveMobEffect(itemStack, suspiciousEffectHolder.getSuspiciousEffect(), suspiciousEffectHolder.getEffectDuration());
					break;
				}
			}
		}

		return itemStack;
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i >= 2 && j >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SUSPICIOUS_STEW;
	}
}
