package net.minecraft.world.level.chunk;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.DebugBuffer;
import net.minecraft.util.Mth;
import net.minecraft.util.ThreadingDetector;

public class PalettedContainer<T> implements PaletteResize<T> {
	private static final int SIZE = 4096;
	public static final int GLOBAL_PALETTE_BITS = 9;
	public static final int MIN_PALETTE_SIZE = 4;
	private final Palette<T> globalPalette;
	private final PaletteResize<T> dummyPaletteResize = (i, objectx) -> 0;
	private final IdMapper<T> registry;
	private final Function<CompoundTag, T> reader;
	private final Function<T, CompoundTag> writer;
	private final T defaultValue;
	protected BitStorage storage;
	private Palette<T> palette;
	private int bits;
	private final Semaphore lock = new Semaphore(1);
	@Nullable
	private final DebugBuffer<Pair<Thread, StackTraceElement[]>> traces = null;

	public void acquire() {
		if (this.traces != null) {
			Thread thread = Thread.currentThread();
			this.traces.push(Pair.of(thread, thread.getStackTrace()));
		}

		ThreadingDetector.checkAndLock(this.lock, this.traces, "PalettedContainer");
	}

	public void release() {
		this.lock.release();
	}

	public PalettedContainer(Palette<T> palette, IdMapper<T> idMapper, Function<CompoundTag, T> function, Function<T, CompoundTag> function2, T object) {
		this.globalPalette = palette;
		this.registry = idMapper;
		this.reader = function;
		this.writer = function2;
		this.defaultValue = object;
		this.setBits(4);
	}

	private static int getIndex(int i, int j, int k) {
		return j << 8 | k << 4 | i;
	}

	private void setBits(int i) {
		if (i != this.bits) {
			this.bits = i;
			if (this.bits <= 4) {
				this.bits = 4;
				this.palette = new LinearPalette<>(this.registry, this.bits, this, this.reader);
			} else if (this.bits < 9) {
				this.palette = new HashMapPalette<>(this.registry, this.bits, this, this.reader, this.writer);
			} else {
				this.palette = this.globalPalette;
				this.bits = Mth.ceillog2(this.registry.size());
			}

			this.palette.idFor(this.defaultValue);
			this.storage = new BitStorage(this.bits, 4096);
		}
	}

	@Override
	public int onResize(int i, T object) {
		BitStorage bitStorage = this.storage;
		Palette<T> palette = this.palette;
		this.setBits(i);

		for (int j = 0; j < bitStorage.getSize(); j++) {
			T object2 = palette.valueFor(bitStorage.get(j));
			if (object2 != null) {
				this.set(j, object2);
			}
		}

		return this.palette.idFor(object);
	}

	public T getAndSet(int i, int j, int k, T object) {
		Object var6;
		try {
			this.acquire();
			T object2 = this.getAndSet(getIndex(i, j, k), object);
			var6 = object2;
		} finally {
			this.release();
		}

		return (T)var6;
	}

	public T getAndSetUnchecked(int i, int j, int k, T object) {
		return this.getAndSet(getIndex(i, j, k), object);
	}

	private T getAndSet(int i, T object) {
		int j = this.palette.idFor(object);
		int k = this.storage.getAndSet(i, j);
		T object2 = this.palette.valueFor(k);
		return object2 == null ? this.defaultValue : object2;
	}

	public void set(int i, int j, int k, T object) {
		try {
			this.acquire();
			this.set(getIndex(i, j, k), object);
		} finally {
			this.release();
		}
	}

	private void set(int i, T object) {
		int j = this.palette.idFor(object);
		this.storage.set(i, j);
	}

	public T get(int i, int j, int k) {
		return this.get(getIndex(i, j, k));
	}

	protected T get(int i) {
		T object = this.palette.valueFor(this.storage.get(i));
		return object == null ? this.defaultValue : object;
	}

	public void read(FriendlyByteBuf friendlyByteBuf) {
		try {
			this.acquire();
			int i = friendlyByteBuf.readByte();
			if (this.bits != i) {
				this.setBits(i);
			}

			this.palette.read(friendlyByteBuf);
			friendlyByteBuf.readLongArray(this.storage.getRaw());
		} finally {
			this.release();
		}
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		try {
			this.acquire();
			friendlyByteBuf.writeByte(this.bits);
			this.palette.write(friendlyByteBuf);
			friendlyByteBuf.writeLongArray(this.storage.getRaw());
		} finally {
			this.release();
		}
	}

	public void read(ListTag listTag, long[] ls) {
		try {
			this.acquire();
			int i = Math.max(4, Mth.ceillog2(listTag.size()));
			if (i != this.bits) {
				this.setBits(i);
			}

			this.palette.read(listTag);
			int j = ls.length * 64 / 4096;
			if (this.palette == this.globalPalette) {
				Palette<T> palette = new HashMapPalette<>(this.registry, i, this.dummyPaletteResize, this.reader, this.writer);
				palette.read(listTag);
				BitStorage bitStorage = new BitStorage(i, 4096, ls);

				for (int k = 0; k < 4096; k++) {
					this.storage.set(k, this.globalPalette.idFor(palette.valueFor(bitStorage.get(k))));
				}
			} else if (j == this.bits) {
				System.arraycopy(ls, 0, this.storage.getRaw(), 0, ls.length);
			} else {
				BitStorage bitStorage2 = new BitStorage(j, 4096, ls);

				for (int l = 0; l < 4096; l++) {
					this.storage.set(l, bitStorage2.get(l));
				}
			}
		} finally {
			this.release();
		}
	}

	public void write(CompoundTag compoundTag, String string, String string2) {
		try {
			this.acquire();
			HashMapPalette<T> hashMapPalette = new HashMapPalette<>(this.registry, this.bits, this.dummyPaletteResize, this.reader, this.writer);
			T object = this.defaultValue;
			int i = hashMapPalette.idFor(this.defaultValue);
			int[] is = new int[4096];

			for (int j = 0; j < 4096; j++) {
				T object2 = this.get(j);
				if (object2 != object) {
					object = object2;
					i = hashMapPalette.idFor(object2);
				}

				is[j] = i;
			}

			ListTag listTag = new ListTag();
			hashMapPalette.write(listTag);
			compoundTag.put(string, listTag);
			int k = Math.max(4, Mth.ceillog2(listTag.size()));
			BitStorage bitStorage = new BitStorage(k, 4096);

			for (int l = 0; l < is.length; l++) {
				bitStorage.set(l, is[l]);
			}

			compoundTag.putLongArray(string2, bitStorage.getRaw());
		} finally {
			this.release();
		}
	}

	public int getSerializedSize() {
		return 1 + this.palette.getSerializedSize() + FriendlyByteBuf.getVarIntSize(this.storage.getSize()) + this.storage.getRaw().length * 8;
	}

	public boolean maybeHas(Predicate<T> predicate) {
		return this.palette.maybeHas(predicate);
	}

	public void count(PalettedContainer.CountConsumer<T> countConsumer) {
		Int2IntMap int2IntMap = new Int2IntOpenHashMap();
		this.storage.getAll(i -> int2IntMap.put(i, int2IntMap.get(i) + 1));
		int2IntMap.int2IntEntrySet().forEach(entry -> countConsumer.accept(this.palette.valueFor(entry.getIntKey()), entry.getIntValue()));
	}

	@FunctionalInterface
	public interface CountConsumer<T> {
		void accept(T object, int i);
	}
}
