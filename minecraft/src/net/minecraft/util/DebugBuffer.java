package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class DebugBuffer<T> {
	private final AtomicReferenceArray<T> data;
	private final AtomicInteger index;

	public DebugBuffer(int i) {
		this.data = new AtomicReferenceArray(i);
		this.index = new AtomicInteger(0);
	}

	public void push(T object) {
		int i = this.data.length();

		int j;
		int k;
		do {
			j = this.index.get();
			k = (j + 1) % i;
		} while (!this.index.compareAndSet(j, k));

		this.data.set(k, object);
	}

	public List<T> dump() {
		int i = this.index.get();
		Builder<T> builder = ImmutableList.builder();

		for (int j = 0; j < this.data.length(); j++) {
			int k = Math.floorMod(i - j, this.data.length());
			T object = (T)this.data.get(k);
			if (object != null) {
				builder.add(object);
			}
		}

		return builder.build();
	}
}
