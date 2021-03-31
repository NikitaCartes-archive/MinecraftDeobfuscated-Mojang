package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public interface Palette<T> {
	int idFor(T object);

	boolean maybeHas(Predicate<T> predicate);

	@Nullable
	T valueFor(int i);

	void read(FriendlyByteBuf friendlyByteBuf);

	void write(FriendlyByteBuf friendlyByteBuf);

	int getSerializedSize();

	int getSize();

	void read(ListTag listTag);
}
