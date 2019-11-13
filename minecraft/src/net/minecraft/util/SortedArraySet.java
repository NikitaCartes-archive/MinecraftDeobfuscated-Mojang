package net.minecraft.util;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SortedArraySet<T> extends AbstractSet<T> {
	private final Comparator<T> comparator;
	private T[] contents;
	private int size;

	private SortedArraySet(int i, Comparator<T> comparator) {
		this.comparator = comparator;
		if (i < 0) {
			throw new IllegalArgumentException("Initial capacity (" + i + ") is negative");
		} else {
			this.contents = (T[])castRawArray(new Object[i]);
		}
	}

	public static <T extends Comparable<T>> SortedArraySet<T> create(int i) {
		return new SortedArraySet<>(i, Comparator.naturalOrder());
	}

	private static <T> T[] castRawArray(Object[] objects) {
		return (T[])objects;
	}

	private int findIndex(T object) {
		return Arrays.binarySearch(this.contents, 0, this.size, object, this.comparator);
	}

	private static int getInsertionPosition(int i) {
		return -i - 1;
	}

	public boolean add(T object) {
		int i = this.findIndex(object);
		if (i >= 0) {
			return false;
		} else {
			int j = getInsertionPosition(i);
			this.addInternal(object, j);
			return true;
		}
	}

	private void grow(int i) {
		if (i > this.contents.length) {
			if (this.contents != ObjectArrays.DEFAULT_EMPTY_ARRAY) {
				i = (int)Math.max(Math.min((long)this.contents.length + (long)(this.contents.length >> 1), 2147483639L), (long)i);
			} else if (i < 10) {
				i = 10;
			}

			Object[] objects = new Object[i];
			System.arraycopy(this.contents, 0, objects, 0, this.size);
			this.contents = (T[])castRawArray(objects);
		}
	}

	private void addInternal(T object, int i) {
		this.grow(this.size + 1);
		if (i != this.size) {
			System.arraycopy(this.contents, i, this.contents, i + 1, this.size - i);
		}

		this.contents[i] = object;
		this.size++;
	}

	private void removeInternal(int i) {
		this.size--;
		if (i != this.size) {
			System.arraycopy(this.contents, i + 1, this.contents, i, this.size - i);
		}

		this.contents[this.size] = null;
	}

	private T getInternal(int i) {
		return this.contents[i];
	}

	public T addOrGet(T object) {
		int i = this.findIndex(object);
		if (i >= 0) {
			return this.getInternal(i);
		} else {
			this.addInternal(object, getInsertionPosition(i));
			return object;
		}
	}

	public boolean remove(Object object) {
		int i = this.findIndex((T)object);
		if (i >= 0) {
			this.removeInternal(i);
			return true;
		} else {
			return false;
		}
	}

	public T first() {
		return this.getInternal(0);
	}

	public boolean contains(Object object) {
		int i = this.findIndex((T)object);
		return i >= 0;
	}

	public Iterator<T> iterator() {
		return new SortedArraySet.ArrayIterator();
	}

	public int size() {
		return this.size;
	}

	public Object[] toArray() {
		return (Object[])this.contents.clone();
	}

	public <U> U[] toArray(U[] objects) {
		if (objects.length < this.size) {
			return (U[])Arrays.copyOf(this.contents, this.size, objects.getClass());
		} else {
			System.arraycopy(this.contents, 0, objects, 0, this.size);
			if (objects.length > this.size) {
				objects[this.size] = null;
			}

			return objects;
		}
	}

	public void clear() {
		Arrays.fill(this.contents, 0, this.size, null);
		this.size = 0;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof SortedArraySet) {
				SortedArraySet<?> sortedArraySet = (SortedArraySet<?>)object;
				if (this.comparator.equals(sortedArraySet.comparator)) {
					return this.size == sortedArraySet.size && Arrays.equals(this.contents, sortedArraySet.contents);
				}
			}

			return super.equals(object);
		}
	}

	class ArrayIterator implements Iterator<T> {
		private int index;
		private int last = -1;

		private ArrayIterator() {
		}

		public boolean hasNext() {
			return this.index < SortedArraySet.this.size;
		}

		public T next() {
			if (this.index >= SortedArraySet.this.size) {
				throw new NoSuchElementException();
			} else {
				this.last = this.index++;
				return SortedArraySet.this.contents[this.last];
			}
		}

		public void remove() {
			if (this.last == -1) {
				throw new IllegalStateException();
			} else {
				SortedArraySet.this.removeInternal(this.last);
				this.index--;
				this.last = -1;
			}
		}
	}
}
