package net.minecraft.world.level.chunk;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;

public class HashMapPalette<T> implements Palette<T> {
	private final IdMapper<T> registry;
	private final CrudeIncrementalIntIdentityHashBiMap<T> values;
	private final PaletteResize<T> resizeHandler;
	private final Function<CompoundTag, T> reader;
	private final Function<T, CompoundTag> writer;
	private final int bits;

	public HashMapPalette(IdMapper<T> idMapper, int i, PaletteResize<T> paletteResize, Function<CompoundTag, T> function, Function<T, CompoundTag> function2) {
		this.registry = idMapper;
		this.bits = i;
		this.resizeHandler = paletteResize;
		this.reader = function;
		this.writer = function2;
		this.values = new CrudeIncrementalIntIdentityHashBiMap<>(1 << i);
	}

	@Override
	public int idFor(T object) {
		int i = this.values.getId(object);
		if (i == -1) {
			i = this.values.add(object);
			if (i >= 1 << this.bits) {
				i = this.resizeHandler.onResize(this.bits + 1, object);
			}
		}

		return i;
	}

	@Override
	public boolean maybeHas(Predicate<T> predicate) {
		for (int i = 0; i < this.getSize(); i++) {
			if (predicate.test(this.values.byId(i))) {
				return true;
			}
		}

		return false;
	}

	@Nullable
	@Override
	public T valueFor(int i) {
		return this.values.byId(i);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) {
		this.values.clear();
		int i = friendlyByteBuf.readVarInt();

		for (int j = 0; j < i; j++) {
			this.values.add(this.registry.byId(friendlyByteBuf.readVarInt()));
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		int i = this.getSize();
		friendlyByteBuf.writeVarInt(i);

		for (int j = 0; j < i; j++) {
			friendlyByteBuf.writeVarInt(this.registry.getId(this.values.byId(j)));
		}
	}

	@Override
	public int getSerializedSize() {
		int i = FriendlyByteBuf.getVarIntSize(this.getSize());

		for (int j = 0; j < this.getSize(); j++) {
			i += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values.byId(j)));
		}

		return i;
	}

	@Override
	public int getSize() {
		return this.values.size();
	}

	@Override
	public void read(ListTag listTag) {
		this.values.clear();

		for (int i = 0; i < listTag.size(); i++) {
			this.values.add((T)this.reader.apply(listTag.getCompound(i)));
		}
	}

	public void write(ListTag listTag) {
		for (int i = 0; i < this.getSize(); i++) {
			listTag.add((Tag)this.writer.apply(this.values.byId(i)));
		}
	}
}
