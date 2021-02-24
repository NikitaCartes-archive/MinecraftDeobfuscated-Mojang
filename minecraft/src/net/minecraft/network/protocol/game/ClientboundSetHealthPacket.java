package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetHealthPacket implements Packet<ClientGamePacketListener> {
	private final float health;
	private final int food;
	private final float saturation;

	public ClientboundSetHealthPacket(float f, int i, float g) {
		this.health = f;
		this.food = i;
		this.saturation = g;
	}

	public ClientboundSetHealthPacket(FriendlyByteBuf friendlyByteBuf) {
		this.health = friendlyByteBuf.readFloat();
		this.food = friendlyByteBuf.readVarInt();
		this.saturation = friendlyByteBuf.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeFloat(this.health);
		friendlyByteBuf.writeVarInt(this.food);
		friendlyByteBuf.writeFloat(this.saturation);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetHealth(this);
	}

	@Environment(EnvType.CLIENT)
	public float getHealth() {
		return this.health;
	}

	@Environment(EnvType.CLIENT)
	public int getFood() {
		return this.food;
	}

	@Environment(EnvType.CLIENT)
	public float getSaturation() {
		return this.saturation;
	}
}
