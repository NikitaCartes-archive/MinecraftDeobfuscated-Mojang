package net.minecraft.network.protocol.game;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.ServerScoreboard;

public class ClientboundSetScorePacket implements Packet<ClientGamePacketListener> {
	private final String owner;
	@Nullable
	private final String objectiveName;
	private final int score;
	private final ServerScoreboard.Method method;

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

	public ClientboundSetScorePacket(FriendlyByteBuf friendlyByteBuf) {
		this.owner = friendlyByteBuf.readUtf(40);
		this.method = friendlyByteBuf.readEnum(ServerScoreboard.Method.class);
		String string = friendlyByteBuf.readUtf(16);
		this.objectiveName = Objects.equals(string, "") ? null : string;
		if (this.method != ServerScoreboard.Method.REMOVE) {
			this.score = friendlyByteBuf.readVarInt();
		} else {
			this.score = 0;
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
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

	public String getOwner() {
		return this.owner;
	}

	@Nullable
	public String getObjectiveName() {
		return this.objectiveName;
	}

	public int getScore() {
		return this.score;
	}

	public ServerScoreboard.Method getMethod() {
		return this.method;
	}
}
