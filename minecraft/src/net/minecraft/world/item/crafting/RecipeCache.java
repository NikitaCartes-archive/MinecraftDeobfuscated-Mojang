package net.minecraft.world.item.crafting;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RecipeCache {
	private final RecipeCache.Entry[] entries;
	private WeakReference<RecipeManager> cachedRecipeManager = new WeakReference(null);

	public RecipeCache(int i) {
		this.entries = new RecipeCache.Entry[i];
	}

	public Optional<RecipeHolder<CraftingRecipe>> get(Level level, CraftingContainer craftingContainer) {
		if (craftingContainer.isEmpty()) {
			return Optional.empty();
		} else {
			this.validateRecipeManager(level);

			for (int i = 0; i < this.entries.length; i++) {
				RecipeCache.Entry entry = this.entries[i];
				if (entry != null && entry.matches(craftingContainer.getItems())) {
					this.moveEntryToFront(i);
					return Optional.ofNullable(entry.value());
				}
			}

			return this.compute(craftingContainer, level);
		}
	}

	private void validateRecipeManager(Level level) {
		RecipeManager recipeManager = level.getRecipeManager();
		if (recipeManager != this.cachedRecipeManager.get()) {
			this.cachedRecipeManager = new WeakReference(recipeManager);
			Arrays.fill(this.entries, null);
		}
	}

	private Optional<RecipeHolder<CraftingRecipe>> compute(CraftingContainer craftingContainer, Level level) {
		Optional<RecipeHolder<CraftingRecipe>> optional = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingContainer, level);
		this.insert(craftingContainer.getItems(), (RecipeHolder<CraftingRecipe>)optional.orElse(null));
		return optional;
	}

	private void moveEntryToFront(int i) {
		if (i > 0) {
			RecipeCache.Entry entry = this.entries[i];
			System.arraycopy(this.entries, 0, this.entries, 1, i);
			this.entries[0] = entry;
		}
	}

	private void insert(List<ItemStack> list, @Nullable RecipeHolder<CraftingRecipe> recipeHolder) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(list.size(), ItemStack.EMPTY);

		for (int i = 0; i < list.size(); i++) {
			nonNullList.set(i, ((ItemStack)list.get(i)).copyWithCount(1));
		}

		System.arraycopy(this.entries, 0, this.entries, 1, this.entries.length - 1);
		this.entries[0] = new RecipeCache.Entry(nonNullList, recipeHolder);
	}

	static record Entry(NonNullList<ItemStack> key, @Nullable RecipeHolder<CraftingRecipe> value) {
		public boolean matches(List<ItemStack> list) {
			if (this.key.size() != list.size()) {
				return false;
			} else {
				for (int i = 0; i < this.key.size(); i++) {
					if (!ItemStack.isSameItemSameComponents(this.key.get(i), (ItemStack)list.get(i))) {
						return false;
					}
				}

				return true;
			}
		}
	}
}
