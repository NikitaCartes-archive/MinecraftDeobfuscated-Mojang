/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.searchtree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.searchtree.RefreshableSearchTree;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class SearchRegistry
implements ResourceManagerReloadListener {
    public static final Key<ItemStack> CREATIVE_NAMES = new Key();
    public static final Key<ItemStack> CREATIVE_TAGS = new Key();
    public static final Key<RecipeCollection> RECIPE_COLLECTIONS = new Key();
    private final Map<Key<?>, TreeEntry<?>> searchTrees = new HashMap();

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        for (TreeEntry<?> treeEntry : this.searchTrees.values()) {
            treeEntry.refresh();
        }
    }

    public <T> void register(Key<T> key, TreeBuilderSupplier<T> treeBuilderSupplier) {
        this.searchTrees.put(key, new TreeEntry<T>(treeBuilderSupplier));
    }

    private <T> TreeEntry<T> getSupplier(Key<T> key) {
        TreeEntry<?> treeEntry = this.searchTrees.get(key);
        if (treeEntry == null) {
            throw new IllegalStateException("Tree builder not registered");
        }
        return treeEntry;
    }

    public <T> void populate(Key<T> key, List<T> list) {
        this.getSupplier(key).populate(list);
    }

    public <T> SearchTree<T> getTree(Key<T> key) {
        return this.getSupplier(key).tree;
    }

    @Environment(value=EnvType.CLIENT)
    static class TreeEntry<T> {
        private final TreeBuilderSupplier<T> factory;
        RefreshableSearchTree<T> tree = RefreshableSearchTree.empty();

        TreeEntry(TreeBuilderSupplier<T> treeBuilderSupplier) {
            this.factory = treeBuilderSupplier;
        }

        void populate(List<T> list) {
            this.tree = (RefreshableSearchTree)this.factory.apply(list);
            this.tree.refresh();
        }

        void refresh() {
            this.tree.refresh();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface TreeBuilderSupplier<T>
    extends Function<List<T>, RefreshableSearchTree<T>> {
    }

    @Environment(value=EnvType.CLIENT)
    public static class Key<T> {
    }
}

