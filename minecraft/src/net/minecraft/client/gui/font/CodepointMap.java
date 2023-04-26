package net.minecraft.client.gui.font;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CodepointMap<T> {
	private static final int BLOCK_BITS = 8;
	private static final int BLOCK_SIZE = 256;
	private static final int IN_BLOCK_MASK = 255;
	private static final int MAX_BLOCK = 4351;
	private static final int BLOCK_COUNT = 4352;
	private final T[] empty;
	private final T[][] blockMap;
	private final IntFunction<T[]> blockConstructor;

	public CodepointMap(IntFunction<T[]> intFunction, IntFunction<T[][]> intFunction2) {
		this.empty = (T[])((Object[])intFunction.apply(256));
		this.blockMap = (T[][])((Object[][])intFunction2.apply(4352));
		Arrays.fill(this.blockMap, this.empty);
		this.blockConstructor = intFunction;
	}

	public void clear() {
		Arrays.fill(this.blockMap, this.empty);
	}

	@Nullable
	public T get(int i) {
		int j = i >> 8;
		int k = i & 0xFF;
		return this.blockMap[j][k];
	}

	@Nullable
	public T put(int i, T object) {
		int j = i >> 8;
		int k = i & 0xFF;
		T[] objects = this.blockMap[j];
		if (objects == this.empty) {
			objects = (T[])((Object[])this.blockConstructor.apply(256));
			this.blockMap[j] = objects;
			objects[k] = object;
			return null;
		} else {
			T object2 = objects[k];
			objects[k] = object;
			return object2;
		}
	}

	public T computeIfAbsent(int i, IntFunction<T> intFunction) {
		int j = i >> 8;
		int k = i & 0xFF;
		T[] objects = this.blockMap[j];
		T object = objects[k];
		if (object != null) {
			return object;
		} else {
			if (objects == this.empty) {
				objects = (T[])((Object[])this.blockConstructor.apply(256));
				this.blockMap[j] = objects;
			}

			T object2 = (T)intFunction.apply(i);
			objects[k] = object2;
			return object2;
		}
	}

	@Nullable
	public T remove(int i) {
		int j = i >> 8;
		int k = i & 0xFF;
		T[] objects = this.blockMap[j];
		if (objects == this.empty) {
			return null;
		} else {
			T object = objects[k];
			objects[k] = null;
			return object;
		}
	}

	public void forEach(CodepointMap.Output<T> output) {
		for (int i = 0; i < this.blockMap.length; i++) {
			T[] objects = this.blockMap[i];
			if (objects != this.empty) {
				for (int j = 0; j < objects.length; j++) {
					T object = objects[j];
					if (object != null) {
						int k = i << 8 | j;
						output.accept(k, object);
					}
				}
			}
		}
	}

	public IntSet keySet() {
		IntOpenHashSet intOpenHashSet = new IntOpenHashSet();
		this.forEach((i, object) -> intOpenHashSet.add(i));
		return intOpenHashSet;
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Output<T> {
		void accept(int i, T object);
	}
}
