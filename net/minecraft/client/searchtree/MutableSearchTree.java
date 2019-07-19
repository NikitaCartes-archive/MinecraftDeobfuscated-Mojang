/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.searchtree;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.searchtree.SearchTree;

@Environment(value=EnvType.CLIENT)
public interface MutableSearchTree<T>
extends SearchTree<T> {
    public void add(T var1);

    public void clear();

    public void refresh();
}

