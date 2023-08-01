package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

public class ArrayListDeque<T> extends AbstractList<T> implements Serializable, Cloneable, Deque<T>, RandomAccess {
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

	public void addFirst(T object) {
		this.add(0, object);
	}

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

	public T removeFirst() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		} else {
			return this.remove(0);
		}
	}

	public T removeLast() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		} else {
			return this.remove(this.size - 1);
		}
	}

	@Nullable
	public T pollFirst() {
		return this.size == 0 ? null : this.removeFirst();
	}

	@Nullable
	public T pollLast() {
		return this.size == 0 ? null : this.removeLast();
	}

	public T getFirst() {
		if (this.size == 0) {
			throw new NoSuchElementException();
		} else {
			return this.get(0);
		}
	}

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

	public boolean offer(T object) {
		return this.offerLast(object);
	}

	public T remove() {
		return this.removeFirst();
	}

	@Nullable
	public T poll() {
		return this.pollFirst();
	}

	public T element() {
		return this.getFirst();
	}

	@Nullable
	public T peek() {
		return this.peekFirst();
	}

	public void push(T object) {
		this.addFirst(object);
	}

	public T pop() {
		return this.removeFirst();
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
}
