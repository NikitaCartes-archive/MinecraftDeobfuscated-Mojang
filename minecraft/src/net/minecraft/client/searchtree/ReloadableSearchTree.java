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
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ReloadableSearchTree<T> extends ReloadableIdSearchTree<T> {
	protected SuffixArray<T> tree = new SuffixArray<>();
	private final Function<T, Stream<String>> filler;

	public ReloadableSearchTree(Function<T, Stream<String>> function, Function<T, Stream<ResourceLocation>> function2) {
		super(function2);
		this.filler = function;
	}

	@Override
	public void refresh() {
		this.tree = new SuffixArray<>();
		super.refresh();
		this.tree.generate();
	}

	@Override
	protected void index(T object) {
		super.index(object);
		((Stream)this.filler.apply(object)).forEach(string -> this.tree.add(object, string.toLowerCase(Locale.ROOT)));
	}

	@Override
	public List<T> search(String string) {
		int i = string.indexOf(58);
		if (i < 0) {
			return this.tree.search(string);
		} else {
			List<T> list = this.namespaceTree.search(string.substring(0, i).trim());
			String string2 = string.substring(i + 1).trim();
			List<T> list2 = this.pathTree.search(string2);
			List<T> list3 = this.tree.search(string2);
			return Lists.<T>newArrayList(
				new ReloadableIdSearchTree.IntersectionIterator<>(
					list.iterator(), new ReloadableSearchTree.MergingUniqueIterator<>(list2.iterator(), list3.iterator(), this::comparePosition), this::comparePosition
				)
			);
		}
	}

	@Environment(EnvType.CLIENT)
	static class MergingUniqueIterator<T> extends AbstractIterator<T> {
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
			boolean bl = !this.firstIterator.hasNext();
			boolean bl2 = !this.secondIterator.hasNext();
			if (bl && bl2) {
				return this.endOfData();
			} else if (bl) {
				return this.secondIterator.next();
			} else if (bl2) {
				return this.firstIterator.next();
			} else {
				int i = this.orderT.compare(this.firstIterator.peek(), this.secondIterator.peek());
				if (i == 0) {
					this.secondIterator.next();
				}

				return i <= 0 ? this.firstIterator.next() : this.secondIterator.next();
			}
		}
	}
}
