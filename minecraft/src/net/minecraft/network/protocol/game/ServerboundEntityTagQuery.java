package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundEntityTagQuery implements Packet<ServerGamePacketListener> {
	private int transactionId;
	private int entityId;

	public ServerboundEntityTagQuery() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundEntityTagQuery(int i, int j) {
		this.transactionId = i;
		this.entityId = j;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.transactionId = friendlyByteBuf.readVarInt();
		this.entityId = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.transactionId);
		friendlyByteBuf.writeVarInt(this.entityId);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleEntityTagQuery(this);
	}

	public int getTransactionId() {
		return this.transactionId;
	}

	public int getEntityId() {
		return this.entityId;
	}
}
