/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.Nullable;

public class SortedArraySet<T>
extends AbstractSet<T> {
    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    private final Comparator<T> comparator;
    T[] contents;
    int size;

    private SortedArraySet(int i, Comparator<T> comparator) {
        this.comparator = comparator;
        if (i < 0) {
            throw new IllegalArgumentException("Initial capacity (" + i + ") is negative");
        }
        this.contents = SortedArraySet.castRawArray(new Object[i]);
    }

    public static <T extends Comparable<T>> SortedArraySet<T> create() {
        return SortedArraySet.create(10);
    }

    public static <T extends Comparable<T>> SortedArraySet<T> create(int i) {
        return new SortedArraySet(i, Comparator.naturalOrder());
    }

    public static <T> SortedArraySet<T> create(Comparator<T> comparator) {
        return SortedArraySet.create(comparator, 10);
    }

    public static <T> SortedArraySet<T> create(Comparator<T> comparator, int i) {
        return new SortedArraySet<T>(i, comparator);
    }

    private static <T> T[] castRawArray(Object[] objects) {
        return objects;
    }

    private int findIndex(T object) {
        return Arrays.binarySearch(this.contents, 0, this.size, object, this.comparator);
    }

    private static int getInsertionPosition(int i) {
        return -i - 1;
    }

    @Override
    public boolean add(T object) {
        int i = this.findIndex(object);
        if (i >= 0) {
            return false;
        }
        int j = SortedArraySet.getInsertionPosition(i);
        this.addInternal(object, j);
        return true;
    }

    private void grow(int i) {
        if (i <= this.contents.length) {
            return;
        }
        if (this.contents != ObjectArrays.DEFAULT_EMPTY_ARRAY) {
            i = (int)Math.max(Math.min((long)this.contents.length + (long)(this.contents.length >> 1), 0x7FFFFFF7L), (long)i);
        } else if (i < 10) {
            i = 10;
        }
        Object[] objects = new Object[i];
        System.arraycopy(this.contents, 0, objects, 0, this.size);
        this.contents = SortedArraySet.castRawArray(objects);
    }

    private void addInternal(T object, int i) {
        this.grow(this.size + 1);
        if (i != this.size) {
            System.arraycopy(this.contents, i, this.contents, i + 1, this.size - i);
        }
        this.contents[i] = object;
        ++this.size;
    }

    void removeInternal(int i) {
        --this.size;
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
        }
        this.addInternal(object, SortedArraySet.getInsertionPosition(i));
        return object;
    }

    @Override
    public boolean remove(Object object) {
        int i = this.findIndex(object);
        if (i >= 0) {
            this.removeInternal(i);
            return true;
        }
        return false;
    }

    @Nullable
    public T get(T object) {
        int i = this.findIndex(object);
        if (i >= 0) {
            return this.getInternal(i);
        }
        return null;
    }

    public T first() {
        return this.getInternal(0);
    }

    public T last() {
        return this.getInternal(this.size - 1);
    }

    @Override
    public boolean contains(Object object) {
        int i = this.findIndex(object);
        return i >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayIterator();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(this.contents, this.size, Object[].class);
    }

    @Override
    public <U> U[] toArray(U[] objects) {
        if (objects.length < this.size) {
            return Arrays.copyOf(this.contents, this.size, objects.getClass());
        }
        System.arraycopy(this.contents, 0, objects, 0, this.size);
        if (objects.length > this.size) {
            objects[this.size] = null;
        }
        return objects;
    }

    @Override
    public void clear() {
        Arrays.fill(this.contents, 0, this.size, null);
        this.size = 0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof SortedArraySet) {
            SortedArraySet sortedArraySet = (SortedArraySet)object;
            if (this.comparator.equals(sortedArraySet.comparator)) {
                return this.size == sortedArraySet.size && Arrays.equals(this.contents, sortedArraySet.contents);
            }
        }
        return super.equals(object);
    }

    class ArrayIterator
    implements Iterator<T> {
        private int index;
        private int last = -1;

        ArrayIterator() {
        }

        @Override
        public boolean hasNext() {
            return this.index < SortedArraySet.this.size;
        }

        @Override
        public T next() {
            if (this.index >= SortedArraySet.this.size) {
                throw new NoSuchElementException();
            }
            this.last = this.index++;
            return SortedArraySet.this.contents[this.last];
        }

        @Override
        public void remove() {
            if (this.last == -1) {
                throw new IllegalStateException();
            }
            SortedArraySet.this.removeInternal(this.last);
            --this.index;
            this.last = -1;
        }
    }
}

