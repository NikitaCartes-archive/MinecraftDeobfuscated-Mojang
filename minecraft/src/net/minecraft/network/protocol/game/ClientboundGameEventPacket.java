package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundGameEventPacket implements Packet<ClientGamePacketListener> {
	public static final String[] EVENT_LANGUAGE_ID = new String[]{"block.minecraft.bed.not_valid"};
	private int event;
	private float param;

	public ClientboundGameEventPacket() {
	}

	public ClientboundGameEventPacket(int i, float f) {
		this.event = i;
		this.param = f;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.event = friendlyByteBuf.readUnsignedByte();
		this.param = friendlyByteBuf.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeByte(this.event);
		friendlyByteBuf.writeFloat(this.param);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleGameEvent(this);
	}

	@Environment(EnvType.CLIENT)
	public int getEvent() {
		return this.event;
	}

	@Environment(EnvType.CLIENT)
	public float getParam() {
		return this.param;
	}
}
