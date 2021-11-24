package net.minecraft.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;

public class CrudeIncrementalIntIdentityHashBiMap<K> implements IdMap<K> {
	private static final int NOT_FOUND = -1;
	private static final Object EMPTY_SLOT = null;
	private static final float LOADFACTOR = 0.8F;
	private K[] keys;
	private int[] values;
	private K[] byId;
	private int nextId;
	private int size;

	private CrudeIncrementalIntIdentityHashBiMap(int i) {
		this.keys = (K[])(new Object[i]);
		this.values = new int[i];
		this.byId = (K[])(new Object[i]);
	}

	private CrudeIncrementalIntIdentityHashBiMap(K[] objects, int[] is, K[] objects2, int i, int j) {
		this.keys = objects;
		this.values = is;
		this.byId = objects2;
		this.nextId = i;
		this.size = j;
	}

	public static <A> CrudeIncrementalIntIdentityHashBiMap<A> create(int i) {
		return new CrudeIncrementalIntIdentityHashBiMap((int)((float)i / 0.8F));
	}

	@Override
	public int getId(@Nullable K object) {
		return this.getValue(this.indexOf(object, this.hash(object)));
	}

	@Nullable
	@Override
	public K byId(int i) {
		return i >= 0 && i < this.byId.length ? this.byId[i] : null;
	}

	private int getValue(int i) {
		return i == -1 ? -1 : this.values[i];
	}

	public boolean contains(K object) {
		return this.getId(object) != -1;
	}

	public boolean contains(int i) {
		return this.byId(i) != null;
	}

	public int add(K object) {
		int i = this.nextId();
		this.addMapping(object, i);
		return i;
	}

	private int nextId() {
		while (this.nextId < this.byId.length && this.byId[this.nextId] != null) {
			this.nextId++;
		}

		return this.nextId;
	}

	private void grow(int i) {
		K[] objects = this.keys;
		int[] is = this.values;
		CrudeIncrementalIntIdentityHashBiMap<K> crudeIncrementalIntIdentityHashBiMap = new CrudeIncrementalIntIdentityHashBiMap<>(i);

		for (int j = 0; j < objects.length; j++) {
			if (objects[j] != null) {
				crudeIncrementalIntIdentityHashBiMap.addMapping(objects[j], is[j]);
			}
		}

		this.keys = crudeIncrementalIntIdentityHashBiMap.keys;
		this.values = crudeIncrementalIntIdentityHashBiMap.values;
		this.byId = crudeIncrementalIntIdentityHashBiMap.byId;
		this.nextId = crudeIncrementalIntIdentityHashBiMap.nextId;
		this.size = crudeIncrementalIntIdentityHashBiMap.size;
	}

	public void addMapping(K object, int i) {
		int j = Math.max(i, this.size + 1);
		if ((float)j >= (float)this.keys.length * 0.8F) {
			int k = this.keys.length << 1;

			while (k < i) {
				k <<= 1;
			}

			this.grow(k);
		}

		int k = this.findEmpty(this.hash(object));
		this.keys[k] = object;
		this.values[k] = i;
		this.byId[i] = object;
		this.size++;
		if (i == this.nextId) {
			this.nextId++;
		}
	}

	private int hash(@Nullable K object) {
		return (Mth.murmurHash3Mixer(System.identityHashCode(object)) & 2147483647) % this.keys.length;
	}

	private int indexOf(@Nullable K object, int i) {
		for (int j = i; j < this.keys.length; j++) {
			if (this.keys[j] == object) {
				return j;
			}

			if (this.keys[j] == EMPTY_SLOT) {
				return -1;
			}
		}

		for (int j = 0; j < i; j++) {
			if (this.keys[j] == object) {
				return j;
			}

			if (this.keys[j] == EMPTY_SLOT) {
				return -1;
			}
		}

		return -1;
	}

	private int findEmpty(int i) {
		for (int j = i; j < this.keys.length; j++) {
			if (this.keys[j] == EMPTY_SLOT) {
				return j;
			}
		}

		for (int jx = 0; jx < i; jx++) {
			if (this.keys[jx] == EMPTY_SLOT) {
				return jx;
			}
		}

		throw new RuntimeException("Overflowed :(");
	}

	public Iterator<K> iterator() {
		return Iterators.filter(Iterators.forArray(this.byId), Predicates.notNull());
	}

	public void clear() {
		Arrays.fill(this.keys, null);
		Arrays.fill(this.byId, null);
		this.nextId = 0;
		this.size = 0;
	}

	@Override
	public int size() {
		return this.size;
	}

	public CrudeIncrementalIntIdentityHashBiMap<K> copy() {
		return new CrudeIncrementalIntIdentityHashBiMap<>(
			(K[])((Object[])this.keys.clone()), (int[])this.values.clone(), (K[])((Object[])this.byId.clone()), this.nextId, this.size
		);
	}
}
