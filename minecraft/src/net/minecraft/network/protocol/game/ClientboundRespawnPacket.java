package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
	private ResourceLocation dimension;
	private long seed;
	private GameType playerGameType;
	private boolean isDebug;
	private boolean isFlat;
	private boolean keepAllPlayerData;

	public ClientboundRespawnPacket() {
	}

	public ClientboundRespawnPacket(ResourceLocation resourceLocation, long l, GameType gameType, boolean bl, boolean bl2, boolean bl3) {
		this.dimension = resourceLocation;
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
		this.dimension = friendlyByteBuf.readResourceLocation();
		this.seed = friendlyByteBuf.readLong();
		this.playerGameType = GameType.byId(friendlyByteBuf.readUnsignedByte());
		this.isDebug = friendlyByteBuf.readBoolean();
		this.isFlat = friendlyByteBuf.readBoolean();
		this.keepAllPlayerData = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeResourceLocation(this.dimension);
		friendlyByteBuf.writeLong(this.seed);
		friendlyByteBuf.writeByte(this.playerGameType.getId());
		friendlyByteBuf.writeBoolean(this.isDebug);
		friendlyByteBuf.writeBoolean(this.isFlat);
		friendlyByteBuf.writeBoolean(this.keepAllPlayerData);
	}

	@Environment(EnvType.CLIENT)
	public ResourceLocation getDimension() {
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
