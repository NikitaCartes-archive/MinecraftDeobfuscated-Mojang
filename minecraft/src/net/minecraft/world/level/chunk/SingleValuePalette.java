package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import org.apache.commons.lang3.Validate;

public class SingleValuePalette<T> implements Palette<T> {
	private final IdMap<T> registry;
	@Nullable
	private T value;
	private final PaletteResize<T> resizeHandler;

	public SingleValuePalette(IdMap<T> idMap, PaletteResize<T> paletteResize, List<T> list) {
		this.registry = idMap;
		this.resizeHandler = paletteResize;
		if (list.size() > 0) {
			Validate.isTrue(list.size() <= 1, "Can't initialize SingleValuePalette with %d values.", (long)list.size());
			this.value = (T)list.get(0);
		}
	}

	public static <A> Palette<A> create(int i, IdMap<A> idMap, PaletteResize<A> paletteResize, List<A> list) {
		return new SingleValuePalette<>(idMap, paletteResize, list);
	}

	@Override
	public int idFor(T object) {
		if (this.value != null && this.value != object) {
			return this.resizeHandler.onResize(1, object);
		} else {
			this.value = object;
			return 0;
		}
	}

	@Override
	public boolean maybeHas(Predicate<T> predicate) {
		if (this.value == null) {
			throw new IllegalStateException("Use of an uninitialized palette");
		} else {
			return predicate.test(this.value);
		}
	}

	@Override
	public T valueFor(int i) {
		if (this.value != null && i == 0) {
			return this.value;
		} else {
			throw new IllegalStateException("Missing Palette entry for id " + i + ".");
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) {
		this.value = this.registry.byIdOrThrow(friendlyByteBuf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		if (this.value == null) {
			throw new IllegalStateException("Use of an uninitialized palette");
		} else {
			friendlyByteBuf.writeVarInt(this.registry.getId(this.value));
		}
	}

	@Override
	public int getSerializedSize() {
		if (this.value == null) {
			throw new IllegalStateException("Use of an uninitialized palette");
		} else {
			return VarInt.getByteSize(this.registry.getId(this.value));
		}
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public Palette<T> copy() {
		if (this.value == null) {
			throw new IllegalStateException("Use of an uninitialized palette");
		} else {
			return this;
		}
	}
}
