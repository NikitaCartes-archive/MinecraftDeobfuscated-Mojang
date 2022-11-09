package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
	private final ResourceKey<DimensionType> dimensionType;
	private final ResourceKey<Level> dimension;
	private final long seed;
	private final GameType playerGameType;
	@Nullable
	private final GameType previousPlayerGameType;
	private final boolean isDebug;
	private final boolean isFlat;
	private final boolean keepAllPlayerData;
	private final Optional<GlobalPos> lastDeathLocation;

	public ClientboundRespawnPacket(
		ResourceKey<DimensionType> resourceKey,
		ResourceKey<Level> resourceKey2,
		long l,
		GameType gameType,
		@Nullable GameType gameType2,
		boolean bl,
		boolean bl2,
		boolean bl3,
		Optional<GlobalPos> optional
	) {
		this.dimensionType = resourceKey;
		this.dimension = resourceKey2;
		this.seed = l;
		this.playerGameType = gameType;
		this.previousPlayerGameType = gameType2;
		this.isDebug = bl;
		this.isFlat = bl2;
		this.keepAllPlayerData = bl3;
		this.lastDeathLocation = optional;
	}

	public ClientboundRespawnPacket(FriendlyByteBuf friendlyByteBuf) {
		this.dimensionType = friendlyByteBuf.readResourceKey(Registries.DIMENSION_TYPE);
		this.dimension = friendlyByteBuf.readResourceKey(Registries.DIMENSION);
		this.seed = friendlyByteBuf.readLong();
		this.playerGameType = GameType.byId(friendlyByteBuf.readUnsignedByte());
		this.previousPlayerGameType = GameType.byNullableId(friendlyByteBuf.readByte());
		this.isDebug = friendlyByteBuf.readBoolean();
		this.isFlat = friendlyByteBuf.readBoolean();
		this.keepAllPlayerData = friendlyByteBuf.readBoolean();
		this.lastDeathLocation = friendlyByteBuf.readOptional(FriendlyByteBuf::readGlobalPos);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeResourceKey(this.dimensionType);
		friendlyByteBuf.writeResourceKey(this.dimension);
		friendlyByteBuf.writeLong(this.seed);
		friendlyByteBuf.writeByte(this.playerGameType.getId());
		friendlyByteBuf.writeByte(GameType.getNullableId(this.previousPlayerGameType));
		friendlyByteBuf.writeBoolean(this.isDebug);
		friendlyByteBuf.writeBoolean(this.isFlat);
		friendlyByteBuf.writeBoolean(this.keepAllPlayerData);
		friendlyByteBuf.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRespawn(this);
	}

	public ResourceKey<DimensionType> getDimensionType() {
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

	public Optional<GlobalPos> getLastDeathLocation() {
		return this.lastDeathLocation;
	}
}
