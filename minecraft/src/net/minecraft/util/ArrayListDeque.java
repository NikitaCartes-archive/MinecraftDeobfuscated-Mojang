package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

public class ArrayListDeque<T> extends AbstractList<T> implements ListAndDeque<T> {
	private static final int MIN_GROWTH = 1;
	private Object[] contents;
	private int head;
	private int size;

	public ArrayListDeque() {
		this(1);
	}

	public ArrayListDeque(int i) {
		this.contents = new Object[i];
		this.head = 0;
		this.size = 0;
	}

	public int size() {
		return this.size;
	}

	@VisibleForTesting
	public int capacity() {
		return this.contents.length;
	}

	private int getIndex(int i) {
		return (i + this.head) % this.contents.length;
	}

	public T get(int i) {
		this.verifyIndexInRange(i);
		return this.getInner(this.getIndex(i));
	}

	private static void verifyIndexInRange(int i, int j) {
		if (i < 0 || i >= j) {
			throw new IndexOutOfBoundsException(i);
		}
	}

	private void verifyIndexInRange(int i) {
		verifyIndexInRange(i, this.size);
	}

	private T getInner(int i) {
		return (T)this.contents[i];
	}

	public T set(int i, T object) {
		this.verifyIndexInRange(i);
		Objects.requireNonNull(object);
		int j = this.getIndex(i);
		T object2 = this.getInner(j);
		this.contents[j] = object;
		return object2;
	}

	public void add(int i, T object) {
		verifyIndexInRange(i, this.size + 1);
		Objects.requireNonNull(object);
		if (this.size == this.contents.length) {
			this.grow();
		}

		int j = this.getIndex(i);
		if (i == this.size) {
			this.contents[j] = object;
		} else if (i == 0) {
			this.head--;
			if (this.head < 0) {
				this.head = this.head + this.contents.length;
			}

			this.contents[this.getIndex(0)] = object;
		} else {
			for (int k = this.size - 1; k >= i; k--) {
				this.contents[this.getIndex(k + 1)] = this.contents[this.getIndex(k)];
			}

			this.contents[j] = object;
		}

		this.modCount++;
		this.size++;
	}

	private void grow() {
		int i = this.contents.length + Math.max(this.contents.length >> 1, 1);
		Object[] objects = new Object[i];
		this.copyCount(objects, this.size);
		this.head = 0;
		this.contents = objects;
	}

	public T remove(int i) {
		this.verifyIndexInRange(i);
		int j = this.getIndex(i);
		T object = this.getInner(j);
		if (i == 0) {
			this.contents[j] = null;
			this.head++;
		} else if (i == this.size - 1) {
			this.contents[j] = null;
		} else {
			for (int k = i + 1; k < this.size; k++) {
				this.contents[this.getIndex(k - 1)] = this.get(k);
			}

			this.contents[this.getIndex(this.size - 1)] = null;
		}

		this.modCount++;
		this.size--;
		return object;
	}

	public boolean removeIf(Predicate<? super T> predicate) {
		int i = 0;

		for (int j = 0; j < this.size; j++) {
			T object = this.get(j);
			if (predicate.test(object)) {
				i++;
			} else if (i != 0) {
				this.contents[this.getIndex(j - i)] = object;
				this.contents[this.getIndex(j)] = null;
			}
		}

		this.modCount += i;
		this.size -= i;
		return i != 0;
	}

	private void copyCount(Object[] objects, int i) {
		for (int j = 0; j < i; j++) {
			objects[j] = this.get(j);
		}
	}

	public void replaceAll(UnaryOperator<T> unaryOperator) {
		for (int i = 0; i < this.size; i++) {
			int j = this.getIndex(i);
			this.contents[j] = Objects.requireNonNull(unaryOperator.apply(this.getInner(i)));
		}
	}

	public void forEach(Consumer<? super T> consumer) {
		for (int i = 0; i < this.size; i++) {
			consumer.accept(this.get(i));
		}
	}

	@Override
	public void addFirst(T object) {
		this.add(0, object);
	}

	@Override
	public void addLast(T object) {
		this.add(this.size, object);
	}

	public boolean offerFirst(T object) {
		this.addFirst(object);
		return true;
	}

	public boolean offerLast(T object) {
		this.addLast(object);
		return true;
	}

