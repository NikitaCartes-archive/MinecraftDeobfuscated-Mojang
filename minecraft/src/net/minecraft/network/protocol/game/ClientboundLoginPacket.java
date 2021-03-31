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

public class ClientboundLoginPacket implements Packet<ClientGamePacketListener> {
	private static final int HARDCORE_FLAG = 8;
	private final int playerId;
	private final long seed;
	private final boolean hardcore;
	private final GameType gameType;
	@Nullable
	private final GameType previousGameType;
	private final Set<ResourceKey<Level>> levels;
	private final RegistryAccess.RegistryHolder registryHolder;
	private final DimensionType dimensionType;
	private final ResourceKey<Level> dimension;
	private final int maxPlayers;
	private final int chunkRadius;
	private final boolean reducedDebugInfo;
	private final boolean showDeathScreen;
	private final boolean isDebug;
	private final boolean isFlat;

	public ClientboundLoginPacket(
		int i,
		GameType gameType,
		@Nullable GameType gameType2,
		long l,
		boolean bl,
		Set<ResourceKey<Level>> set,
		RegistryAccess.RegistryHolder registryHolder,
		DimensionType dimensionType,
		ResourceKey<Level> resourceKey,
		int j,
		int k,
		boolean bl2,
		boolean bl3,
		boolean bl4,
		boolean bl5
	) {
		this.playerId = i;
		this.levels = set;
		this.registryHolder = registryHolder;
		this.dimensionType = dimensionType;
		this.dimension = resourceKey;
		this.seed = l;
		this.gameType = gameType;
		this.previousGameType = gameType2;
		this.maxPlayers = j;
		this.hardcore = bl;
		this.chunkRadius = k;
		this.reducedDebugInfo = bl2;
		this.showDeathScreen = bl3;
		this.isDebug = bl4;
		this.isFlat = bl5;
	}

	public ClientboundLoginPacket(FriendlyByteBuf friendlyByteBuf) {
		this.playerId = friendlyByteBuf.readInt();
		this.hardcore = friendlyByteBuf.readBoolean();
		this.gameType = GameType.byId(friendlyByteBuf.readByte());
		this.previousGameType = GameType.byNullableId(friendlyByteBuf.readByte());
		this.levels = friendlyByteBuf.readCollection(
			Sets::newHashSetWithExpectedSize, friendlyByteBufx -> ResourceKey.create(Registry.DIMENSION_REGISTRY, friendlyByteBufx.readResourceLocation())
		);
		this.registryHolder = friendlyByteBuf.readWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC);
		this.dimensionType = (DimensionType)friendlyByteBuf.readWithCodec(DimensionType.CODEC).get();
		this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, friendlyByteBuf.readResourceLocation());
		this.seed = friendlyByteBuf.readLong();
		this.maxPlayers = friendlyByteBuf.readVarInt();
		this.chunkRadius = friendlyByteBuf.readVarInt();
		this.reducedDebugInfo = friendlyByteBuf.readBoolean();
		this.showDeathScreen = friendlyByteBuf.readBoolean();
		this.isDebug = friendlyByteBuf.readBoolean();
		this.isFlat = friendlyByteBuf.readBoolean();
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

	public int getPlayerId() {
		return this.playerId;
	}

	public long getSeed() {
		return this.seed;
	}

	public boolean isHardcore() {
		return this.hardcore;
	}

	public GameType getGameType() {
		return this.gameType;
	}

	@Nullable
	public GameType getPreviousGameType() {
		return this.previousGameType;
	}

	public Set<ResourceKey<Level>> levels() {
		return this.levels;
	}

	public RegistryAccess registryAccess() {
		return this.registryHolder;
	}

	public DimensionType getDimensionType() {
		return this.dimensionType;
	}

	public ResourceKey<Level> getDimension() {
		return this.dimension;
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	public int getChunkRadius() {
		return this.chunkRadius;
	}

	public boolean isReducedDebugInfo() {
		return this.reducedDebugInfo;
	}

	public boolean shouldShowDeathScreen() {
		return this.showDeathScreen;
	}

	public boolean isDebug() {
		return this.isDebug;
	}

	public boolean isFlat() {
		return this.isFlat;
	}
}
