package net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.Comparator;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class IntersectionIterator<T> extends AbstractIterator<T> {
	private final PeekingIterator<T> firstIterator;
	private final PeekingIterator<T> secondIterator;
	private final Comparator<T> comparator;

	public IntersectionIterator(Iterator<T> iterator, Iterator<T> iterator2, Comparator<T> comparator) {
		this.firstIterator = Iterators.peekingIterator(iterator);
		this.secondIterator = Iterators.peekingIterator(iterator2);
		this.comparator = comparator;
	}

	@Override
	protected T computeNext() {
		while (this.firstIterator.hasNext() && this.secondIterator.hasNext()) {
			int i = this.comparator.compare(this.firstIterator.peek(), this.secondIterator.peek());
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
