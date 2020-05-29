package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public class GlobalPalette<T> implements Palette<T> {
	private final IdMapper<T> registry;
	private final T defaultValue;

	public GlobalPalette(IdMapper<T> idMapper, T object) {
		this.registry = idMapper;
		this.defaultValue = object;
	}

	@Override
	public int idFor(T object) {
		int i = this.registry.getId(object);
		return i == -1 ? 0 : i;
	}

	@Override
	public boolean maybeHas(Predicate<T> predicate) {
		return true;
	}

	@Override
	public T valueFor(int i) {
		T object = this.registry.byId(i);
		return object == null ? this.defaultValue : object;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) {
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
	}

	@Override
	public int getSerializedSize() {
		return FriendlyByteBuf.getVarIntSize(0);
	}

	@Override
	public void read(ListTag listTag) {
	}
}
