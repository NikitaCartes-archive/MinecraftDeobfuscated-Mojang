package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetHealthPacket implements Packet<ClientGamePacketListener> {
	private final float health;
	private final int food;
	private final float saturation;
	private final int thirst;

	public ClientboundSetHealthPacket(float f, int i, float g, int j) {
		this.health = f;
		this.food = i;
		this.saturation = g;
		this.thirst = j;
	}

	public ClientboundSetHealthPacket(FriendlyByteBuf friendlyByteBuf) {
		this.health = friendlyByteBuf.readFloat();
		this.food = friendlyByteBuf.readVarInt();
		this.saturation = friendlyByteBuf.readFloat();
		this.thirst = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeFloat(this.health);
		friendlyByteBuf.writeVarInt(this.food);
		friendlyByteBuf.writeFloat(this.saturation);
		friendlyByteBuf.writeVarInt(this.thirst);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetHealth(this);
	}

	public float getHealth() {
		return this.health;
	}

	public int getFood() {
		return this.food;
	}

	public float getSaturation() {
		return this.saturation;
	}

	public int getThirst() {
		return this.thirst;
	}
}
