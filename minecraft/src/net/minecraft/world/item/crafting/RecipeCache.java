package net.minecraft.world.item.crafting;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RecipeCache {
	private final RecipeCache.Entry[] entries;
	private WeakReference<RecipeManager> cachedRecipeManager = new WeakReference(null);

	public RecipeCache(int i) {
		this.entries = new RecipeCache.Entry[i];
	}

	public Optional<RecipeHolder<CraftingRecipe>> get(Level level, CraftingInput craftingInput) {
		if (craftingInput.isEmpty()) {
			return Optional.empty();
		} else {
			this.validateRecipeManager(level);

			for (int i = 0; i < this.entries.length; i++) {
				RecipeCache.Entry entry = this.entries[i];
				if (entry != null && entry.matches(craftingInput)) {
					this.moveEntryToFront(i);
					return Optional.ofNullable(entry.value());
				}
			}

			return this.compute(craftingInput, level);
		}
	}

	private void validateRecipeManager(Level level) {
		RecipeManager recipeManager = level.getRecipeManager();
		if (recipeManager != this.cachedRecipeManager.get()) {
			this.cachedRecipeManager = new WeakReference(recipeManager);
			Arrays.fill(this.entries, null);
		}
	}

	private Optional<RecipeHolder<CraftingRecipe>> compute(CraftingInput craftingInput, Level level) {
		Optional<RecipeHolder<CraftingRecipe>> optional = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInput, level);
		this.insert(craftingInput, (RecipeHolder<CraftingRecipe>)optional.orElse(null));
		return optional;
	}

	private void moveEntryToFront(int i) {
		if (i > 0) {
			RecipeCache.Entry entry = this.entries[i];
			System.arraycopy(this.entries, 0, this.entries, 1, i);
			this.entries[0] = entry;
		}
	}

	private void insert(CraftingInput craftingInput, @Nullable RecipeHolder<CraftingRecipe> recipeHolder) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);

		for (int i = 0; i < craftingInput.size(); i++) {
			nonNullList.set(i, craftingInput.getItem(i).copyWithCount(1));
		}

		System.arraycopy(this.entries, 0, this.entries, 1, this.entries.length - 1);
		this.entries[0] = new RecipeCache.Entry(nonNullList, craftingInput.width(), craftingInput.height(), recipeHolder);
	}

	static record Entry(NonNullList<ItemStack> key, int width, int height, @Nullable RecipeHolder<CraftingRecipe> value) {
		public boolean matches(CraftingInput craftingInput) {
			if (this.width == craftingInput.width() && this.height == craftingInput.height()) {
				for (int i = 0; i < this.key.size(); i++) {
					if (!ItemStack.isSameItemSameComponents(this.key.get(i), craftingInput.getItem(i))) {
						return false;
					}
				}

				return true;
			} else {
				return false;
			}
		}
	}
}
