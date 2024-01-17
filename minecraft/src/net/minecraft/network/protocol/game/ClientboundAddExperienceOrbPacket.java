package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.ExperienceOrb;

public class ClientboundAddExperienceOrbPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundAddExperienceOrbPacket> STREAM_CODEC = Packet.codec(
		ClientboundAddExperienceOrbPacket::write, ClientboundAddExperienceOrbPacket::new
	);
	private final int id;
	private final double x;
	private final double y;
	private final double z;
	private final int value;

	public ClientboundAddExperienceOrbPacket(ExperienceOrb experienceOrb) {
		this.id = experienceOrb.getId();
		this.x = experienceOrb.getX();
		this.y = experienceOrb.getY();
		this.z = experienceOrb.getZ();
		this.value = experienceOrb.getValue();
	}

	private ClientboundAddExperienceOrbPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.value = friendlyByteBuf.readShort();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeShort(this.value);
	}

	@Override
	public PacketType<ClientboundAddExperienceOrbPacket> type() {
		return GamePacketTypes.CLIENTBOUND_ADD_EXPERIENCE_ORB;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddExperienceOrb(this);
	}

	public int getId() {
		return this.id;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public double getZ() {
		return this.z;
	}

	public int getValue() {
		return this.value;
	}
}