	@Override
	public T removeFirst() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		} else {
			return this.remove(0);
		}
	}

	@Override
	public T removeLast() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		} else {
			return this.remove(this.size - 1);
		}
	}

	@Override
	public ListAndDeque<T> reversed() {
		return new ArrayListDeque.ReversedView(this);
	}

	@Nullable
	public T pollFirst() {
		return this.size == 0 ? null : this.removeFirst();
	}

	@Nullable
	public T pollLast() {
		return this.size == 0 ? null : this.removeLast();
	}

	@Override
	public T getFirst() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		} else {
			return this.get(0);
		}
	}

	@Override
	public T getLast() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		} else {
			return this.get(this.size - 1);
		}
	}

	@Nullable
	public T peekFirst() {
		return this.size == 0 ? null : this.getFirst();
	}

	@Nullable
	public T peekLast() {
		return this.size == 0 ? null : this.getLast();
	}

	public boolean removeFirstOccurrence(Object object) {
		for (int i = 0; i < this.size; i++) {
			T object2 = this.get(i);
			if (Objects.equals(object, object2)) {
				this.remove(i);
				return true;
			}
		}

		return false;
	}

	public boolean removeLastOccurrence(Object object) {
		for (int i = this.size - 1; i >= 0; i--) {
			T object2 = this.get(i);
			if (Objects.equals(object, object2)) {
				this.remove(i);
				return true;
			}
		}

		return false;
	}

	public Iterator<T> descendingIterator() {
		return new ArrayListDeque.DescendingIterator();
	}

	class DescendingIterator implements Iterator<T> {
		private int index = ArrayListDeque.this.size() - 1;

		public DescendingIterator() {
		}

		public boolean hasNext() {
			return this.index >= 0;
		}

		public T next() {
			return ArrayListDeque.this.get(this.index--);
		}

		public void remove() {
			ArrayListDeque.this.remove(this.index + 1);
		}
	}

	class ReversedView extends AbstractList<T> implements ListAndDeque<T> {
		private final ArrayListDeque<T> source;

		public ReversedView(ArrayListDeque<T> arrayListDeque2) {
			this.source = arrayListDeque2;
		}

		@Override
		public ListAndDeque<T> reversed() {
			return this.source;
		}

		@Override
		public T getFirst() {
			return this.source.getLast();
		}

		@Override
		public T getLast() {
			return this.source.getFirst();
		}

		@Override
		public void addFirst(T object) {
			this.source.addLast(object);
		}

		@Override
		public void addLast(T object) {
			this.source.addFirst(object);
		}

		public boolean offerFirst(T object) {
			return this.source.offerLast(object);
		}

		public boolean offerLast(T object) {
			return this.source.offerFirst(object);
		}

		public T pollFirst() {
			return this.source.pollLast();
		}

		public T pollLast() {
			return this.source.pollFirst();
		}

		public T peekFirst() {
			return this.source.peekLast();
		}

		public T peekLast() {
			return this.source.peekFirst();
		}

		@Override
		public T removeFirst() {
			return this.source.removeLast();
		}

		@Override
		public T removeLast() {
			return this.source.removeFirst();
		}

		public boolean removeFirstOccurrence(Object object) {
			return this.source.removeLastOccurrence(object);
		}

		public boolean removeLastOccurrence(Object object) {
			return this.source.removeFirstOccurrence(object);
		}

		public Iterator<T> descendingIterator() {
			return this.source.iterator();
		}

		public int size() {
			return this.source.size();
		}

		public boolean isEmpty() {
			return this.source.isEmpty();
		}

		public boolean contains(Object object) {
			return this.source.contains(object);
		}

		public T get(int i) {
			return this.source.get(this.reverseIndex(i));
		}

		public T set(int i, T object) {
			return this.source.set(this.reverseIndex(i), object);
		}

		public void add(int i, T object) {
			this.source.add(this.reverseIndex(i) + 1, object);
		}

		public T remove(int i) {
			return this.source.remove(this.reverseIndex(i));
		}

		public int indexOf(Object object) {
			return this.reverseIndex(this.source.lastIndexOf(object));
		}

		public int lastIndexOf(Object object) {
			return this.reverseIndex(this.source.indexOf(object));
		}

		public List<T> subList(int i, int j) {
			return this.source.subList(this.reverseIndex(j) + 1, this.reverseIndex(i) + 1).reversed();
		}

		public Iterator<T> iterator() {
			return this.source.descendingIterator();
		}

		public void clear() {
			this.source.clear();
		}

		private int reverseIndex(int i) {
			return i == -1 ? -1 : this.source.size() - 1 - i;
		}
	}
}
