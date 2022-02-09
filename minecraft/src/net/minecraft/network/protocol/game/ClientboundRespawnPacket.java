package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
	private final Holder<DimensionType> dimensionType;
	private final ResourceKey<Level> dimension;
	private final long seed;
	private final GameType playerGameType;
	@Nullable
	private final GameType previousPlayerGameType;
	private final boolean isDebug;
	private final boolean isFlat;
	private final boolean keepAllPlayerData;

	public ClientboundRespawnPacket(
		Holder<DimensionType> holder, ResourceKey<Level> resourceKey, long l, GameType gameType, @Nullable GameType gameType2, boolean bl, boolean bl2, boolean bl3
	) {
		this.dimensionType = holder;
		this.dimension = resourceKey;
		this.seed = l;
		this.playerGameType = gameType;
		this.previousPlayerGameType = gameType2;
		this.isDebug = bl;
		this.isFlat = bl2;
		this.keepAllPlayerData = bl3;
	}

	public ClientboundRespawnPacket(FriendlyByteBuf friendlyByteBuf) {
		this.dimensionType = friendlyByteBuf.readWithCodec(DimensionType.CODEC);
		this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, friendlyByteBuf.readResourceLocation());
		this.seed = friendlyByteBuf.readLong();
		this.playerGameType = GameType.byId(friendlyByteBuf.readUnsignedByte());
		this.previousPlayerGameType = GameType.byNullableId(friendlyByteBuf.readByte());
		this.isDebug = friendlyByteBuf.readBoolean();
		this.isFlat = friendlyByteBuf.readBoolean();
		this.keepAllPlayerData = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeWithCodec(DimensionType.CODEC, this.dimensionType);
		friendlyByteBuf.writeResourceLocation(this.dimension.location());
		friendlyByteBuf.writeLong(this.seed);
		friendlyByteBuf.writeByte(this.playerGameType.getId());
		friendlyByteBuf.writeByte(GameType.getNullableId(this.previousPlayerGameType));
		friendlyByteBuf.writeBoolean(this.isDebug);
		friendlyByteBuf.writeBoolean(this.isFlat);
		friendlyByteBuf.writeBoolean(this.keepAllPlayerData);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRespawn(this);
	}

	public Holder<DimensionType> getDimensionType() {
		return this.dimensionType;
	}

	public ResourceKey<Level> getDimension() {
		return this.dimension;
	}

	public long getSeed() {
		return this.seed;
	}

	public GameType getPlayerGameType() {
		return this.playerGameType;
	}

	@Nullable
	public GameType getPreviousPlayerGameType() {
		return this.previousPlayerGameType;
	}

	public boolean isDebug() {
		return this.isDebug;
	}

	public boolean isFlat() {
		return this.isFlat;
	}

	public boolean shouldKeepAllPlayerData() {
		return this.keepAllPlayerData;
	}
}
