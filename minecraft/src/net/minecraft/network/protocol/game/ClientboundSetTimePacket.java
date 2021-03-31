package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetTimePacket implements Packet<ClientGamePacketListener> {
	private final long gameTime;
	private final long dayTime;

	public ClientboundSetTimePacket(long l, long m, boolean bl) {
		this.gameTime = l;
		long n = m;
		if (!bl) {
			n = -m;
			if (n == 0L) {
				n = -1L;
			}
		}

		this.dayTime = n;
	}

	public ClientboundSetTimePacket(FriendlyByteBuf friendlyByteBuf) {
		this.gameTime = friendlyByteBuf.readLong();
		this.dayTime = friendlyByteBuf.readLong();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeLong(this.gameTime);
		friendlyByteBuf.writeLong(this.dayTime);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetTime(this);
	}

	public long getGameTime() {
		return this.gameTime;
	}

	public long getDayTime() {
		return this.dayTime;
	}
}
