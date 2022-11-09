package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public record ClientboundLoginPacket(
	int playerId,
	boolean hardcore,
	GameType gameType,
	@Nullable GameType previousGameType,
	Set<ResourceKey<Level>> levels,
	RegistryAccess.Frozen registryHolder,
	ResourceKey<DimensionType> dimensionType,
	ResourceKey<Level> dimension,
	long seed,
	int maxPlayers,
	int chunkRadius,
	int simulationDistance,
	boolean reducedDebugInfo,
	boolean showDeathScreen,
	boolean isDebug,
	boolean isFlat,
	Optional<GlobalPos> lastDeathLocation
) implements Packet<ClientGamePacketListener> {
	public ClientboundLoginPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readInt(),
			friendlyByteBuf.readBoolean(),
			GameType.byId(friendlyByteBuf.readByte()),
			GameType.byNullableId(friendlyByteBuf.readByte()),
			friendlyByteBuf.readCollection(Sets::newHashSetWithExpectedSize, friendlyByteBufx -> friendlyByteBufx.readResourceKey(Registries.DIMENSION)),
			friendlyByteBuf.readWithCodec(RegistrySynchronization.NETWORK_CODEC).freeze(),
			friendlyByteBuf.readResourceKey(Registries.DIMENSION_TYPE),
			friendlyByteBuf.readResourceKey(Registries.DIMENSION),
			friendlyByteBuf.readLong(),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readOptional(FriendlyByteBuf::readGlobalPos)
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.playerId);
		friendlyByteBuf.writeBoolean(this.hardcore);
		friendlyByteBuf.writeByte(this.gameType.getId());
		friendlyByteBuf.writeByte(GameType.getNullableId(this.previousGameType));
		friendlyByteBuf.writeCollection(this.levels, FriendlyByteBuf::writeResourceKey);
		friendlyByteBuf.writeWithCodec(RegistrySynchronization.NETWORK_CODEC, this.registryHolder);
		friendlyByteBuf.writeResourceKey(this.dimensionType);
		friendlyByteBuf.writeResourceKey(this.dimension);
		friendlyByteBuf.writeLong(this.seed);
		friendlyByteBuf.writeVarInt(this.maxPlayers);
		friendlyByteBuf.writeVarInt(this.chunkRadius);
		friendlyByteBuf.writeVarInt(this.simulationDistance);
		friendlyByteBuf.writeBoolean(this.reducedDebugInfo);
		friendlyByteBuf.writeBoolean(this.showDeathScreen);
		friendlyByteBuf.writeBoolean(this.isDebug);
		friendlyByteBuf.writeBoolean(this.isFlat);
		friendlyByteBuf.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleLogin(this);
	}
}
