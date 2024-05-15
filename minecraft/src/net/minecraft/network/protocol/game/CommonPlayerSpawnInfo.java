package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public record CommonPlayerSpawnInfo(
	Holder<DimensionType> dimensionType,
	ResourceKey<Level> dimension,
	long seed,
	GameType gameType,
	@Nullable GameType previousGameType,
	boolean isDebug,
	boolean isFlat,
	Optional<GlobalPos> lastDeathLocation,
	int portalCooldown
) {
	public CommonPlayerSpawnInfo(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this(
			DimensionType.STREAM_CODEC.decode(registryFriendlyByteBuf),
			registryFriendlyByteBuf.readResourceKey(Registries.DIMENSION),
			registryFriendlyByteBuf.readLong(),
			GameType.byId(registryFriendlyByteBuf.readByte()),
			GameType.byNullableId(registryFriendlyByteBuf.readByte()),
			registryFriendlyByteBuf.readBoolean(),
			registryFriendlyByteBuf.readBoolean(),
			registryFriendlyByteBuf.readOptional(FriendlyByteBuf::readGlobalPos),
			registryFriendlyByteBuf.readVarInt()
		);
	}

	public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		DimensionType.STREAM_CODEC.encode(registryFriendlyByteBuf, this.dimensionType);
		registryFriendlyByteBuf.writeResourceKey(this.dimension);
		registryFriendlyByteBuf.writeLong(this.seed);
		registryFriendlyByteBuf.writeByte(this.gameType.getId());
		registryFriendlyByteBuf.writeByte(GameType.getNullableId(this.previousGameType));
		registryFriendlyByteBuf.writeBoolean(this.isDebug);
		registryFriendlyByteBuf.writeBoolean(this.isFlat);
		registryFriendlyByteBuf.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
		registryFriendlyByteBuf.writeVarInt(this.portalCooldown);
	}
}
