package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetTimePacket implements Packet<ClientGamePacketListener> {
	private long gameTime;
	private long dayTime;

	public ClientboundSetTimePacket() {
	}

	public ClientboundSetTimePacket(long l, long m, boolean bl) {
		this.gameTime = l;
		this.dayTime = m;
		if (!bl) {
			this.dayTime = -this.dayTime;
			if (this.dayTime == 0L) {
				this.dayTime = -1L;
			}
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.gameTime = friendlyByteBuf.readLong();
		this.dayTime = friendlyByteBuf.readLong();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeLong(this.gameTime);
		friendlyByteBuf.writeLong(this.dayTime);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetTime(this);
	}

	@Environment(EnvType.CLIENT)
	public long getGameTime() {
		return this.gameTime;
	}

	@Environment(EnvType.CLIENT)
	public long getDayTime() {
		return this.dayTime;
	}
}
