package net.minecraft.network.protocol.login;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundCustomQueryPacket implements Packet<ServerLoginPacketListener> {
	private static final int MAX_PAYLOAD_SIZE = 1048576;
	private final int transactionId;
	@Nullable
	private final FriendlyByteBuf data;

	public ServerboundCustomQueryPacket(int i, @Nullable FriendlyByteBuf friendlyByteBuf) {
		this.transactionId = i;
		this.data = friendlyByteBuf;
	}

	public ServerboundCustomQueryPacket(FriendlyByteBuf friendlyByteBuf) {
		this.transactionId = friendlyByteBuf.readVarInt();
		this.data = friendlyByteBuf.readNullable(friendlyByteBufx -> {
			int i = friendlyByteBufx.readableBytes();
			if (i >= 0 && i <= 1048576) {
				return new FriendlyByteBuf(friendlyByteBufx.readBytes(i));
			} else {
				throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
			}
		});
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.transactionId);
		friendlyByteBuf.writeNullable(this.data, (friendlyByteBufx, friendlyByteBuf2) -> friendlyByteBufx.writeBytes(friendlyByteBuf2.slice()));
	}

	public void handle(ServerLoginPacketListener serverLoginPacketListener) {
		serverLoginPacketListener.handleCustomQueryPacket(this);
	}

	public int getTransactionId() {
		return this.transactionId;
	}

	@Nullable
	public FriendlyByteBuf getData() {
		return this.data;
	}
}
