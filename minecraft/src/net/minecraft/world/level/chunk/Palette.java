package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public interface Palette<T> {
	int idFor(T object);

	boolean maybeHas(T object);

	@Nullable
	T valueFor(int i);

	@Environment(EnvType.CLIENT)
	void read(FriendlyByteBuf friendlyByteBuf);

	void write(FriendlyByteBuf friendlyByteBuf);

	int getSerializedSize();

	void read(ListTag listTag);
}
