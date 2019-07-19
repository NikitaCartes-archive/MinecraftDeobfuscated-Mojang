package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundLoginPacket implements Packet<ClientGamePacketListener> {
	private int playerId;
	private boolean hardcore;
	private GameType gameType;
	private DimensionType dimension;
	private int maxPlayers;
	private LevelType levelType;
	private int chunkRadius;
	private boolean reducedDebugInfo;

	public ClientboundLoginPacket() {
	}

	public ClientboundLoginPacket(int i, GameType gameType, boolean bl, DimensionType dimensionType, int j, LevelType levelType, int k, boolean bl2) {
		this.playerId = i;
		this.dimension = dimensionType;
		this.gameType = gameType;
		this.maxPlayers = j;
		this.hardcore = bl;
		this.levelType = levelType;
		this.chunkRadius = k;
		this.reducedDebugInfo = bl2;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.playerId = friendlyByteBuf.readInt();
		int i = friendlyByteBuf.readUnsignedByte();
		this.hardcore = (i & 8) == 8;
		i &= -9;
		this.gameType = GameType.byId(i);
		this.dimension = DimensionType.getById(friendlyByteBuf.readInt());
		this.maxPlayers = friendlyByteBuf.readUnsignedByte();
		this.levelType = LevelType.getLevelType(friendlyByteBuf.readUtf(16));
		if (this.levelType == null) {
			this.levelType = LevelType.NORMAL;
		}

		this.chunkRadius = friendlyByteBuf.readVarInt();
		this.reducedDebugInfo = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeInt(this.playerId);
		int i = this.gameType.getId();
		if (this.hardcore) {
			i |= 8;
		}

		friendlyByteBuf.writeByte(i);
		friendlyByteBuf.writeInt(this.dimension.getId());
		friendlyByteBuf.writeByte(this.maxPlayers);
		friendlyByteBuf.writeUtf(this.levelType.getName());
		friendlyByteBuf.writeVarInt(this.chunkRadius);
		friendlyByteBuf.writeBoolean(this.reducedDebugInfo);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleLogin(this);
	}

	@Environment(EnvType.CLIENT)
	public int getPlayerId() {
		return this.playerId;
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
	public DimensionType getDimension() {
		return this.dimension;
	}

	@Environment(EnvType.CLIENT)
	public LevelType getLevelType() {
		return this.levelType;
	}

	@Environment(EnvType.CLIENT)
	public int getChunkRadius() {
		return this.chunkRadius;
	}

	@Environment(EnvType.CLIENT)
	public boolean isReducedDebugInfo() {
		return this.reducedDebugInfo;
	}
}
