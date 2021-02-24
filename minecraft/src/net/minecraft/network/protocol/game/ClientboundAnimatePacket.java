package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundAnimatePacket implements Packet<ClientGamePacketListener> {
	private final int id;
	private final int action;

	public ClientboundAnimatePacket(Entity entity, int i) {
		this.id = entity.getId();
		this.action = i;
	}

	public ClientboundAnimatePacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
		this.action = friendlyByteBuf.readUnsignedByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeByte(this.action);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAnimate(this);
	}

	@Environment(EnvType.CLIENT)
	public int getId() {
		return this.id;
	}

	@Environment(EnvType.CLIENT)
	public int getAction() {
		return this.action;
	}
}
