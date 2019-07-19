package net.minecraft.client.searchtree;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class SearchRegistry implements ResourceManagerReloadListener {
	public static final SearchRegistry.Key<ItemStack> CREATIVE_NAMES = new SearchRegistry.Key<>();
	public static final SearchRegistry.Key<ItemStack> CREATIVE_TAGS = new SearchRegistry.Key<>();
	public static final SearchRegistry.Key<RecipeCollection> RECIPE_COLLECTIONS = new SearchRegistry.Key<>();
	private final Map<SearchRegistry.Key<?>, MutableSearchTree<?>> searchTrees = Maps.<SearchRegistry.Key<?>, MutableSearchTree<?>>newHashMap();

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		for (MutableSearchTree<?> mutableSearchTree : this.searchTrees.values()) {
			mutableSearchTree.refresh();
		}
	}

	public <T> void register(SearchRegistry.Key<T> key, MutableSearchTree<T> mutableSearchTree) {
		this.searchTrees.put(key, mutableSearchTree);
	}

	public <T> MutableSearchTree<T> getTree(SearchRegistry.Key<T> key) {
		return (MutableSearchTree<T>)this.searchTrees.get(key);
	}

	@Environment(EnvType.CLIENT)
	public static class Key<T> {
	}
}
