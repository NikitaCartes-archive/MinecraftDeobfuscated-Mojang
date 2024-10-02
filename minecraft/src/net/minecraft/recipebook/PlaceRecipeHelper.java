package net.minecraft.recipebook;

import java.util.Iterator;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public interface PlaceRecipeHelper {
	static <T> void placeRecipe(int i, int j, Recipe<?> recipe, Iterable<T> iterable, PlaceRecipeHelper.Output<T> output) {
		if (recipe instanceof ShapedRecipe shapedRecipe) {
			placeRecipe(i, j, shapedRecipe.getWidth(), shapedRecipe.getHeight(), iterable, output);
		} else {
			placeRecipe(i, j, i, j, iterable, output);
		}
	}

	static <T> void placeRecipe(int i, int j, int k, int l, Iterable<T> iterable, PlaceRecipeHelper.Output<T> output) {
		Iterator<T> iterator = iterable.iterator();
		int m = 0;

		for (int n = 0; n < j; n++) {
			boolean bl = (float)l < (float)j / 2.0F;
			int o = Mth.floor((float)j / 2.0F - (float)l / 2.0F);
			if (bl && o > n) {
				m += i;
				n++;
			}

			for (int p = 0; p < i; p++) {
				if (!iterator.hasNext()) {
					return;
				}

				bl = (float)k < (float)i / 2.0F;
				o = Mth.floor((float)i / 2.0F - (float)k / 2.0F);
				int q = k;
				boolean bl2 = p < k;
				if (bl) {
					q = o + k;
					bl2 = o <= p && p < o + k;
				}

				if (bl2) {
					output.addItemToSlot((T)iterator.next(), m, p, n);
				} else if (q == p) {
					m += i - p;
					break;
				}

				m++;
			}
		}
	}

	@FunctionalInterface
	public interface Output<T> {
		void addItemToSlot(T object, int i, int j, int k);
	}
}
