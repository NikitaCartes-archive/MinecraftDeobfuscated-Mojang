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
	private long seed;
	private boolean hardcore;
	private GameType gameType;
	private DimensionType dimension;
	private int maxPlayers;
	private LevelType levelType;
	private int chunkRadius;
	private boolean reducedDebugInfo;
	private boolean showDeathScreen;

	public ClientboundLoginPacket() {
	}

	public ClientboundLoginPacket(
		int i, GameType gameType, long l, boolean bl, DimensionType dimensionType, int j, LevelType levelType, int k, boolean bl2, boolean bl3
	) {
		this.playerId = i;
		this.dimension = dimensionType;
		this.seed = l;
		this.gameType = gameType;
		this.maxPlayers = j;
		this.hardcore = bl;
		this.levelType = levelType;
		this.chunkRadius = k;
		this.reducedDebugInfo = bl2;
		this.showDeathScreen = bl3;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.playerId = friendlyByteBuf.readInt();
		int i = friendlyByteBuf.readUnsignedByte();
		this.hardcore = (i & 8) == 8;
		i &= -9;
		this.gameType = GameType.byId(i);
		this.dimension = DimensionType.getById(friendlyByteBuf.readInt());
		this.seed = friendlyByteBuf.readLong();
		this.maxPlayers = friendlyByteBuf.readUnsignedByte();
		this.levelType = LevelType.getLevelType(friendlyByteBuf.readUtf(16));
		if (this.levelType == null) {
			this.levelType = LevelType.NORMAL;
		}

		this.chunkRadius = friendlyByteBuf.readVarInt();
		this.reducedDebugInfo = friendlyByteBuf.readBoolean();
		this.showDeathScreen = friendlyByteBuf.readBoolean();
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
		friendlyByteBuf.writeLong(this.seed);
		friendlyByteBuf.writeByte(this.maxPlayers);
		friendlyByteBuf.writeUtf(this.levelType.getName());
		friendlyByteBuf.writeVarInt(this.chunkRadius);
		friendlyByteBuf.writeBoolean(this.reducedDebugInfo);
		friendlyByteBuf.writeBoolean(this.showDeathScreen);
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

	@Environment(EnvType.CLIENT)
	public boolean shouldShowDeathScreen() {
		return this.showDeathScreen;
	}
}
