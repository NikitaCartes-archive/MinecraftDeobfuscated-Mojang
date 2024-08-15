package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public interface Palette<T> {
	int idFor(T object);

	boolean maybeHas(Predicate<T> predicate);

	T valueFor(int i);

	void read(FriendlyByteBuf friendlyByteBuf);

	void write(FriendlyByteBuf friendlyByteBuf);

	int getSerializedSize();

	int getSize();

	Palette<T> copy(PaletteResize<T> paletteResize);

	public interface Factory {
		<A> Palette<A> create(int i, IdMap<A> idMap, PaletteResize<A> paletteResize, List<A> list);
	}
}
