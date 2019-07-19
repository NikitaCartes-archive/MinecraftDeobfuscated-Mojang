package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetExperiencePacket implements Packet<ClientGamePacketListener> {
	private float experienceProgress;
	private int totalExperience;
	private int experienceLevel;

	public ClientboundSetExperiencePacket() {
	}

	public ClientboundSetExperiencePacket(float f, int i, int j) {
		this.experienceProgress = f;
		this.totalExperience = i;
		this.experienceLevel = j;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.experienceProgress = friendlyByteBuf.readFloat();
		this.experienceLevel = friendlyByteBuf.readVarInt();
		this.totalExperience = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
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
