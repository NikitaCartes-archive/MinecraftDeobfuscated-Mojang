package net.minecraft.network.protocol.login;

import java.io.IOException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundCustomQueryPacket implements Packet<ServerLoginPacketListener> {
	private int transactionId;
	private FriendlyByteBuf data;

	public ServerboundCustomQueryPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundCustomQueryPacket(int i, @Nullable FriendlyByteBuf friendlyByteBuf) {
		this.transactionId = i;
		this.data = friendlyByteBuf;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.transactionId = friendlyByteBuf.readVarInt();
		if (friendlyByteBuf.readBoolean()) {
			int i = friendlyByteBuf.readableBytes();
			if (i < 0 || i > 1048576) {
				throw new IOException("Payload may not be larger than 1048576 bytes");
			}

			this.data = new FriendlyByteBuf(friendlyByteBuf.readBytes(i));
		} else {
			this.data = null;
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
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
}
