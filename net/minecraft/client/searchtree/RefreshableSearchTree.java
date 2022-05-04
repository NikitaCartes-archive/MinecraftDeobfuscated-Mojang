/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.searchtree;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.searchtree.SearchTree;

@Environment(value=EnvType.CLIENT)
public interface RefreshableSearchTree<T>
extends SearchTree<T> {
    public static <T> RefreshableSearchTree<T> empty() {
        return string -> List.of();
    }

    default public void refresh() {
    }
}

