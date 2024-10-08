package net.minecraft.world.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;

public class FireworkStarFadeRecipe extends CustomRecipe {
	private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

	public FireworkStarFadeRecipe(CraftingBookCategory craftingBookCategory) {
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
					if (itemStack.getItem() instanceof DyeItem) {
						bl = true;
					} else {
						if (!STAR_INGREDIENT.test(itemStack)) {
							return false;
						}

						if (bl2) {
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
		IntList intList = new IntArrayList();
		ItemStack itemStack = null;

		for (int i = 0; i < craftingInput.size(); i++) {
			ItemStack itemStack2 = craftingInput.getItem(i);
			if (itemStack2.getItem() instanceof DyeItem dyeItem) {
				intList.add(dyeItem.getDyeColor().getFireworkColor());
			} else if (STAR_INGREDIENT.test(itemStack2)) {
				itemStack = itemStack2.copyWithCount(1);
			}
		}

		if (itemStack != null && !intList.isEmpty()) {
			itemStack.update(DataComponents.FIREWORK_EXPLOSION, FireworkExplosion.DEFAULT, intList, FireworkExplosion::withFadeColors);
			return itemStack;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public RecipeSerializer<FireworkStarFadeRecipe> getSerializer() {
		return RecipeSerializer.FIREWORK_STAR_FADE;
	}
}
