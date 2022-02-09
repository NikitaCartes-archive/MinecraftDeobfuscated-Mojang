package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
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
	Holder<DimensionType> dimensionType,
	ResourceKey<Level> dimension,
	long seed,
	int maxPlayers,
	int chunkRadius,
	int simulationDistance,
	boolean reducedDebugInfo,
	boolean showDeathScreen,
	boolean isDebug,
	boolean isFlat
) implements Packet<ClientGamePacketListener> {
	public ClientboundLoginPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readInt(),
			friendlyByteBuf.readBoolean(),
			GameType.byId(friendlyByteBuf.readByte()),
			GameType.byNullableId(friendlyByteBuf.readByte()),
			friendlyByteBuf.readCollection(
				Sets::newHashSetWithExpectedSize, friendlyByteBufx -> ResourceKey.create(Registry.DIMENSION_REGISTRY, friendlyByteBufx.readResourceLocation())
			),
			friendlyByteBuf.readWithCodec(RegistryAccess.NETWORK_CODEC).freeze(),
			friendlyByteBuf.readWithCodec(DimensionType.CODEC),
			ResourceKey.create(Registry.DIMENSION_REGISTRY, friendlyByteBuf.readResourceLocation()),
			friendlyByteBuf.readLong(),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean()
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.playerId);
		friendlyByteBuf.writeBoolean(this.hardcore);
		friendlyByteBuf.writeByte(this.gameType.getId());
		friendlyByteBuf.writeByte(GameType.getNullableId(this.previousGameType));
		friendlyByteBuf.writeCollection(this.levels, (friendlyByteBufx, resourceKey) -> friendlyByteBufx.writeResourceLocation(resourceKey.location()));
		friendlyByteBuf.writeWithCodec(RegistryAccess.NETWORK_CODEC, this.registryHolder);
		friendlyByteBuf.writeWithCodec(DimensionType.CODEC, this.dimensionType);
		friendlyByteBuf.writeResourceLocation(this.dimension.location());
		friendlyByteBuf.writeLong(this.seed);
		friendlyByteBuf.writeVarInt(this.maxPlayers);
		friendlyByteBuf.writeVarInt(this.chunkRadius);
		friendlyByteBuf.writeVarInt(this.simulationDistance);
		friendlyByteBuf.writeBoolean(this.reducedDebugInfo);
		friendlyByteBuf.writeBoolean(this.showDeathScreen);
		friendlyByteBuf.writeBoolean(this.isDebug);
		friendlyByteBuf.writeBoolean(this.isFlat);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleLogin(this);
	}
}
