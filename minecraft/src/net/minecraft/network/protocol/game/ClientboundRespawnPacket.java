package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
	private ResourceKey<DimensionType> dimensionType;
	private ResourceKey<Level> dimension;
	private long seed;
	private GameType playerGameType;
	private boolean isDebug;
	private boolean isFlat;
	private boolean keepAllPlayerData;

	public ClientboundRespawnPacket() {
	}

	public ClientboundRespawnPacket(
		ResourceKey<DimensionType> resourceKey, ResourceKey<Level> resourceKey2, long l, GameType gameType, boolean bl, boolean bl2, boolean bl3
	) {
		this.dimensionType = resourceKey;
		this.dimension = resourceKey2;
		this.seed = l;
		this.playerGameType = gameType;
		this.isDebug = bl;
		this.isFlat = bl2;
		this.keepAllPlayerData = bl3;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRespawn(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.dimensionType = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, friendlyByteBuf.readResourceLocation());
		this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, friendlyByteBuf.readResourceLocation());
		this.seed = friendlyByteBuf.readLong();
		this.playerGameType = GameType.byId(friendlyByteBuf.readUnsignedByte());
		this.isDebug = friendlyByteBuf.readBoolean();
		this.isFlat = friendlyByteBuf.readBoolean();
		this.keepAllPlayerData = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeResourceLocation(this.dimensionType.location());
		friendlyByteBuf.writeResourceLocation(this.dimension.location());
		friendlyByteBuf.writeLong(this.seed);
		friendlyByteBuf.writeByte(this.playerGameType.getId());
		friendlyByteBuf.writeBoolean(this.isDebug);
		friendlyByteBuf.writeBoolean(this.isFlat);
		friendlyByteBuf.writeBoolean(this.keepAllPlayerData);
	}

	@Environment(EnvType.CLIENT)
	public ResourceKey<DimensionType> getDimensionType() {
		return this.dimensionType;
	}

	@Environment(EnvType.CLIENT)
	public ResourceKey<Level> getDimension() {
		return this.dimension;
	}

	@Environment(EnvType.CLIENT)
	public long getSeed() {
		return this.seed;
	}

	@Environment(EnvType.CLIENT)
	public GameType getPlayerGameType() {
		return this.playerGameType;
	}

	@Environment(EnvType.CLIENT)
	public boolean isDebug() {
		return this.isDebug;
	}

	@Environment(EnvType.CLIENT)
	public boolean isFlat() {
		return this.isFlat;
	}

	@Environment(EnvType.CLIENT)
	public boolean shouldKeepAllPlayerData() {
		return this.keepAllPlayerData;
	}
}
