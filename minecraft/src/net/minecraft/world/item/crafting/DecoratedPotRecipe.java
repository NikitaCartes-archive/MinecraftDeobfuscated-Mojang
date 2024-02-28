package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;

public class DecoratedPotRecipe extends CustomRecipe {
	public DecoratedPotRecipe(CraftingBookCategory craftingBookCategory) {
		super(craftingBookCategory);
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		if (!this.canCraftInDimensions(craftingContainer.getWidth(), craftingContainer.getHeight())) {
			return false;
		} else {
			for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
				ItemStack itemStack = craftingContainer.getItem(i);
				switch (i) {
					case 1:
					case 3:
					case 5:
					case 7:
						if (!itemStack.is(ItemTags.DECORATED_POT_INGREDIENTS)) {
							return false;
						}
						break;
					case 2:
					case 4:
					case 6:
					default:
						if (!itemStack.is(Items.AIR)) {
							return false;
						}
				}
			}

			return true;
		}
	}

	public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
		PotDecorations potDecorations = new PotDecorations(
			craftingContainer.getItem(1).getItem(),
			craftingContainer.getItem(3).getItem(),
			craftingContainer.getItem(5).getItem(),
			craftingContainer.getItem(7).getItem()
		);
		return DecoratedPotBlockEntity.createDecoratedPotItem(potDecorations);
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i == 3 && j == 3;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.DECORATED_POT_RECIPE;
	}
}
