package net.minecraft.recipebook;

import java.util.Iterator;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public interface PlaceRecipe<T> {
	default void placeRecipe(int i, int j, int k, Recipe<?> recipe, Iterator<T> iterator, int l) {
		int m = i;
		int n = j;
		if (recipe instanceof ShapedRecipe shapedRecipe) {
			m = shapedRecipe.getWidth();
			n = shapedRecipe.getHeight();
		}

		int o = 0;

		for (int p = 0; p < j; p++) {
			if (o == k) {
				o++;
			}

			boolean bl = (float)n < (float)j / 2.0F;
			int q = Mth.floor((float)j / 2.0F - (float)n / 2.0F);
			if (bl && q > p) {
				o += i;
				p++;
			}

			for (int r = 0; r < i; r++) {
				if (!iterator.hasNext()) {
					return;
				}

				bl = (float)m < (float)i / 2.0F;
				q = Mth.floor((float)i / 2.0F - (float)m / 2.0F);
				int s = m;
				boolean bl2 = r < m;
				if (bl) {
					s = q + m;
					bl2 = q <= r && r < q + m;
				}

				if (bl2) {
					this.addItemToSlot(iterator, o, l, p, r);
				} else if (s == r) {
					o += i - r;
					break;
				}

				o++;
			}
		}
	}

	void addItemToSlot(Iterator<T> iterator, int i, int j, int k, int l);
}
