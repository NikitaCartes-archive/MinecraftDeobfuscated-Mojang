package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;

public class DecoratedPotRecipe extends CustomRecipe {
	public DecoratedPotRecipe(CraftingBookCategory craftingBookCategory) {
		super(craftingBookCategory);
	}

	public boolean matches(CraftingInput craftingInput, Level level) {
		if (craftingInput.width() == 3 && craftingInput.height() == 3) {
			for (int i = 0; i < craftingInput.size(); i++) {
				ItemStack itemStack = craftingInput.getItem(i);
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
		} else {
			return false;
		}
	}

	public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
		PotDecorations potDecorations = new PotDecorations(
			craftingInput.getItem(1).getItem(), craftingInput.getItem(3).getItem(), craftingInput.getItem(5).getItem(), craftingInput.getItem(7).getItem()
		);
		return DecoratedPotBlockEntity.createDecoratedPotItem(potDecorations);
	}

	@Override
	public RecipeSerializer<DecoratedPotRecipe> getSerializer() {
		return RecipeSerializer.DECORATED_POT_RECIPE;
	}
}
