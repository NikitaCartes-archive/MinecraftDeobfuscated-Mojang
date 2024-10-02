package net.minecraft.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.google.common.collect.ImmutableList.Builder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.BasicRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

@Environment(EnvType.CLIENT)
public class ClientRecipeBook extends RecipeBook {
	private final Map<RecipeDisplayId, RecipeDisplayEntry> known = new HashMap();
	private final Set<RecipeDisplayId> highlight = new HashSet();
	private Map<RecipeBookCategory, List<RecipeCollection>> collectionsByTab = Map.of();
	private List<RecipeCollection> allCollections = List.of();

	public void add(RecipeDisplayEntry recipeDisplayEntry) {
		this.known.put(recipeDisplayEntry.id(), recipeDisplayEntry);
	}

	public void remove(RecipeDisplayId recipeDisplayId) {
		this.known.remove(recipeDisplayId);
		this.highlight.remove(recipeDisplayId);
	}

	public boolean willHighlight(RecipeDisplayId recipeDisplayId) {
		return this.highlight.contains(recipeDisplayId);
	}

	public void removeHighlight(RecipeDisplayId recipeDisplayId) {
		this.highlight.remove(recipeDisplayId);
	}

	public void addHighlight(RecipeDisplayId recipeDisplayId) {
		this.highlight.add(recipeDisplayId);
	}

	public void rebuildCollections() {
		Map<BasicRecipeBookCategory, List<List<RecipeDisplayEntry>>> map = categorizeAndGroupRecipes(this.known.values());
		Map<RecipeBookCategory, List<RecipeCollection>> map2 = new HashMap();
		Builder<RecipeCollection> builder = ImmutableList.builder();
		map.forEach(
			(basicRecipeBookCategory, list) -> map2.put(
					basicRecipeBookCategory, (List)list.stream().map(RecipeCollection::new).peek(builder::add).collect(ImmutableList.toImmutableList())
				)
		);

		for (SearchRecipeBookCategory searchRecipeBookCategory : SearchRecipeBookCategory.values()) {
			map2.put(
				searchRecipeBookCategory,
				(List)searchRecipeBookCategory.includedCategories()
					.stream()
					.flatMap(basicRecipeBookCategory -> ((List)map2.getOrDefault(basicRecipeBookCategory, List.of())).stream())
					.collect(ImmutableList.toImmutableList())
			);
		}

		this.collectionsByTab = Map.copyOf(map2);
		this.allCollections = builder.build();
	}

	private static Map<BasicRecipeBookCategory, List<List<RecipeDisplayEntry>>> categorizeAndGroupRecipes(Iterable<RecipeDisplayEntry> iterable) {
		Map<BasicRecipeBookCategory, List<List<RecipeDisplayEntry>>> map = new HashMap();
		Table<BasicRecipeBookCategory, Integer, List<RecipeDisplayEntry>> table = HashBasedTable.create();

		for (RecipeDisplayEntry recipeDisplayEntry : iterable) {
			BasicRecipeBookCategory basicRecipeBookCategory = recipeDisplayEntry.category();
			OptionalInt optionalInt = recipeDisplayEntry.group();
			if (optionalInt.isEmpty()) {
				((List)map.computeIfAbsent(basicRecipeBookCategory, basicRecipeBookCategoryx -> new ArrayList())).add(List.of(recipeDisplayEntry));
			} else {
				List<RecipeDisplayEntry> list = table.get(basicRecipeBookCategory, optionalInt.getAsInt());
				if (list == null) {
					list = new ArrayList();
					table.put(basicRecipeBookCategory, optionalInt.getAsInt(), list);
					((List)map.computeIfAbsent(basicRecipeBookCategory, basicRecipeBookCategoryx -> new ArrayList())).add(list);
				}

				list.add(recipeDisplayEntry);
			}
		}

		return map;
	}

	public List<RecipeCollection> getCollections() {
		return this.allCollections;
	}

	public List<RecipeCollection> getCollection(RecipeBookCategory recipeBookCategory) {
		return (List<RecipeCollection>)this.collectionsByTab.getOrDefault(recipeBookCategory, Collections.emptyList());
	}
}
