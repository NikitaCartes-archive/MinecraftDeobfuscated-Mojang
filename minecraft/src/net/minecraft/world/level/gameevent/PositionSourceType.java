package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public interface PositionSourceType<T extends PositionSource> {
	PositionSourceType<BlockPositionSource> BLOCK = register("block", new BlockPositionSource.Type());
	PositionSourceType<EntityPositionSource> ENTITY = register("entity", new EntityPositionSource.Type());

	T read(FriendlyByteBuf friendlyByteBuf);

	void write(FriendlyByteBuf friendlyByteBuf, T positionSource);

	Codec<T> codec();

	static <S extends PositionSourceType<T>, T extends PositionSource> S register(String string, S positionSourceType) {
		return Registry.register(BuiltInRegistries.POSITION_SOURCE_TYPE, string, positionSourceType);
	}

	static PositionSource fromNetwork(FriendlyByteBuf friendlyByteBuf) {
		PositionSourceType<?> positionSourceType = friendlyByteBuf.readById(BuiltInRegistries.POSITION_SOURCE_TYPE);
		if (positionSourceType == null) {
			throw new IllegalArgumentException("Unknown position source type");
		} else {
			return positionSourceType.read(friendlyByteBuf);
		}
	}

	static <T extends PositionSource> void toNetwork(T positionSource, FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeId(BuiltInRegistries.POSITION_SOURCE_TYPE, positionSource.getType());
		((PositionSourceType<T>)positionSource.getType()).write(friendlyByteBuf, positionSource);
	}
}
