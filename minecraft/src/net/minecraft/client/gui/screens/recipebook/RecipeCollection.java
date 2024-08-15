package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.RegistryAccess;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

@Environment(EnvType.CLIENT)
public class RecipeCollection {
	private final RegistryAccess registryAccess;
	private final List<RecipeHolder<?>> recipes;
	private final boolean singleResultItem;
	private final Set<RecipeHolder<?>> craftable = Sets.<RecipeHolder<?>>newHashSet();
	private final Set<RecipeHolder<?>> fitsDimensions = Sets.<RecipeHolder<?>>newHashSet();
	private final Set<RecipeHolder<?>> known = Sets.<RecipeHolder<?>>newHashSet();

	public RecipeCollection(RegistryAccess registryAccess, List<RecipeHolder<?>> list) {
		this.registryAccess = registryAccess;
		this.recipes = ImmutableList.copyOf(list);
		if (list.size() <= 1) {
			this.singleResultItem = true;
		} else {
			this.singleResultItem = allRecipesHaveSameResult(registryAccess, list);
		}
	}

	private static boolean allRecipesHaveSameResult(RegistryAccess registryAccess, List<RecipeHolder<?>> list) {
		int i = list.size();
		ItemStack itemStack = ((RecipeHolder)list.get(0)).value().getResultItem(registryAccess);

		for (int j = 1; j < i; j++) {
			ItemStack itemStack2 = ((RecipeHolder)list.get(j)).value().getResultItem(registryAccess);
			if (!ItemStack.isSameItemSameComponents(itemStack, itemStack2)) {
				return false;
			}
		}

		return true;
	}

	public RegistryAccess registryAccess() {
		return this.registryAccess;
	}

	public boolean hasKnownRecipes() {
		return !this.known.isEmpty();
	}

	public void updateKnownRecipes(RecipeBook recipeBook) {
		for (RecipeHolder<?> recipeHolder : this.recipes) {
			if (recipeBook.contains(recipeHolder)) {
				this.known.add(recipeHolder);
			}
		}
	}

	public void selectMatchingRecipes(StackedItemContents stackedItemContents, int i, int j, RecipeBook recipeBook) {
		for (RecipeHolder<?> recipeHolder : this.recipes) {
			boolean bl = recipeHolder.value().canCraftInDimensions(i, j) && recipeBook.contains(recipeHolder);
			if (bl) {
				this.fitsDimensions.add(recipeHolder);
			} else {
				this.fitsDimensions.remove(recipeHolder);
			}

			if (bl && stackedItemContents.canCraft(recipeHolder.value(), null)) {
				this.craftable.add(recipeHolder);
			} else {
				this.craftable.remove(recipeHolder);
			}
		}
	}

	public boolean isCraftable(RecipeHolder<?> recipeHolder) {
		return this.craftable.contains(recipeHolder);
	}

	public boolean hasCraftable() {
		return !this.craftable.isEmpty();
	}

	public boolean hasFitting() {
		return !this.fitsDimensions.isEmpty();
	}

	public List<RecipeHolder<?>> getRecipes() {
		return this.recipes;
	}

	public List<RecipeHolder<?>> getFittingRecipes(RecipeCollection.CraftableStatus craftableStatus) {
		Predicate<RecipeHolder<?>> predicate = switch (craftableStatus) {
			case ANY -> this.fitsDimensions::contains;
			case CRAFTABLE -> this.craftable::contains;
			case NOT_CRAFTABLE -> recipeHolderx -> this.fitsDimensions.contains(recipeHolderx) && !this.craftable.contains(recipeHolderx);
		};
		List<RecipeHolder<?>> list = new ArrayList();

		for (RecipeHolder<?> recipeHolder : this.recipes) {
			if (predicate.test(recipeHolder)) {
				list.add(recipeHolder);
			}
		}

		return list;
	}

	public boolean hasSingleResultItem() {
		return this.singleResultItem;
	}

	@Environment(EnvType.CLIENT)
	public static enum CraftableStatus {
		ANY,
		CRAFTABLE,
		NOT_CRAFTABLE;
	}
}
