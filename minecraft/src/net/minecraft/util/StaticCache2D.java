package net.minecraft.util;

import java.util.Locale;
import java.util.function.Consumer;

public class StaticCache2D<T> {
	private final int minX;
	private final int minZ;
	private final int sizeX;
	private final int sizeZ;
	private final Object[] cache;

	public static <T> StaticCache2D<T> create(int i, int j, int k, StaticCache2D.Initializer<T> initializer) {
		int l = i - k;
		int m = j - k;
		int n = 2 * k + 1;
		return new StaticCache2D<>(l, m, n, n, initializer);
	}

	private StaticCache2D(int i, int j, int k, int l, StaticCache2D.Initializer<T> initializer) {
		this.minX = i;
		this.minZ = j;
		this.sizeX = k;
		this.sizeZ = l;
		this.cache = new Object[this.sizeX * this.sizeZ];

		for (int m = i; m < i + k; m++) {
			for (int n = j; n < j + l; n++) {
				this.cache[this.getIndex(m, n)] = initializer.get(m, n);
			}
		}
	}

	public void forEach(Consumer<T> consumer) {
		for (Object object : this.cache) {
			consumer.accept(object);
		}
	}

	public T get(int i, int j) {
		if (!this.contains(i, j)) {
			throw new IllegalArgumentException("Requested out of range value (" + i + "," + j + ") from " + this);
		} else {
			return (T)this.cache[this.getIndex(i, j)];
		}
	}

	public boolean contains(int i, int j) {
		int k = i - this.minX;
		int l = j - this.minZ;
		return k >= 0 && k < this.sizeX && l >= 0 && l < this.sizeZ;
	}

	public String toString() {
		return String.format(Locale.ROOT, "StaticCache2D[%d, %d, %d, %d]", this.minX, this.minZ, this.minX + this.sizeX, this.minZ + this.sizeZ);
	}

	private int getIndex(int i, int j) {
		int k = i - this.minX;
		int l = j - this.minZ;
		return k * this.sizeZ + l;
	}

	@FunctionalInterface
	public interface Initializer<T> {
		T get(int i, int j);
	}
}
