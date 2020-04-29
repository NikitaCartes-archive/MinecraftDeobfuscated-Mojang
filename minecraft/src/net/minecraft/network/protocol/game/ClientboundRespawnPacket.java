package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
	private DimensionType dimension;
	private long seed;
	private GameType playerGameType;
	private LevelType levelType;
	private boolean keepAllPlayerData;

	public ClientboundRespawnPacket() {
	}

	public ClientboundRespawnPacket(DimensionType dimensionType, long l, LevelType levelType, GameType gameType, boolean bl) {
		this.dimension = dimensionType;
		this.seed = l;
		this.playerGameType = gameType;
		this.levelType = levelType;
		this.keepAllPlayerData = bl;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRespawn(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.dimension = DimensionType.getById(friendlyByteBuf.readInt());
		this.seed = friendlyByteBuf.readLong();
		this.playerGameType = GameType.byId(friendlyByteBuf.readUnsignedByte());
		this.levelType = LevelType.getLevelType(friendlyByteBuf.readUtf(16));
		if (this.levelType == null) {
			this.levelType = LevelType.NORMAL;
		}

		this.keepAllPlayerData = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeInt(this.dimension.getId());
		friendlyByteBuf.writeLong(this.seed);
		friendlyByteBuf.writeByte(this.playerGameType.getId());
		friendlyByteBuf.writeUtf(this.levelType.getName());
		friendlyByteBuf.writeBoolean(this.keepAllPlayerData);
	}

	@Environment(EnvType.CLIENT)
	public DimensionType getDimension() {
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
	public LevelType getLevelType() {
		return this.levelType;
	}

	@Environment(EnvType.CLIENT)
	public boolean shouldKeepAllPlayerData() {
		return this.keepAllPlayerData;
	}
}
