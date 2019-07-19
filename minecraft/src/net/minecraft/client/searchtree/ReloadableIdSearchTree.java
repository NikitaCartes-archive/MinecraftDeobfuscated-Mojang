package net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
public class ReloadableIdSearchTree<T> implements MutableSearchTree<T> {
	protected SuffixArray<T> namespaceTree = new SuffixArray<>();
	protected SuffixArray<T> pathTree = new SuffixArray<>();
	private final Function<T, Stream<ResourceLocation>> idGetter;
	private final List<T> contents = Lists.<T>newArrayList();
	private final Object2IntMap<T> orderT = new Object2IntOpenHashMap<>();

	public ReloadableIdSearchTree(Function<T, Stream<ResourceLocation>> function) {
		this.idGetter = function;
	}

	@Override
	public void refresh() {
		this.namespaceTree = new SuffixArray<>();
		this.pathTree = new SuffixArray<>();

		for (T object : this.contents) {
			this.index(object);
		}

		this.namespaceTree.generate();
		this.pathTree.generate();
	}

	@Override
	public void add(T object) {
		this.orderT.put(object, this.contents.size());
		this.contents.add(object);
		this.index(object);
	}

	@Override
	public void clear() {
		this.contents.clear();
		this.orderT.clear();
	}

	protected void index(T object) {
		((Stream)this.idGetter.apply(object)).forEach(resourceLocation -> {
			this.namespaceTree.add(object, resourceLocation.getNamespace().toLowerCase(Locale.ROOT));
			this.pathTree.add(object, resourceLocation.getPath().toLowerCase(Locale.ROOT));
		});
	}

	protected int comparePosition(T object, T object2) {
		return Integer.compare(this.orderT.getInt(object), this.orderT.getInt(object2));
	}

	@Override
	public List<T> search(String string) {
		int i = string.indexOf(58);
		if (i == -1) {
			return this.pathTree.search(string);
		} else {
			List<T> list = this.namespaceTree.search(string.substring(0, i).trim());
			String string2 = string.substring(i + 1).trim();
			List<T> list2 = this.pathTree.search(string2);
			return Lists.<T>newArrayList(new ReloadableIdSearchTree.IntersectionIterator<>(list.iterator(), list2.iterator(), this::comparePosition));
		}
	}

	@Environment(EnvType.CLIENT)
	public static class IntersectionIterator<T> extends AbstractIterator<T> {
		private final PeekingIterator<T> firstIterator;
		private final PeekingIterator<T> secondIterator;
		private final Comparator<T> orderT;

		public IntersectionIterator(Iterator<T> iterator, Iterator<T> iterator2, Comparator<T> comparator) {
			this.firstIterator = Iterators.peekingIterator(iterator);
			this.secondIterator = Iterators.peekingIterator(iterator2);
			this.orderT = comparator;
		}

		@Override
		protected T computeNext() {
			while (this.firstIterator.hasNext() && this.secondIterator.hasNext()) {
				int i = this.orderT.compare(this.firstIterator.peek(), this.secondIterator.peek());
				if (i == 0) {
					this.secondIterator.next();
					return this.firstIterator.next();
				}

				if (i < 0) {
					this.firstIterator.next();
				} else {
					this.secondIterator.next();
				}
			}

			return this.endOfData();
		}
	}
}
