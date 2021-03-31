package net.minecraft.world.level.chunk;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public class LinearPalette<T> implements Palette<T> {
	private final IdMapper<T> registry;
	private final T[] values;
	private final PaletteResize<T> resizeHandler;
	private final Function<CompoundTag, T> reader;
	private final int bits;
	private int size;

	public LinearPalette(IdMapper<T> idMapper, int i, PaletteResize<T> paletteResize, Function<CompoundTag, T> function) {
		this.registry = idMapper;
		this.values = (T[])(new Object[1 << i]);
		this.bits = i;
		this.resizeHandler = paletteResize;
		this.reader = function;
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

	@Nullable
	@Override
	public T valueFor(int i) {
		return i >= 0 && i < this.size ? this.values[i] : null;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) {
		this.size = friendlyByteBuf.readVarInt();

		for (int i = 0; i < this.size; i++) {
			this.values[i] = this.registry.byId(friendlyByteBuf.readVarInt());
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
		int i = FriendlyByteBuf.getVarIntSize(this.getSize());

		for (int j = 0; j < this.getSize(); j++) {
			i += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values[j]));
		}

		return i;
	}

	@Override
	public int getSize() {
		return this.size;
	}

	@Override
	public void read(ListTag listTag) {
		for (int i = 0; i < listTag.size(); i++) {
			this.values[i] = (T)this.reader.apply(listTag.getCompound(i));
		}

		this.size = listTag.size();
	}
}
