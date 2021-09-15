package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public record ClientboundLoginPacket() implements Packet {
	private final int playerId;
	private final boolean hardcore;
	private final GameType gameType;
	@Nullable
	private final GameType previousGameType;
	private final Set<ResourceKey<Level>> levels;
	private final RegistryAccess.RegistryHolder registryHolder;
	private final DimensionType dimensionType;
	private final ResourceKey<Level> dimension;
	private final long seed;
	private final int maxPlayers;
	private final int chunkRadius;
	private final boolean reducedDebugInfo;
	private final boolean showDeathScreen;
	private final boolean isDebug;
	private final boolean isFlat;

	public ClientboundLoginPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readInt(),
			friendlyByteBuf.readBoolean(),
			GameType.byId(friendlyByteBuf.readByte()),
			GameType.byNullableId(friendlyByteBuf.readByte()),
			friendlyByteBuf.readCollection(
				Sets::newHashSetWithExpectedSize, friendlyByteBufx -> ResourceKey.create(Registry.DIMENSION_REGISTRY, friendlyByteBufx.readResourceLocation())
			),
			friendlyByteBuf.readWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC),
			(DimensionType)friendlyByteBuf.readWithCodec(DimensionType.CODEC).get(),
			ResourceKey.create(Registry.DIMENSION_REGISTRY, friendlyByteBuf.readResourceLocation()),
			friendlyByteBuf.readLong(),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean()
		);
	}

	public ClientboundLoginPacket(
		int i,
		boolean bl,
		GameType gameType,
		@Nullable GameType gameType2,
		Set<ResourceKey<Level>> set,
		RegistryAccess.RegistryHolder registryHolder,
		DimensionType dimensionType,
		ResourceKey<Level> resourceKey,
		long l,
		int j,
		int k,
		boolean bl2,
		boolean bl3,
		boolean bl4,
		boolean bl5
	) {
		this.playerId = i;
		this.hardcore = bl;
		this.gameType = gameType;
		this.previousGameType = gameType2;
		this.levels = set;
		this.registryHolder = registryHolder;
		this.dimensionType = dimensionType;
		this.dimension = resourceKey;
		this.seed = l;
		this.maxPlayers = j;
		this.chunkRadius = k;
		this.reducedDebugInfo = bl2;
		this.showDeathScreen = bl3;
		this.isDebug = bl4;
		this.isFlat = bl5;
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.playerId);
		friendlyByteBuf.writeBoolean(this.hardcore);
		friendlyByteBuf.writeByte(this.gameType.getId());
		friendlyByteBuf.writeByte(GameType.getNullableId(this.previousGameType));
		friendlyByteBuf.writeCollection(this.levels, (friendlyByteBufx, resourceKey) -> friendlyByteBufx.writeResourceLocation(resourceKey.location()));
		friendlyByteBuf.writeWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC, this.registryHolder);
		friendlyByteBuf.writeWithCodec(DimensionType.CODEC, () -> this.dimensionType);
		friendlyByteBuf.writeResourceLocation(this.dimension.location());
		friendlyByteBuf.writeLong(this.seed);
		friendlyByteBuf.writeVarInt(this.maxPlayers);
		friendlyByteBuf.writeVarInt(this.chunkRadius);
		friendlyByteBuf.writeBoolean(this.reducedDebugInfo);
		friendlyByteBuf.writeBoolean(this.showDeathScreen);
		friendlyByteBuf.writeBoolean(this.isDebug);
		friendlyByteBuf.writeBoolean(this.isFlat);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleLogin(this);
	}
}
