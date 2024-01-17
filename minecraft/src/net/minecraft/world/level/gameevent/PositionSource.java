package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface PositionSource {
	Codec<PositionSource> CODEC = BuiltInRegistries.POSITION_SOURCE_TYPE.byNameCodec().dispatch(PositionSource::getType, PositionSourceType::codec);
	StreamCodec<RegistryFriendlyByteBuf, PositionSource> STREAM_CODEC = ByteBufCodecs.registry(Registries.POSITION_SOURCE_TYPE)
		.dispatch(PositionSource::getType, PositionSourceType::streamCodec);

	Optional<Vec3> getPosition(Level level);

	PositionSourceType<? extends PositionSource> getType();
}
