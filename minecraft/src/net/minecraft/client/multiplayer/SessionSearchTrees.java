package net.minecraft.client.multiplayer;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

@Environment(EnvType.CLIENT)
public class SessionSearchTrees {
	private static final SessionSearchTrees.Key RECIPE_COLLECTIONS = new SessionSearchTrees.Key();
	private static final SessionSearchTrees.Key CREATIVE_NAMES = new SessionSearchTrees.Key();
	private static final SessionSearchTrees.Key CREATIVE_TAGS = new SessionSearchTrees.Key();
	private CompletableFuture<SearchTree<ItemStack>> creativeByNameSearch = CompletableFuture.completedFuture(SearchTree.empty());
	private CompletableFuture<SearchTree<ItemStack>> creativeByTagSearch = CompletableFuture.completedFuture(SearchTree.empty());
	private CompletableFuture<SearchTree<RecipeCollection>> recipeSearch = CompletableFuture.completedFuture(SearchTree.empty());
	private final Map<SessionSearchTrees.Key, Runnable> reloaders = new IdentityHashMap();

	private void register(SessionSearchTrees.Key key, Runnable runnable) {
		runnable.run();
		this.reloaders.put(key, runnable);
	}

	public void rebuildAfterLanguageChange() {
		for (Runnable runnable : this.reloaders.values()) {
			runnable.run();
		}
	}

	private static Stream<String> getTooltipLines(Stream<ItemStack> stream, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag) {
		return stream.flatMap(itemStack -> itemStack.getTooltipLines(tooltipContext, null, tooltipFlag).stream())
			.map(component -> ChatFormatting.stripFormatting(component.getString()).trim())
			.filter(string -> !string.isEmpty());
	}

	public void updateRecipes(ClientRecipeBook clientRecipeBook, RegistryAccess.Frozen frozen) {
		this.register(
			RECIPE_COLLECTIONS,
			() -> {
				List<RecipeCollection> list = clientRecipeBook.getCollections();
				Registry<Item> registry = frozen.registryOrThrow(Registries.ITEM);
				Item.TooltipContext tooltipContext = Item.TooltipContext.of(frozen);
				TooltipFlag tooltipFlag = TooltipFlag.Default.NORMAL;
				CompletableFuture<?> completableFuture = this.recipeSearch;
				this.recipeSearch = CompletableFuture.supplyAsync(
					() -> new FullTextSearchTree(
							recipeCollection -> getTooltipLines(
									recipeCollection.getRecipes().stream().map(recipeHolder -> recipeHolder.value().getResultItem(frozen)), tooltipContext, tooltipFlag
								),
							recipeCollection -> recipeCollection.getRecipes().stream().map(recipeHolder -> registry.getKey(recipeHolder.value().getResultItem(frozen).getItem())),
							list
						),
					Util.backgroundExecutor()
				);
				completableFuture.cancel(true);
			}
		);
	}

	public SearchTree<RecipeCollection> recipes() {
		return (SearchTree<RecipeCollection>)this.recipeSearch.join();
	}

	public void updateCreativeTags(List<ItemStack> list) {
		this.register(
			CREATIVE_TAGS,
			() -> {
				CompletableFuture<?> completableFuture = this.creativeByTagSearch;
				this.creativeByTagSearch = CompletableFuture.supplyAsync(
					() -> new IdSearchTree(itemStack -> itemStack.getTags().map(TagKey::location), list), Util.backgroundExecutor()
				);
				completableFuture.cancel(true);
			}
		);
	}

	public SearchTree<ItemStack> creativeTagSearch() {
		return (SearchTree<ItemStack>)this.creativeByTagSearch.join();
	}

	public void updateCreativeTooltips(HolderLookup.Provider provider, List<ItemStack> list) {
		this.register(
			CREATIVE_NAMES,
			() -> {
				Item.TooltipContext tooltipContext = Item.TooltipContext.of(provider);
				TooltipFlag tooltipFlag = TooltipFlag.Default.NORMAL.asCreative();
				CompletableFuture<?> completableFuture = this.creativeByNameSearch;
				this.creativeByNameSearch = CompletableFuture.supplyAsync(
					() -> new FullTextSearchTree(
							itemStack -> getTooltipLines(Stream.of(itemStack), tooltipContext, tooltipFlag),
							itemStack -> itemStack.getItemHolder().unwrapKey().map(ResourceKey::location).stream(),
							list
						),
					Util.backgroundExecutor()
				);
				completableFuture.cancel(true);
			}
		);
	}

	public SearchTree<ItemStack> creativeNameSearch() {
		return (SearchTree<ItemStack>)this.creativeByNameSearch.join();
	}

	@Environment(EnvType.CLIENT)
	static class Key {
	}
}
