package net.minecraft.client.searchtree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class SearchRegistry implements ResourceManagerReloadListener {
	public static final SearchRegistry.Key<ItemStack> CREATIVE_NAMES = new SearchRegistry.Key<>();
	public static final SearchRegistry.Key<ItemStack> CREATIVE_TAGS = new SearchRegistry.Key<>();
	private final Map<SearchRegistry.Key<?>, SearchRegistry.TreeEntry<?>> searchTrees = new HashMap();

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		for (SearchRegistry.TreeEntry<?> treeEntry : this.searchTrees.values()) {
			treeEntry.refresh();
		}
	}

	public <T> void register(SearchRegistry.Key<T> key, SearchRegistry.TreeBuilderSupplier<T> treeBuilderSupplier) {
		this.searchTrees.put(key, new SearchRegistry.TreeEntry<>(treeBuilderSupplier));
	}

	private <T> SearchRegistry.TreeEntry<T> getSupplier(SearchRegistry.Key<T> key) {
		SearchRegistry.TreeEntry<T> treeEntry = (SearchRegistry.TreeEntry<T>)this.searchTrees.get(key);
		if (treeEntry == null) {
			throw new IllegalStateException("Tree builder not registered");
		} else {
			return treeEntry;
		}
	}

	public <T> void populate(SearchRegistry.Key<T> key, List<T> list) {
		this.getSupplier(key).populate(list);
	}

	public <T> SearchTree<T> getTree(SearchRegistry.Key<T> key) {
		return this.getSupplier(key).tree;
	}

	@Environment(EnvType.CLIENT)
	public static class Key<T> {
	}

	@Environment(EnvType.CLIENT)
	public interface TreeBuilderSupplier<T> extends Function<List<T>, RefreshableSearchTree<T>> {
	}

	@Environment(EnvType.CLIENT)
	static class TreeEntry<T> {
		private final SearchRegistry.TreeBuilderSupplier<T> factory;
		RefreshableSearchTree<T> tree = RefreshableSearchTree.empty();

		TreeEntry(SearchRegistry.TreeBuilderSupplier<T> treeBuilderSupplier) {
			this.factory = treeBuilderSupplier;
		}

		void populate(List<T> list) {
			this.tree = (RefreshableSearchTree<T>)this.factory.apply(list);
			this.tree.refresh();
		}

		void refresh() {
			this.tree.refresh();
		}
	}
}
