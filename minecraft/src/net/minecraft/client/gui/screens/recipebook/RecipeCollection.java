package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

@Environment(EnvType.CLIENT)
public class RecipeCollection {
	private final List<Recipe<?>> recipes;
	private final boolean singleResultItem;
	private final Set<Recipe<?>> craftable = Sets.<Recipe<?>>newHashSet();
	private final Set<Recipe<?>> fitsDimensions = Sets.<Recipe<?>>newHashSet();
	private final Set<Recipe<?>> known = Sets.<Recipe<?>>newHashSet();

	public RecipeCollection(List<Recipe<?>> list) {
		this.recipes = ImmutableList.copyOf(list);
		if (list.size() <= 1) {
			this.singleResultItem = true;
		} else {
			this.singleResultItem = allRecipesHaveSameResult(list);
		}
	}

	private static boolean allRecipesHaveSameResult(List<Recipe<?>> list) {
		int i = list.size();
		ItemStack itemStack = ((Recipe)list.get(0)).getResultItem();

		for (int j = 1; j < i; j++) {
			ItemStack itemStack2 = ((Recipe)list.get(j)).getResultItem();
			if (!ItemStack.isSame(itemStack, itemStack2) || !ItemStack.tagMatches(itemStack, itemStack2)) {
				return false;
			}
		}

		return true;
	}

	public boolean hasKnownRecipes() {
		return !this.known.isEmpty();
	}

	public void updateKnownRecipes(RecipeBook recipeBook) {
		for (Recipe<?> recipe : this.recipes) {
			if (recipeBook.contains(recipe)) {
				this.known.add(recipe);
			}
		}
	}

	public void canCraft(StackedContents stackedContents, int i, int j, RecipeBook recipeBook) {
		for (Recipe<?> recipe : this.recipes) {
			boolean bl = recipe.canCraftInDimensions(i, j) && recipeBook.contains(recipe);
			if (bl) {
				this.fitsDimensions.add(recipe);
			} else {
				this.fitsDimensions.remove(recipe);
			}

			if (bl && stackedContents.canCraft(recipe, null)) {
				this.craftable.add(recipe);
			} else {
				this.craftable.remove(recipe);
			}
		}
	}

	public boolean isCraftable(Recipe<?> recipe) {
		return this.craftable.contains(recipe);
	}

	public boolean hasCraftable() {
		return !this.craftable.isEmpty();
	}

	public boolean hasFitting() {
		return !this.fitsDimensions.isEmpty();
	}

	public List<Recipe<?>> getRecipes() {
		return this.recipes;
	}

	public List<Recipe<?>> getRecipes(boolean bl) {
		List<Recipe<?>> list = Lists.<Recipe<?>>newArrayList();
		Set<Recipe<?>> set = bl ? this.craftable : this.fitsDimensions;

		for (Recipe<?> recipe : this.recipes) {
			if (set.contains(recipe)) {
				list.add(recipe);
			}
		}

		return list;
	}

	public List<Recipe<?>> getDisplayRecipes(boolean bl) {
		List<Recipe<?>> list = Lists.<Recipe<?>>newArrayList();

		for (Recipe<?> recipe : this.recipes) {
			if (this.fitsDimensions.contains(recipe) && this.craftable.contains(recipe) == bl) {
				list.add(recipe);
			}
		}

		return list;
	}

	public boolean hasSingleResultItem() {
		return this.singleResultItem;
	}
}
