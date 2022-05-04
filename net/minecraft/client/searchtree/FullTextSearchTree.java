/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.searchtree;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.IntersectionIterator;
import net.minecraft.client.searchtree.MergingUniqueIterator;
import net.minecraft.client.searchtree.PlainTextSearchTree;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public class FullTextSearchTree<T>
extends IdSearchTree<T> {
    private final List<T> contents;
    private final Function<T, Stream<String>> filler;
    private PlainTextSearchTree<T> plainTextSearchTree = PlainTextSearchTree.empty();

    public FullTextSearchTree(Function<T, Stream<String>> function, Function<T, Stream<ResourceLocation>> function2, List<T> list) {
        super(function2, list);
        this.contents = list;
        this.filler = function;
    }

    @Override
    public void refresh() {
        super.refresh();
        this.plainTextSearchTree = PlainTextSearchTree.create(this.contents, this.filler);
    }

    @Override
    protected List<T> searchPlainText(String string) {
        return this.plainTextSearchTree.search(string);
    }

    @Override
    protected List<T> searchResourceLocation(String string, String string2) {
        List list = this.resourceLocationSearchTree.searchNamespace(string);
        List list2 = this.resourceLocationSearchTree.searchPath(string2);
        List<T> list3 = this.plainTextSearchTree.search(string2);
        MergingUniqueIterator iterator = new MergingUniqueIterator(list2.iterator(), list3.iterator(), this.additionOrder);
        return ImmutableList.copyOf(new IntersectionIterator(list.iterator(), iterator, this.additionOrder));
    }
}

