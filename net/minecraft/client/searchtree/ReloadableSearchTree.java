/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.searchtree.ReloadableIdSearchTree;
import net.minecraft.client.searchtree.SuffixArray;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public class ReloadableSearchTree<T>
extends ReloadableIdSearchTree<T> {
    protected SuffixArray<T> tree = new SuffixArray();
    private final Function<T, Stream<String>> filler;

    public ReloadableSearchTree(Function<T, Stream<String>> function, Function<T, Stream<ResourceLocation>> function2) {
        super(function2);
        this.filler = function;
    }

    @Override
    public void refresh() {
        this.tree = new SuffixArray();
        super.refresh();
        this.tree.generate();
    }

    @Override
    protected void index(T object) {
        super.index(object);
        this.filler.apply(object).forEach(string -> this.tree.add(object, string.toLowerCase(Locale.ROOT)));
    }

    @Override
    public List<T> search(String string) {
        int i = string.indexOf(58);
        if (i < 0) {
            return this.tree.search(string);
        }
        List list = this.namespaceTree.search(string.substring(0, i).trim());
        String string2 = string.substring(i + 1).trim();
        List list2 = this.pathTree.search(string2);
        List<T> list3 = this.tree.search(string2);
        return Lists.newArrayList(new ReloadableIdSearchTree.IntersectionIterator(list.iterator(), new MergingUniqueIterator(list2.iterator(), list3.iterator(), this::comparePosition), this::comparePosition));
    }

    @Environment(value=EnvType.CLIENT)
    static class MergingUniqueIterator<T>
    extends AbstractIterator<T> {
        private final PeekingIterator<T> firstIterator;
        private final PeekingIterator<T> secondIterator;
        private final Comparator<T> orderT;

        public MergingUniqueIterator(Iterator<T> iterator, Iterator<T> iterator2, Comparator<T> comparator) {
            this.firstIterator = Iterators.peekingIterator(iterator);
            this.secondIterator = Iterators.peekingIterator(iterator2);
            this.orderT = comparator;
        }

        @Override
        protected T computeNext() {
            boolean bl2;
            boolean bl = !this.firstIterator.hasNext();
            boolean bl3 = bl2 = !this.secondIterator.hasNext();
            if (bl && bl2) {
                return this.endOfData();
            }
            if (bl) {
                return this.secondIterator.next();
            }
            if (bl2) {
                return this.firstIterator.next();
            }
            int i = this.orderT.compare(this.firstIterator.peek(), this.secondIterator.peek());
            if (i == 0) {
                this.secondIterator.next();
            }
            return i <= 0 ? this.firstIterator.next() : this.secondIterator.next();
        }
    }
}

