package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundLoginPacket implements Packet<ClientGamePacketListener> {
	private int playerId;
	private long seed;
	private boolean hardcore;
	private GameType gameType;
	private GameType previousGameType;
	private Set<ResourceKey<Level>> levels;
	private RegistryAccess.RegistryHolder registryHolder;
	private DimensionType dimensionType;
	private ResourceKey<Level> dimension;
	private int maxPlayers;
	private int chunkRadius;
	private boolean reducedDebugInfo;
	private boolean showDeathScreen;
	private boolean isDebug;
	private boolean isFlat;

	public ClientboundLoginPacket() {
	}

	public ClientboundLoginPacket(
		int i,
		GameType gameType,
		GameType gameType2,
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

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.playerId = friendlyByteBuf.readInt();
		this.hardcore = friendlyByteBuf.readBoolean();
		this.gameType = GameType.byId(friendlyByteBuf.readByte());
		this.previousGameType = GameType.byId(friendlyByteBuf.readByte());
		int i = friendlyByteBuf.readVarInt();
		this.levels = Sets.<ResourceKey<Level>>newHashSet();

		for (int j = 0; j < i; j++) {
			this.levels.add(ResourceKey.create(Registry.DIMENSION_REGISTRY, friendlyByteBuf.readResourceLocation()));
		}

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
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeInt(this.playerId);
		friendlyByteBuf.writeBoolean(this.hardcore);
		friendlyByteBuf.writeByte(this.gameType.getId());
		friendlyByteBuf.writeByte(this.previousGameType.getId());
		friendlyByteBuf.writeVarInt(this.levels.size());

		for (ResourceKey<Level> resourceKey : this.levels) {
			friendlyByteBuf.writeResourceLocation(resourceKey.location());
		}

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

	@Environment(EnvType.CLIENT)
	public int getPlayerId() {
		return this.playerId;
	}

	@Environment(EnvType.CLIENT)
	public long getSeed() {
		return this.seed;
	}

	@Environment(EnvType.CLIENT)
	public boolean isHardcore() {
		return this.hardcore;
	}

	@Environment(EnvType.CLIENT)
	public GameType getGameType() {
		return this.gameType;
	}

	@Environment(EnvType.CLIENT)
	public GameType getPreviousGameType() {
		return this.previousGameType;
	}

	@Environment(EnvType.CLIENT)
	public Set<ResourceKey<Level>> levels() {
		return this.levels;
	}

	@Environment(EnvType.CLIENT)
	public RegistryAccess registryAccess() {
		return this.registryHolder;
	}

	@Environment(EnvType.CLIENT)
	public DimensionType getDimensionType() {
		return this.dimensionType;
	}

	@Environment(EnvType.CLIENT)
	public ResourceKey<Level> getDimension() {
		return this.dimension;
	}

	@Environment(EnvType.CLIENT)
	public int getChunkRadius() {
		return this.chunkRadius;
	}

	@Environment(EnvType.CLIENT)
	public boolean isReducedDebugInfo() {
		return this.reducedDebugInfo;
	}

	@Environment(EnvType.CLIENT)
	public boolean shouldShowDeathScreen() {
		return this.showDeathScreen;
	}

	@Environment(EnvType.CLIENT)
	public boolean isDebug() {
		return this.isDebug;
	}

	@Environment(EnvType.CLIENT)
	public boolean isFlat() {
		return this.isFlat;
	}
}
