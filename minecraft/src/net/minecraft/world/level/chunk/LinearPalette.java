package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import org.apache.commons.lang3.Validate;

public class LinearPalette<T> implements Palette<T> {
	private final IdMap<T> registry;
	private final T[] values;
	private final PaletteResize<T> resizeHandler;
	private final int bits;
	private int size;

	private LinearPalette(IdMap<T> idMap, int i, PaletteResize<T> paletteResize, List<T> list) {
		this.registry = idMap;
		this.values = (T[])(new Object[1 << i]);
		this.bits = i;
		this.resizeHandler = paletteResize;
		Validate.isTrue(list.size() <= this.values.length, "Can't initialize LinearPalette of size %d with %d entries", this.values.length, list.size());

		for (int j = 0; j < list.size(); j++) {
			this.values[j] = (T)list.get(j);
		}

		this.size = list.size();
	}

	private LinearPalette(IdMap<T> idMap, T[] objects, PaletteResize<T> paletteResize, int i, int j) {
		this.registry = idMap;
		this.values = objects;
		this.resizeHandler = paletteResize;
		this.bits = i;
		this.size = j;
	}

	public static <A> Palette<A> create(int i, IdMap<A> idMap, PaletteResize<A> paletteResize, List<A> list) {
		return new LinearPalette<>(idMap, i, paletteResize, list);
	}

	@Override
	public int idFor(T object) {
		for (int i = 0; i < this.size; i++) {
			if (this.values[i] == object) {
				return i;
			}
		}

		int ix = this.size;
		if (ix < this.values.length) {
			this.values[ix] = object;
			this.size++;
			return ix;
		} else {
			return this.resizeHandler.onResize(this.bits + 1, object);
		}
	}

	@Override
	public boolean maybeHas(Predicate<T> predicate) {
		for (int i = 0; i < this.size; i++) {
			if (predicate.test(this.values[i])) {
				return true;
			}
		}

		return false;
	}

	@Override
	public T valueFor(int i) {
		if (i >= 0 && i < this.size) {
			return this.values[i];
		} else {
			throw new MissingPaletteEntryException(i);
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) {
		this.size = friendlyByteBuf.readVarInt();

		for (int i = 0; i < this.size; i++) {
			this.values[i] = this.registry.byIdOrThrow(friendlyByteBuf.readVarInt());
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.size);

		for (int i = 0; i < this.size; i++) {
			friendlyByteBuf.writeVarInt(this.registry.getId(this.values[i]));
		}
	}

	@Override
	public int getSerializedSize() {
		int i = VarInt.getByteSize(this.getSize());

		for (int j = 0; j < this.getSize(); j++) {
			i += VarInt.getByteSize(this.registry.getId(this.values[j]));
		}

		return i;
	}

	@Override
	public int getSize() {
		return this.size;
	}

	@Override
	public Palette<T> copy(PaletteResize<T> paletteResize) {
		return new LinearPalette<>(this.registry, (T[])((Object[])this.values.clone()), paletteResize, this.bits, this.size);
	}
}
