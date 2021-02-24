package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetCarriedItemPacket implements Packet<ClientGamePacketListener> {
	private final int slot;

	public ClientboundSetCarriedItemPacket(int i) {
		this.slot = i;
	}

	public ClientboundSetCarriedItemPacket(FriendlyByteBuf friendlyByteBuf) {
		this.slot = friendlyByteBuf.readByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.slot);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetCarriedItem(this);
	}

	@Environment(EnvType.CLIENT)
	public int getSlot() {
		return this.slot;
	}
}
