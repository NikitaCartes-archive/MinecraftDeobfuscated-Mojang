package net.minecraft.network.protocol.login;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundCustomQueryPacket implements Packet<ServerLoginPacketListener> {
	private static final int MAX_PAYLOAD_SIZE = 1048576;
	private final int transactionId;
	private final FriendlyByteBuf data;

	public ServerboundCustomQueryPacket(int i, @Nullable FriendlyByteBuf friendlyByteBuf) {
		this.transactionId = i;
		this.data = friendlyByteBuf;
	}

	public ServerboundCustomQueryPacket(FriendlyByteBuf friendlyByteBuf) {
		this.transactionId = friendlyByteBuf.readVarInt();
		if (friendlyByteBuf.readBoolean()) {
			int i = friendlyByteBuf.readableBytes();
			if (i < 0 || i > 1048576) {
				throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
			}

			this.data = new FriendlyByteBuf(friendlyByteBuf.readBytes(i));
		} else {
			this.data = null;
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.transactionId);
		if (this.data != null) {
			friendlyByteBuf.writeBoolean(true);
			friendlyByteBuf.writeBytes(this.data.copy());
		} else {
			friendlyByteBuf.writeBoolean(false);
		}
	}

	public void handle(ServerLoginPacketListener serverLoginPacketListener) {
		serverLoginPacketListener.handleCustomQueryPacket(this);
	}

	public int getTransactionId() {
		return this.transactionId;
	}

	public FriendlyByteBuf getData() {
		return this.data;
	}
}
