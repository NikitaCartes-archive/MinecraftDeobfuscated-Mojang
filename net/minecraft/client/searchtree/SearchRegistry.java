/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.searchtree;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class SearchRegistry
implements ResourceManagerReloadListener {
    public static final Key<ItemStack> CREATIVE_NAMES = new Key();
    public static final Key<ItemStack> CREATIVE_TAGS = new Key();
    public static final Key<RecipeCollection> RECIPE_COLLECTIONS = new Key();
    private final Map<Key<?>, MutableSearchTree<?>> searchTrees = Maps.newHashMap();

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        for (MutableSearchTree<?> mutableSearchTree : this.searchTrees.values()) {
            mutableSearchTree.refresh();
        }
    }

    public <T> void register(Key<T> key, MutableSearchTree<T> mutableSearchTree) {
        this.searchTrees.put(key, mutableSearchTree);
    }

    public <T> MutableSearchTree<T> getTree(Key<T> key) {
        return this.searchTrees.get(key);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Key<T> {
    }
}

