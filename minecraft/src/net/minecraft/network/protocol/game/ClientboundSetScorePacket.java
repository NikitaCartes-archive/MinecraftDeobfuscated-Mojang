package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.ServerScoreboard;

public class ClientboundSetScorePacket implements Packet<ClientGamePacketListener> {
	private String owner = "";
	@Nullable
	private String objectiveName;
	private int score;
	private ServerScoreboard.Method method;

	public ClientboundSetScorePacket() {
	}

	public ClientboundSetScorePacket(ServerScoreboard.Method method, @Nullable String string, String string2, int i) {
		if (method != ServerScoreboard.Method.REMOVE && string == null) {
			throw new IllegalArgumentException("Need an objective name");
		} else {
			this.owner = string2;
			this.objectiveName = string;
			this.score = i;
			this.method = method;
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.owner = friendlyByteBuf.readUtf(40);
		this.method = friendlyByteBuf.readEnum(ServerScoreboard.Method.class);
		String string = friendlyByteBuf.readUtf(16);
		this.objectiveName = Objects.equals(string, "") ? null : string;
		if (this.method != ServerScoreboard.Method.REMOVE) {
			this.score = friendlyByteBuf.readVarInt();
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeUtf(this.owner);
		friendlyByteBuf.writeEnum(this.method);
		friendlyByteBuf.writeUtf(this.objectiveName == null ? "" : this.objectiveName);
		if (this.method != ServerScoreboard.Method.REMOVE) {
			friendlyByteBuf.writeVarInt(this.score);
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetScore(this);
	}

	@Environment(EnvType.CLIENT)
	public String getOwner() {
		return this.owner;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public String getObjectiveName() {
		return this.objectiveName;
	}

	@Environment(EnvType.CLIENT)
	public int getScore() {
		return this.score;
	}

	@Environment(EnvType.CLIENT)
	public ServerScoreboard.Method getMethod() {
		return this.method;
	}
}
