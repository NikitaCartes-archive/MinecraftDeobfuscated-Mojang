package net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.Comparator;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MergingUniqueIterator<T> extends AbstractIterator<T> {
	private final PeekingIterator<T> firstIterator;
	private final PeekingIterator<T> secondIterator;
	private final Comparator<T> comparator;

	public MergingUniqueIterator(Iterator<T> iterator, Iterator<T> iterator2, Comparator<T> comparator) {
		this.firstIterator = Iterators.peekingIterator(iterator);
		this.secondIterator = Iterators.peekingIterator(iterator2);
		this.comparator = comparator;
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
			int i = this.comparator.compare(this.firstIterator.peek(), this.secondIterator.peek());
			if (i == 0) {
				this.secondIterator.next();
			}

			return i <= 0 ? this.firstIterator.next() : this.secondIterator.next();
		}
	}
}
