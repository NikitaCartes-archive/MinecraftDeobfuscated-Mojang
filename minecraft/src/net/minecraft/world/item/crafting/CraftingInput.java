package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;

public class CraftingInput implements RecipeInput {
	public static final CraftingInput EMPTY = new CraftingInput(0, 0, List.of());
	private final int width;
	private final int height;
	private final List<ItemStack> items;
	private final StackedContents stackedContents = new StackedContents();
	private final int ingredientCount;

	private CraftingInput(int i, int j, List<ItemStack> list) {
		this.width = i;
		this.height = j;
		this.items = list;
		int k = 0;

		for (ItemStack itemStack : list) {
			if (!itemStack.isEmpty()) {
				k++;
				this.stackedContents.accountStack(itemStack, 1);
			}
		}

		this.ingredientCount = k;
	}

	public static CraftingInput of(int i, int j, List<ItemStack> list) {
		if (i != 0 && j != 0) {
			int k = i - 1;
			int l = 0;
			int m = j - 1;
			int n = 0;

			for (int o = 0; o < j; o++) {
				boolean bl = true;

				for (int p = 0; p < i; p++) {
					ItemStack itemStack = (ItemStack)list.get(p + o * i);
					if (!itemStack.isEmpty()) {
						k = Math.min(k, p);
						l = Math.max(l, p);
						bl = false;
					}
				}

				if (!bl) {
					m = Math.min(m, o);
					n = Math.max(n, o);
				}
			}

			int o = l - k + 1;
			int q = n - m + 1;
			if (o <= 0 || q <= 0) {
				return EMPTY;
			} else if (o == i && q == j) {
				return new CraftingInput(i, j, list);
			} else {
				List<ItemStack> list2 = new ArrayList(o * q);

				for (int r = 0; r < q; r++) {
					for (int s = 0; s < o; s++) {
						int t = s + k + (r + m) * i;
						list2.add((ItemStack)list.get(t));
					}
				}

				return new CraftingInput(o, q, list2);
			}
		} else {
			return EMPTY;
		}
	}

	@Override
	public ItemStack getItem(int i) {
		return (ItemStack)this.items.get(i);
	}

	public ItemStack getItem(int i, int j) {
		return (ItemStack)this.items.get(i + j * this.width);
	}

	@Override
	public int size() {
		return this.items.size();
	}

	@Override
	public boolean isEmpty() {
		return this.ingredientCount == 0;
	}

	public StackedContents stackedContents() {
		return this.stackedContents;
	}

	public List<ItemStack> items() {
		return this.items;
	}

	public int ingredientCount() {
		return this.ingredientCount;
	}

	public int width() {
		return this.width;
	}

	public int height() {
		return this.height;
	}

	public boolean equals(Object object) {
		if (object == this) {
			return true;
		} else {
			return !(object instanceof CraftingInput craftingInput)
				? false
				: this.width == craftingInput.width
					&& this.height == craftingInput.height
					&& this.ingredientCount == craftingInput.ingredientCount
					&& ItemStack.listMatches(this.items, craftingInput.items);
		}
	}

	public int hashCode() {
		int i = ItemStack.hashStackList(this.items);
		i = 31 * i + this.width;
		return 31 * i + this.height;
	}
}
