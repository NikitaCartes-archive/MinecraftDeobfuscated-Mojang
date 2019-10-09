package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.ExperienceOrb;

public class ClientboundAddExperienceOrbPacket implements Packet<ClientGamePacketListener> {
	private int id;
	private double x;
	private double y;
	private double z;
	private int value;

	public ClientboundAddExperienceOrbPacket() {
	}

	public ClientboundAddExperienceOrbPacket(ExperienceOrb experienceOrb) {
		this.id = experienceOrb.getId();
		this.x = experienceOrb.getX();
		this.y = experienceOrb.getY();
		this.z = experienceOrb.getZ();
		this.value = experienceOrb.getValue();
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.id = friendlyByteBuf.readVarInt();
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.value = friendlyByteBuf.readShort();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeShort(this.value);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddExperienceOrb(this);
	}

	@Environment(EnvType.CLIENT)
	public int getId() {
		return this.id;
	}

	@Environment(EnvType.CLIENT)
	public double getX() {
		return this.x;
	}

	@Environment(EnvType.CLIENT)
	public double getY() {
		return this.y;
	}

	@Environment(EnvType.CLIENT)
	public double getZ() {
		return this.z;
	}

	@Environment(EnvType.CLIENT)
	public int getValue() {
		return this.value;
	}
}
