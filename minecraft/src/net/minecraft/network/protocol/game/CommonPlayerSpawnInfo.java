package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public record CommonPlayerSpawnInfo(
	ResourceKey<DimensionType> dimensionType,
	ResourceKey<Level> dimension,
	long seed,
	GameType gameType,
	@Nullable GameType previousGameType,
	boolean isDebug,
	boolean isFlat,
	Optional<GlobalPos> lastDeathLocation,
	int portalCooldown
) {
	public CommonPlayerSpawnInfo(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readResourceKey(Registries.DIMENSION_TYPE),
			friendlyByteBuf.readResourceKey(Registries.DIMENSION),
			friendlyByteBuf.readLong(),
			GameType.byId(friendlyByteBuf.readByte()),
			GameType.byNullableId(friendlyByteBuf.readByte()),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readOptional(FriendlyByteBuf::readGlobalPos),
			friendlyByteBuf.readVarInt()
		);
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeResourceKey(this.dimensionType);
		friendlyByteBuf.writeResourceKey(this.dimension);
		friendlyByteBuf.writeLong(this.seed);
		friendlyByteBuf.writeByte(this.gameType.getId());
		friendlyByteBuf.writeByte(GameType.getNullableId(this.previousGameType));
		friendlyByteBuf.writeBoolean(this.isDebug);
		friendlyByteBuf.writeBoolean(this.isFlat);
		friendlyByteBuf.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
		friendlyByteBuf.writeVarInt(this.portalCooldown);
	}
}
