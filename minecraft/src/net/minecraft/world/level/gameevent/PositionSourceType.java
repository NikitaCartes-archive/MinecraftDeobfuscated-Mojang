package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface PositionSourceType<T extends PositionSource> {
	PositionSourceType<BlockPositionSource> BLOCK = register("block", new BlockPositionSource.Type());
	PositionSourceType<EntityPositionSource> ENTITY = register("entity", new EntityPositionSource.Type());

	T read(FriendlyByteBuf friendlyByteBuf);

	void write(FriendlyByteBuf friendlyByteBuf, T positionSource);

	Codec<T> codec();

	static <S extends PositionSourceType<T>, T extends PositionSource> S register(String string, S positionSourceType) {
		return Registry.register(Registry.POSITION_SOURCE_TYPE, string, positionSourceType);
	}

	static PositionSource fromNetwork(FriendlyByteBuf friendlyByteBuf) {
		ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
		return ((PositionSourceType)Registry.POSITION_SOURCE_TYPE
				.getOptional(resourceLocation)
				.orElseThrow(() -> new IllegalArgumentException("Unknown position source type " + resourceLocation)))
			.read(friendlyByteBuf);
	}

	static <T extends PositionSource> void toNetwork(T positionSource, FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeResourceLocation(Registry.POSITION_SOURCE_TYPE.getKey(positionSource.getType()));
		((PositionSourceType<T>)positionSource.getType()).write(friendlyByteBuf, positionSource);
	}
}
