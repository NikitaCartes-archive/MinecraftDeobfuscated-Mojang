package net.minecraft.client.gui.screens.recipebook;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplay;

@Environment(EnvType.CLIENT)
public class RecipeCollection {
	private final List<RecipeDisplayEntry> entries;
	private final boolean singleResultItem;
	private final Set<RecipeDisplayId> craftable = new HashSet();
	private final Set<RecipeDisplayId> selected = new HashSet();

	public RecipeCollection(List<RecipeDisplayEntry> list) {
		this.entries = list;
		if (list.size() <= 1) {
			this.singleResultItem = true;
		} else {
			this.singleResultItem = allRecipesHaveSameResult(this.entries);
		}
	}

	private static boolean allRecipesHaveSameResult(List<RecipeDisplayEntry> list) {
		int i = list.size();
		SlotDisplay slotDisplay = ((RecipeDisplayEntry)list.getFirst()).display().result();

		for (int j = 1; j < i; j++) {
			SlotDisplay slotDisplay2 = ((RecipeDisplayEntry)list.get(j)).display().result();
			if (!slotDisplay2.equals(slotDisplay)) {
				return false;
			}
		}

		return true;
	}

	public void selectRecipes(StackedItemContents stackedItemContents, Predicate<RecipeDisplay> predicate) {
		for (RecipeDisplayEntry recipeDisplayEntry : this.entries) {
			boolean bl = predicate.test(recipeDisplayEntry.display());
			if (bl) {
				this.selected.add(recipeDisplayEntry.id());
			} else {
				this.selected.remove(recipeDisplayEntry.id());
			}

			if (bl && recipeDisplayEntry.canCraft(stackedItemContents)) {
				this.craftable.add(recipeDisplayEntry.id());
			} else {
				this.craftable.remove(recipeDisplayEntry.id());
			}
		}
	}

	public boolean isCraftable(RecipeDisplayId recipeDisplayId) {
		return this.craftable.contains(recipeDisplayId);
	}

	public boolean hasCraftable() {
		return !this.craftable.isEmpty();
	}

	public boolean hasAnySelected() {
		return !this.selected.isEmpty();
	}

	public List<RecipeDisplayEntry> getRecipes() {
		return this.entries;
	}

	public List<RecipeDisplayEntry> getSelectedRecipes(RecipeCollection.CraftableStatus craftableStatus) {
		Predicate<RecipeDisplayId> predicate = switch (craftableStatus) {
			case ANY -> this.selected::contains;
			case CRAFTABLE -> this.craftable::contains;
			case NOT_CRAFTABLE -> recipeDisplayId -> this.selected.contains(recipeDisplayId) && !this.craftable.contains(recipeDisplayId);
		};
		List<RecipeDisplayEntry> list = new ArrayList();

		for (RecipeDisplayEntry recipeDisplayEntry : this.entries) {
			if (predicate.test(recipeDisplayEntry.id())) {
				list.add(recipeDisplayEntry);
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
