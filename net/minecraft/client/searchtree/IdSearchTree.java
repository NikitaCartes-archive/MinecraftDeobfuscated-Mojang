/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.searchtree;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.searchtree.IntersectionIterator;
import net.minecraft.client.searchtree.RefreshableSearchTree;
import net.minecraft.client.searchtree.ResourceLocationSearchTree;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public class IdSearchTree<T>
implements RefreshableSearchTree<T> {
    protected final Comparator<T> additionOrder;
    protected final ResourceLocationSearchTree<T> resourceLocationSearchTree;

    public IdSearchTree(Function<T, Stream<ResourceLocation>> function, List<T> list) {
        ToIntFunction<T> toIntFunction = Util.createIndexLookup(list);
        this.additionOrder = Comparator.comparingInt(toIntFunction);
        this.resourceLocationSearchTree = ResourceLocationSearchTree.create(list, function);
    }

    @Override
    public List<T> search(String string) {
        int i = string.indexOf(58);
        if (i == -1) {
            return this.searchPlainText(string);
        }
        return this.searchResourceLocation(string.substring(0, i).trim(), string.substring(i + 1).trim());
    }

    protected List<T> searchPlainText(String string) {
        return this.resourceLocationSearchTree.searchPath(string);
    }

    protected List<T> searchResourceLocation(String string, String string2) {
        List<T> list = this.resourceLocationSearchTree.searchNamespace(string);
        List<T> list2 = this.resourceLocationSearchTree.searchPath(string2);
        return ImmutableList.copyOf(new IntersectionIterator<T>(list.iterator(), list2.iterator(), this.additionOrder));
    }
}

