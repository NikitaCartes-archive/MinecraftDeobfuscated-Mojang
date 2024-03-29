package net.minecraft.world.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;

public class FireworkStarFadeRecipe extends CustomRecipe {
	private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

	public FireworkStarFadeRecipe(CraftingBookCategory craftingBookCategory) {
		super(craftingBookCategory);
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
		boolean bl = false;
		boolean bl2 = false;

		for(int i = 0; i < craftingContainer.getContainerSize(); ++i) {
			ItemStack itemStack = craftingContainer.getItem(i);
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

	public ItemStack assemble(CraftingContainer craftingContainer, HolderLookup.Provider provider) {
		IntList intList = new IntArrayList();
		ItemStack itemStack = null;

		for(int i = 0; i < craftingContainer.getContainerSize(); ++i) {
			ItemStack itemStack2 = craftingContainer.getItem(i);
			Item item = itemStack2.getItem();
			if (item instanceof DyeItem) {
				intList.add(((DyeItem)item).getDyeColor().getFireworkColor());
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
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.FIREWORK_STAR_FADE;
	}
}
