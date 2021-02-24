package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetExperiencePacket implements Packet<ClientGamePacketListener> {
	private final float experienceProgress;
	private final int totalExperience;
	private final int experienceLevel;

	public ClientboundSetExperiencePacket(float f, int i, int j) {
		this.experienceProgress = f;
		this.totalExperience = i;
		this.experienceLevel = j;
	}

	public ClientboundSetExperiencePacket(FriendlyByteBuf friendlyByteBuf) {
		this.experienceProgress = friendlyByteBuf.readFloat();
		this.experienceLevel = friendlyByteBuf.readVarInt();
		this.totalExperience = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeFloat(this.experienceProgress);
		friendlyByteBuf.writeVarInt(this.experienceLevel);
		friendlyByteBuf.writeVarInt(this.totalExperience);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetExperience(this);
	}

	@Environment(EnvType.CLIENT)
	public float getExperienceProgress() {
		return this.experienceProgress;
	}

	@Environment(EnvType.CLIENT)
	public int getTotalExperience() {
		return this.totalExperience;
	}

	@Environment(EnvType.CLIENT)
	public int getExperienceLevel() {
		return this.experienceLevel;
	}
}
