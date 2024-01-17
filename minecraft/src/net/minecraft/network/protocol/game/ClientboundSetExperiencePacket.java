package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSetExperiencePacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetExperiencePacket> STREAM_CODEC = Packet.codec(
		ClientboundSetExperiencePacket::write, ClientboundSetExperiencePacket::new
	);
	private final float experienceProgress;
	private final int totalExperience;
	private final int experienceLevel;

	public ClientboundSetExperiencePacket(float f, int i, int j) {
		this.experienceProgress = f;
		this.totalExperience = i;
		this.experienceLevel = j;
	}

	private ClientboundSetExperiencePacket(FriendlyByteBuf friendlyByteBuf) {
		this.experienceProgress = friendlyByteBuf.readFloat();
		this.experienceLevel = friendlyByteBuf.readVarInt();
		this.totalExperience = friendlyByteBuf.readVarInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeFloat(this.experienceProgress);
		friendlyByteBuf.writeVarInt(this.experienceLevel);
		friendlyByteBuf.writeVarInt(this.totalExperience);
	}

	@Override
	public PacketType<ClientboundSetExperiencePacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_EXPERIENCE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetExperience(this);
	}

	public float getExperienceProgress() {
		return this.experienceProgress;
	}

	public int getTotalExperience() {
		return this.totalExperience;
	}

	public int getExperienceLevel() {
		return this.experienceLevel;
	}
}
